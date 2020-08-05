package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.model.RegisterModel
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.view.RegisterView

class RegisterPresenter(view: RegisterView) : ParentPresenter<RegisterModel, RegisterView>(view) {

    override fun getIModel(view: RegisterView): RegisterModel {
        return RegisterModel(view)
    }

    fun setCountryCode(countryCode: String) {
        model?.setCountryCode(countryCode)
    }

    fun getCountryCode(): String {
        return model!!.getCountryCode()
    }

    fun setMobilePhone(phone: String) {
        model?.phone = phone
    }

    fun setEmailAddress(email: String) {
        model?.email = email
    }

    /**
     * 获取手机号验证码
     */
    fun requestPhoneCode() {
        model?.run {
            if (isAgreement()) {
                requestPhoneCode()
            }
        }
    }

    /**
     * 获取邮箱验证码
     */
    fun requestEmailCode() {
        model?.run {
            if (isAgreement()) {
                requestEmailCode()
            }
        }
    }

    /**
     * 同意或不同意用户协议
     */
    fun agreement() {
        model?.agreement()
    }

    /**
     * 是否同意协议
     */
    fun isAgreement(): Boolean {
        return model!!.isAgreement()
    }

}