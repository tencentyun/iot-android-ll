package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.MemberEntity

/**
 * 家庭成员列表响应实体
 */
class MemberListResponse {
    var MemberList = arrayListOf<MemberEntity>()
    var Total =0
    var RequestId =""
}