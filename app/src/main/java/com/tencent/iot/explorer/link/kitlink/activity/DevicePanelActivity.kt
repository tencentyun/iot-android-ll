package com.tencent.iot.explorer.link.kitlink.activity

import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.webview.WebCallBack
import kotlinx.android.synthetic.main.activity_device_panel.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class DevicePanelActivity: BaseActivity(), View.OnClickListener, MyCallback {

    private var callback: WebCallBack? = null
    private var deviceEntity: DeviceEntity? = null

    override fun getContentView(): Int {
        return  R.layout.activity_device_panel
    }

    override fun initView() {
        iv_back.setColorFilter(R.color.black_333333)
        tv_title.text = ""
        deviceEntity = get("device")
        getAppGetTokenTicket()
    }

    /**
     * 获取一次性的 TokenTicket
     */
    private fun getAppGetTokenTicket() {
        HttpRequest.instance.getOneTimeTokenTicket(this)
    }

    override fun setListener() {
    }

    override fun onClick(v: View?) {
    }

    override fun fail(msg: String?, reqCode: Int) {
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.code == 0) {
            val js = JSON.parse(response.data.toString()) as JSONObject
            var weburl = "https://iot.cloud.tencent.com/scf/h5panel/?deviceId=${deviceEntity?.DeviceId}&familyId=${deviceEntity?.FamilyId}&roomId=${deviceEntity?.RoomId}&familyType=0&lid=15&quid=10&ticket=${js[CommonField.TOKEN_TICKET]}" +
                    "&appId=com.tencent.iot.explorer.link.opensource&platform=android&regionId=1"
            showUrl(weburl)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        device_panel_webview.clearHistory()
        device_panel_webview.clearCache(true)
        device_panel_webview.loadUrl("about:blank")
    }

    private fun showUrl(url: String) {
        device_panel_webview.settings.javaScriptEnabled = true  // 设置js支持
        device_panel_webview.settings.domStorageEnabled = true  // 开启 DOM storage API 功能
        device_panel_webview.settings.textZoom = 100            // 设置网页字体不跟随系统字体发生改变
        device_panel_webview.settings.useWideViewPort = true    // 缩放至屏幕的大小
        device_panel_webview.settings.loadWithOverviewMode = true
        // 是否支持缩放
        device_panel_webview.settings.setSupportZoom(true)
        device_panel_webview.settings.builtInZoomControls = true
        device_panel_webview.settings.displayZoomControls = false
        device_panel_webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE     // 不缓存
        device_panel_webview.settings.allowContentAccess = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            device_panel_webview.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE;
        }
        device_panel_webview.settings.blockNetworkImage = false

        device_panel_webview.loadUrl(url)
        callback = WebCallBack(device_panel_webview)

        device_panel_webview.webViewClient = webViewClient
        device_panel_webview.webChromeClient = webChromeClient
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            val jsObject = JSONObject()
            val jsObject2 = JSONObject()
            jsObject2.put("name", "pageShow")
            jsObject.put(CommonField.HANDLER_NAME, "emitEvent")
            jsObject.put("data", jsObject2)
            callback?.apply(jsObject)
        }
    }

    override fun onPause() {
        super.onPause()
        if (callback != null) {
            val jsObject = JSONObject()
            val jsObject2 = JSONObject()
            jsObject2.put("name", "pageHide")
            jsObject.put(CommonField.HANDLER_NAME, "emitEvent")
            jsObject.put("data", jsObject2)
            callback?.apply(jsObject)
        }
    }

    private val webChromeClient = object: WebChromeClient() {

        override fun onShowFileChooser (webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams?): Boolean {
            return true
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            tv_title.text = title
        }

        override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
        }

        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            L.e("onJsPrompt: " + url)
            return true
        }
    }

    private val webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            L.e("shouldOverrideUrlLoading: " + url)
            if (url.contains("goDeviceDetailPage")) {
                if (callback != null) {
                    var uri = Uri.parse(url)
                    var json = JSON.parseObject(uri.query)
                    var id = json["callbackId"]
                    val jsObject = JSONObject()
                    val jsObject2 = JSONObject()
                    jsObject2.put("result", "true")
                    jsObject2.put("callbackId", id)
                    jsObject2.put("data", "archurtest")
                    jsObject.put(CommonField.HANDLER_NAME, "callResult")
                    jsObject.put("data", jsObject2)
                    callback?.apply(jsObject)
                    return true
                }
            }
            return true
        }
    }
}