package com.auth.message.upload

class ActivePushMessage(deviceIds: ArrayString) : UploadMessage() {

    init {
        action = "ActivePush"
        commonParams["DeviceIds"] = deviceIds
    }

    override fun toString(): String {
        reqId = 1
        return super.toString()
    }

}