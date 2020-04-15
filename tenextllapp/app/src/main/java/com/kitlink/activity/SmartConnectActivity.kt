package com.kitlink.activity

import android.text.TextUtils
import com.kitlink.R
import com.kitlink.fragment.*
import com.kitlink.popup.CommonPopupWindow
import com.mvp.IPresenter
import com.util.T
import com.kitlink.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_smart_connect.*

/**
 * 智能配网
 */
class SmartConnectActivity : BaseActivity() {

    private lateinit var scStepFragment: SCStepFragment
    private lateinit var wifiFragment: WifiFragment
    private lateinit var connectProgressFragment: ConnectProgressFragment

    private var closePopup: CommonPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_smart_connect
    }

    override fun initView() {
        scStepFragment = SCStepFragment()
        scStepFragment.onNextListener = object : SCStepFragment.OnNextListener {
            override fun onNext() {
                showFragment(wifiFragment, scStepFragment)
                showTitle(
                    getString(R.string.smart_config_second_title),
                    getString(R.string.cancel)
                )
            }
        }
        wifiFragment = WifiFragment(WifiFragment.smart_config)
        wifiFragment.onCommitWifiListener = object : WifiFragment.OnCommitWifiListener {
            override fun commitWifi(ssid: String, bssid: String?, password: String) {
                if (TextUtils.isEmpty(bssid)) {
                    T.show(getString(R.string.connecting_to_wifi))
                    return
                }
                showTitle(
                    getString(R.string.smart_config_third_connect_progress)
                    , getString(R.string.close)
                )
                connectProgressFragment.setWifiInfo(ssid, bssid!!, password)
                showFragment(connectProgressFragment, wifiFragment)
            }
        }
        connectProgressFragment = ConnectProgressFragment(WifiFragment.smart_config)
        connectProgressFragment.onRestartListener =
            object : ConnectProgressFragment.OnRestartListener {
                override fun restart() {
                    showFragment(scStepFragment, connectProgressFragment)
                    showTitle(
                        getString(R.string.smart_config),
                        getString(R.string.close)
                    )
                }
            }
        supportFragmentManager.beginTransaction()
            .add(R.id.container_smart_connect, scStepFragment)
            .commit()
    }

    private fun showTitle(title: String, cancel: String) {
        tv_smart_connect_title.text = title
        tv_smart_connect_cancel.text = cancel
    }

    private fun showFragment(showFragment: BaseFragment, hideFragment: BaseFragment) {
        val transaction = this.supportFragmentManager.beginTransaction()
        if (showFragment.isAdded) {
            transaction.show(showFragment).hide(hideFragment)
                .commit()
        } else {
            transaction.add(R.id.container_smart_connect, showFragment)
                .hide(hideFragment)
                .commit()
        }
    }

    override fun setListener() {
        tv_smart_connect_cancel.setOnClickListener {
            if (connectProgressFragment.isVisible) {
                showPopup()
            } else {
                finish()
            }
        }
    }

    private fun showPopup() {
        if (closePopup == null) {
            closePopup = CommonPopupWindow(this)
            closePopup?.setCommonParams(
                getString(R.string.exit_toast_title),
                getString(R.string.exit_toast_content)
            )
            closePopup?.setMenuText(getString(R.string.cancel), getString(R.string.confirm))
            closePopup?.setBg(smart_config_bg)
            closePopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
                override fun confirm(popupWindow: CommonPopupWindow) {
                    finish()
                }

                override fun cancel(popupWindow: CommonPopupWindow) {
                    popupWindow.dismiss()
                }
            }
        }
        closePopup?.show(smart_config)
    }

    override fun onBackPressed() {
        if (connectProgressFragment.isVisible) {
            showPopup()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        closePopup?.dismiss()
        super.onDestroy()
    }
}
