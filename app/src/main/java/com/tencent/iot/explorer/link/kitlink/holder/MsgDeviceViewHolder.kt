package com.kitlink.holder

import android.view.View
import com.kitlink.R
import com.kitlink.entity.MessageEntity
import com.util.date.DateFormatUtil
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_message_device.view.*

class MsgDeviceViewHolder : CRecyclerView.CViewHolder<MessageEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.let {
            itemView.run {
                tv_message_title.text = it.MsgTitle
                tv_message_content.text = it.MsgContent
                iv_icon_message.setImageResource(R.mipmap.icon_light)
                tv_message_time.text =
                    DateFormatUtil.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
            }
        }
        itemView.rl_delete_message.setOnClickListener {
            it.tag = 2
            recyclerItemView?.doAction(this, it, position)
        }
    }
}