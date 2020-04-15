package com.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.kitlink.R
import com.kitlink.entity.MemberEntity
import com.view.recyclerview.CRecyclerView
import com.yho.image.imp.ImageManager
import kotlinx.android.synthetic.main.item_member.view.*

/**
 * 家庭成员列表
 */
class MemberListViewHolder : CRecyclerView.CViewHolder<MemberEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_member_item_name.text = NickName
            ImageManager.setImagePath(
                itemView.context,
                itemView.iv_member_item_portrait,
                Avatar,
                R.mipmap.image_default_portrait
            )
            itemView.tv_member_item_role.text = if (Role == 1)
                getString(R.string.role_owner) else getString(R.string.role_member)
        }
        itemView.setOnClickListener { recyclerItemView?.doAction(this, itemView, position) }
    }
}