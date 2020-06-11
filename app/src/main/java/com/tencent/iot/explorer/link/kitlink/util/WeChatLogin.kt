package com.kitlink.util

import android.app.Activity
import com.kitlink.R
import com.kitlink.wxapi.WXEntryActivity
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.util.T


/**
 * 微信登录
 */
class WeChatLogin {

    private constructor()

    companion object {
        const val APP_ID = "wxfb36c49df3a370c7"
        private var login: WeChatLogin? = null

        fun getInstance(): WeChatLogin {
            if (login == null) {
                synchronized(this) {
                    if (login == null) {
                        login = WeChatLogin()
                    }
                    return login!!
                }
            }
            return login!!
        }
    }

    fun login(activity: Activity, listener: OnLoginListener) {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        val api = WXAPIFactory.createWXAPI(activity, APP_ID, true)
        if (api.isWXAppInstalled) {
            // 将应用的appId注册到微信
            if (api.registerApp(APP_ID)) {
                WXEntryActivity.onLoginListener = listener
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo"
                req.state = "tenext"
                api?.sendReq(req)
            }
        } else {
            T.show(activity.resources.getString(R.string.not_wechat_client))
        }
    }

    interface OnLoginListener {
        fun onSuccess(reqCode: String)
        fun cancel()
        fun onFail(msg:String)
    }


}