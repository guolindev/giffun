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

package com.quxianggif.user.adapter

import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.common.holder.LoadingMoreViewHolder
import com.quxianggif.core.model.User
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.user.ui.BaseFollowshipFragment
import com.quxianggif.user.ui.FollowshipActivity
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.glide.CustomUrl

import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * 关注模块的RecyclerView适配器，用于在界面上展示关注的数据。
 *
 * @author guolin
 * @since 17/7/25
 */
class FollowshipAdapter(private val fragment: BaseFollowshipFragment, private val userList: List<User>,
                        private val layoutManager: RecyclerView.LayoutManager?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val activity: FollowshipActivity = fragment.activity

    private var isLoadFailed: Boolean = false
        get() = fragment.isLoadFailed

    private var isNoMoreData: Boolean = false
        get() = fragment.isNoMoreData

    /**
     * 获取RecyclerView数据源中元素的数量。
     * @return RecyclerView数据源中元素的数量。
     */
    private val dataItemCount: Int
        get() = userList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_USERS -> return createFollowshipHolder(parent)
            TYPE_LOADING_MORE -> return createLoadingMoreHolder(parent)
        }
        throw IllegalArgumentException()
    }

    private fun createFollowshipHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.followship_item, parent, false)
        val holder = FollowshipViewHolder(view)
        holder.rootLayout.setOnClickListener {
            val position = holder.adapterPosition
            val user = userList[position]
            UserHomePageActivity.actionStart(activity, holder.avatar, user.userId, user.nickname, user.avatar, user.bgImage)
        }
        return holder
    }

    private fun createLoadingMoreHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val holder = LoadingMoreViewHolder.createLoadingMoreViewHolder(activity, parent)
        holder.failed.setOnClickListener {
            fragment.onLoad()
            notifyItemChanged(itemCount - 1)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_USERS -> bindFollowshipHolder(holder as FollowshipViewHolder, position)
            TYPE_LOADING_MORE -> bindLoadingMoreHolder(holder as LoadingMoreViewHolder)
        }
    }

    private fun bindFollowshipHolder(holder: FollowshipViewHolder, position: Int) {
        val user = userList[position]
        holder.nickname.text = user.nickname
        if (TextUtils.isEmpty(user.description)) {
            holder.description.text = GlobalUtil.getString(R.string.user_does_not_feed_anything)
        } else {
            holder.description.text = user.description
        }

        Glide.with(activity)
                .load(CustomUrl(user.avatar))
                .bitmapTransform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.avatar)

        if (layoutManager != null) {
            val visibleItemCount = layoutManager.childCount
            if (visibleItemCount >= dataItemCount - 1) {
                fragment.onLoad()
            }
        }
    }

    private fun bindLoadingMoreHolder(holder: LoadingMoreViewHolder) {
        when {
            isNoMoreData -> {
                holder.progress.visibility = View.GONE
                holder.failed.visibility = View.GONE
                holder.end.visibility = View.VISIBLE
            }
            isLoadFailed -> {
                holder.progress.visibility = View.GONE
                holder.failed.visibility = View.VISIBLE
                holder.end.visibility = View.GONE
            }
            else -> {
                holder.progress.visibility = View.VISIBLE
                holder.failed.visibility = View.GONE
                holder.end.visibility = View.GONE
            }
        }
    }

    /**
     * 元素的总数是List的数量+1（1是底部的加载更多视图）。
     */
    override fun getItemCount(): Int {
        return dataItemCount + 1
    }

    /**
     * 根据位置返回不同的view type。
     */
    override fun getItemViewType(position: Int): Int {
        return if (position < dataItemCount && dataItemCount > 0) {
            TYPE_USERS
        } else TYPE_LOADING_MORE
    }

    private class FollowshipViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        val rootLayout: LinearLayout = view as LinearLayout

        val avatar: ImageView = view.findViewById(R.id.avatar)

        val nickname: TextView = view.findViewById(R.id.nickname)

        val description: TextView = view.findViewById(R.id.description)

    }

    companion object {

        private const val TAG = "FollowshipAdapter"

        private const val TYPE_USERS = 0

        private const val TYPE_LOADING_MORE = 1
    }

}
