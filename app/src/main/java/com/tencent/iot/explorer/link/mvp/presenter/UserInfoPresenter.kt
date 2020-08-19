package com.tencent.iot.explorer.link.mvp.presenter

import android.content.Context
import com.tencent.iot.explorer.link.mvp.model.UserInfoModel
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.view.UserInfoView

/**
 * 个人信息
 */
class UserInfoPresenter(view: UserInfoView) : ParentPresenter<UserInfoModel, UserInfoView>(view) {
    override fun getIModel(view: UserInfoView): UserInfoModel {
        return UserInfoModel(view)
    }

    fun upload(context: Context, srcPath: String) {
        model?.upload(context, srcPath)
    }

    fun logout() {
        model?.logout()
    }

    fun getUserInfo() {
        model?.getUserInfo()
    }

    fun modifyNick(nick: String) {
        model?.modifyNick(nick)
    }

    fun setTemperatureUnit(unit: String) {
        model?.setTemperatureUnit(unit)
    }

    fun getUserSetting() {
        model?.getUserSetting()
    }

    fun getGlobalConfig(key: String) {
        model?.getGlobalConfig(key)
    }
}