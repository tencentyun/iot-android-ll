package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.customview.dialog.WifiHelperDialog
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.core.utils.LocationUtil
import com.tencent.iot.explorer.link.core.utils.KeyBoardUtils
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import kotlinx.android.synthetic.main.fragment_wifi.*
import kotlinx.android.synthetic.main.smart_config_second.*

/**
 * 输入wifi密码
 */
class WifiFragment() : BaseFragment() {

    private var type = 0
    private var wifiInfo: WifiInfo? = null
    private var bssid = ""
    var showTipTag = false

    constructor(type: Int, showTag: Boolean) : this() {
        this.type = type
        showTipTag = showTag
    }

    var onCommitWifiListener: OnCommitWifiListener? = null
    var openWifiDialog: WifiHelperDialog? = null
    var openLocationServiceDialog: WifiHelperDialog? = null

    companion object {
        const val smart_config = 0
        const val soft_ap = 1
    }

    init {
        showWifiInfo()
    }

    /**
     * 展示wifi
     */
    private fun showWifiInfo() {
        context?.let {
            val wifiManager = it.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiInfo = wifiManager.connectionInfo

            if (wifiInfo == null || wifiInfo!!.bssid == null) {
                tv_select_wifi.hint = getString(R.string.not_network)
                tv_wifi_commit.isEnabled = false
                tv_select_wifi.setText("")
                openWifiDialog?.show()
            } else {
                var ssid2Set = wifiManager.connectionInfo.ssid.replace("\"", "")
                if (!LocationUtil.isLocationServiceEnable(context)) {
                    tv_select_wifi.hint = getString(R.string.open_location_tip)
                    ssid2Set = ""
                    openLocationServiceDialog?.show()
                }
                tv_select_wifi.setText(ssid2Set)
                if (tv_select_wifi.text.contains(CommonField.SSID_UNKNOWN)) {
                    T.show(getString(R.string.open_location_tip))
                }
                bssid = wifiInfo!!.bssid
            }
            tv_select_wifi.isEnabled = type == soft_ap

            isNextClickable()

            if (showTipTag) {
                tv_method.visibility = View.VISIBLE
                tv_method_tip.visibility = View.VISIBLE
                tv_tip_wifi.setText(R.string.connect_dev_wifi)
                et_select_wifi_pwd.setHint( R.string.smart_config_second_hint_not_required)
            } else {
                tv_method.visibility = View.GONE
                tv_method_tip.visibility = View.GONE
                tv_tip_wifi.setText(R.string.input_wifi_pwd)
            }
        }
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_wifi
    }

    override fun onResume() {
        showWifiInfo()
        super.onResume()
    }

    override fun startHere(view: View) {
        openWifiDialog = WifiHelperDialog(context, getString(R.string.please_open_wifi))
        openWifiDialog?.setOnDismisListener(object: WifiHelperDialog.OnDismisListener{
            override fun onOkClicked() {
                startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
            }
            override fun onCancelClicked() {}
        })
        openLocationServiceDialog = WifiHelperDialog(context, getString(R.string.please_open_location_service))
        openLocationServiceDialog?.setOnDismisListener(object: WifiHelperDialog.OnDismisListener{
            override fun onOkClicked() {
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            override fun onCancelClicked() {}
        })
        showWifiInfo()
        et_select_wifi_pwd.addClearImage(iv_wifi_eye_clear)
        et_select_wifi_pwd.addShowImage(iv_wifi_eye, R.mipmap.icon_visible, R.mipmap.icon_invisible)

        tv_select_wifi.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        et_select_wifi_pwd.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isNextClickable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        tv_wifi_commit.setOnClickListener {
            wifiInfo?.let {
                onCommitWifiListener?.commitWifi(
                    it.ssid.replace("\"", ""),
                    it.bssid,
                    et_select_wifi_pwd.text.trim().toString()
                )
            }
            KeyBoardUtils.hideKeyBoard(
                context,
                et_select_wifi_pwd
            )
        }

        container_wifi.setOnClickListener {
            KeyBoardUtils.hideKeyBoard(context, et_select_wifi_pwd)
        }

        iv_select_wifi.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun isNextClickable() {
        if (tv_select_wifi.text != null && (TextUtils.isEmpty(tv_select_wifi.text.toString())) ||
            tv_select_wifi.text.toString().equals(CommonField.SSID_UNKNOWN) ||
            (!showTipTag && et_select_wifi_pwd.text != null && (TextUtils.isEmpty(et_select_wifi_pwd.text.toString())))) {
            tv_wifi_commit.isClickable = false
            tv_wifi_commit.background = getDrawable(context!!, R.drawable.bg_edit)
            return
        }

        tv_wifi_commit.isClickable = true
        tv_wifi_commit.background = getDrawable(context!!, R.drawable.btn_bg)
    }


    interface OnCommitWifiListener {
        fun commitWifi(ssid: String, bssid: String?, password: String)
    }
}