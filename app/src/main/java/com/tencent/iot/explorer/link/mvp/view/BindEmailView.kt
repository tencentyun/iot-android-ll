package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.mvp.ParentView

interface BindEmailView : ParentView {
    fun bindSuccess()
    fun bindFail(msg: ErrorMessage)
    fun sendVerifyCodeSuccess()
    fun sendVerifyCodeFail(msg: ErrorMessage)
}