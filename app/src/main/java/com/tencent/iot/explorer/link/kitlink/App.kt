package com.tencent.iot.explorer.link.kitlink

import android.app.Application
import android.content.Intent
import androidx.multidex.MultiDex
import com.tencent.iot.explorer.link.auth.IoTAuth
import com.tencent.iot.explorer.link.kitlink.activity.LoginActivity
import com.tencent.iot.explorer.link.kitlink.util.AppData
import com.tencent.iot.explorer.link.kitlink.util.Weak
import com.tencent.android.tpush.XGPushConfig
import com.tencent.iot.explorer.link.util.L
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity

/**
 * APP
 */
class App : Application() {

    companion object {
        //app数据
        val data = AppData.instance

        const val CONFIG = "config"
        var language: String? = ""
        //打开测试版本
        const val DEBUG_VERSION: Boolean = true
        const val PULL_OTHER: Boolean = false
        var activity by Weak<BaseActivity>()

        /**
         * 去登录
         */
        @Synchronized
        fun toLogin() {
            activity?.run {
                App.data.clear()
                startActivity(Intent(activity, LoginActivity::class.java))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        IoTAuth.init()
        //初始化弹框
        T.setContext(this.applicationContext)
        //日志开关
        L.isLog(DEBUG_VERSION)
        //信鸽推送日志开关
        XGPushConfig.enableDebug(applicationContext, DEBUG_VERSION)
        XGPushConfig.enablePullUpOtherApp(applicationContext, PULL_OTHER)
        language = SharePreferenceUtil.getString(this, CONFIG, "language")
        data.readLocalUser(this)
    }

    /**
     * 应用销毁
     */
    override fun onTerminate() {
        super.onTerminate()
        //关闭WebSocket
        IoTAuth.destroy()
    }

}