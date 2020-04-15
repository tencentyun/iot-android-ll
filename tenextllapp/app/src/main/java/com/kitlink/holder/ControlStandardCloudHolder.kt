package com.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.kitlink.R
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_standard_long.view.*

/**
 * 云端定时长按钮
 */
class ControlStandardCloudHolder : CRecyclerView.FootViewHolder<Any> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        itemView.tv_standard_long_name.text = getString(R.string.cloud_timing)
        itemView.tv_standard_long_value.visibility = View.INVISIBLE
        itemView.setOnClickListener { footListener?.doAction(this, it, 0) }
    }
}