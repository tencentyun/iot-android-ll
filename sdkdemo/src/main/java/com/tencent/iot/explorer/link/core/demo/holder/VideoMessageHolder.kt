package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.entity.VideoMessageEntity
import kotlinx.android.synthetic.main.item_video_message.view.*

class VideoMessageHolder : BaseHolder<VideoMessageEntity> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_device_name.text = deviceName
        }
    }
}