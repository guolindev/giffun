/*
 * Copyright (C) guolin, Suzhou Quxiang Inc. Open source codes for study only.
 * Do not use for commercial purpose.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quxianggif.comments.adapter

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.common.holder.CommentHolder
import com.quxianggif.core.model.Comment
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.network.model.GoodComment
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.DateUtil
import com.quxianggif.util.glide.CustomUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * Feed详情界面的热门评论所使用的adapter。
 *
 * @author davy, guolin
 * @since 17/7/17
 */
internal class HotCommentsAdapter(private val activity: Activity, private val comments: MutableList<Comment>) : RecyclerView.Adapter<CommentHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.hot_comment_item, parent, false)
        val holder = CommentHolder(view)
        val avatar = view.findViewById<ImageView>(R.id.avatar)
        val nickname = view.findViewById<TextView>(R.id.nickname)
        val goodImage = view.findViewById<ImageView>(R.id.goodImage)
        val onUserClick = View.OnClickListener {
            val position = holder.adapterPosition
            val comment = comments[position]
            UserHomePageActivity.actionStart(activity, avatar, comment.userId, comment.nickname, comment.avatar, comment.bgImage)
        }
        avatar.setOnClickListener(onUserClick)
        nickname.setOnClickListener(onUserClick)
        goodImage.setOnClickListener {
            val position = holder.adapterPosition
            val comment = comments[position]
            var goodsCount = comment.goodsCount
            if (comment.isGoodAlready) {
                comment.isGoodAlready = false
                goodsCount--
                if (goodsCount < 0) {
                    goodsCount = 0
                }
                comment.goodsCount = goodsCount
            } else {
                comment.isGoodAlready = true
                comment.goodsCount = ++goodsCount
            }
            notifyItemChanged(position)
            GoodComment.getResponse(comment.commentId, null)
        }
        return holder
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val comment = comments[position]
        val avatar = comment.avatar
        Glide.with(activity)
                .load(CustomUrl(avatar))
                .bitmapTransform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.avatar)
        holder.nickname.text = comment.nickname
        holder.content.text = comment.content
        holder.postDate.text = DateUtil.getConvertedDate(comment.postDate)
        if (comment.goodsCount > 0) {
            holder.goodCount.visibility = View.VISIBLE
            holder.goodCount.text = GlobalUtil.getConvertedNumber(comment.goodsCount)
        } else {
            holder.goodCount.visibility = View.INVISIBLE
        }
        if (comment.isGoodAlready) {
            holder.goodImage.setImageResource(R.drawable.ic_gooded)
        } else {
            holder.goodImage.setImageResource(R.drawable.ic_good)
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

}