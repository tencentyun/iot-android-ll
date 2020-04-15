package com.kitlink.holder

import android.content.Context
import android.view.KeyEvent
import android.view.ViewGroup
import com.kitlink.R
import com.kitlink.entity.DeviceEntity
import com.kitlink.entity.DevicePropertyEntity
import com.kitlink.util.DataHolder
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_simple_big_switch.view.*

/**
 * 暗黑主题大按钮：布尔 开关类型
 */
class ControlSimpleSwitchBigHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_simple_big_switch_text.text = if (getValue() == "1") {
                itemView.iv_simple_big_switch.setImageResource(R.drawable.icon_control_simple_switch_on_1)
                "$name：${getString(R.string.on)}"
            } else {
                itemView.iv_simple_big_switch.setImageResource(R.drawable.icon_control_simple_switch_off_1)
                "$name：${getString(R.string.off)}"
            }
            DataHolder.instance.get<DeviceEntity>("device")?.let {
                if (it.online == 1) {
                    itemView.tab_simple_switch.setOnTouchListener { v, event ->
                        when (event.action) {
                            KeyEvent.ACTION_DOWN -> {
                                if (getValue() == "1")
                                    itemView.iv_simple_big_switch.setImageResource(R.drawable.icon_control_simple_switch_on_2)
                                else
                                    itemView.iv_simple_big_switch.setImageResource(R.drawable.icon_control_simple_switch_off_2)
                            }
                            KeyEvent.ACTION_UP -> {
                                if (getValue() == "1")
                                    itemView.iv_simple_big_switch.setImageResource(R.drawable.icon_control_simple_switch_on_1)
                                else
                                    itemView.iv_simple_big_switch.setImageResource(R.drawable.icon_control_simple_switch_off_1)
                                recyclerItemView?.doAction(
                                    this@ControlSimpleSwitchBigHolder,
                                    v,
                                    position
                                )
                            }
                        }
                        true
                    }
                }
            }
        }
        itemView.tab_simple_switch.setOnClickListener {
            itemView.iv_simple_big_switch.isPressed = true
            recyclerItemView?.doAction(this, it, position)
        }
    }
}