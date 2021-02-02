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

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.User
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.FollowUser
import com.quxianggif.network.model.Response
import com.quxianggif.network.model.UnfollowUser
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.glide.CustomUrl

import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * 系统推荐关注用户列表的RecyclerView适配器。
 *
 * @author guolin
 * @since 18/3/20
 */
class RecommendFollowingAdapter(private val activity: Activity, private val userList: List<User>) : RecyclerView.Adapter<RecommendFollowingAdapter.RecommendFollowingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendFollowingViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.recommend_following_item, parent, false)
        val holder = RecommendFollowingViewHolder(view)
        holder.rootLayout.setOnClickListener {
            val position = holder.adapterPosition
            val user = userList[position]
            UserHomePageActivity.actionStart(activity, holder.avatar, user.userId, user.nickname, user.avatar, user.bgImage)
        }
        holder.followsButton.setOnClickListener {
            val position = holder.adapterPosition
            val user = userList[position]
            if (user.isFollowing) {
                user.isFollowing = false
                unfollowUser(position)
            } else {
                user.isFollowing = true
                followUser(position)
            }
            notifyItemChanged(position)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecommendFollowingViewHolder, position: Int) {
        val user = userList[position]
        holder.nickname.text = user.nickname
        if (TextUtils.isEmpty(user.description)) {
            holder.description.text = GlobalUtil.getString(R.string.user_does_not_feed_anything)
        } else {
            holder.description.text = user.description
        }

        holder.feedsAndFollowersCount.text = String.format(GlobalUtil.getString(R.string.feeds_and_followers_count), GlobalUtil.getConvertedNumber(user.feedsCount), GlobalUtil.getConvertedNumber(user.followersCount))
        if (user.isFollowing) {
            holder.followsButton.setBackgroundResource(R.drawable.followed_button_bg)
        } else {
            holder.followsButton.setBackgroundResource(R.drawable.follow_button_bg)
        }
        if (user.avatar.isBlank()) {
            Glide.with(activity)
                 .load(R.drawable.avatar_default)
                 .bitmapTransform(CropCircleTransformation(activity))
                 .placeholder(R.drawable.loading_bg_circle)
                 .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                 .into(holder.avatar)
        } else {
            Glide.with(activity)
                 .load(CustomUrl(user.avatar))
                 .bitmapTransform(CropCircleTransformation(activity))
                 .placeholder(R.drawable.loading_bg_circle)
                 .error(R.drawable.avatar_default)
                 .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                 .into(holder.avatar)
        }
    }

    /**
     * 元素的总数是List的数量。
     */
    override fun getItemCount(): Int {
        return userList.size
    }

    @Synchronized
    private fun followUser(position: Int) {
        val user = userList[position]
        FollowUser.getResponse(user.userId, object : Callback {
            override fun onResponse(response: Response) {
                val status = response.status
                if (status != 0) {
                    user.isFollowing = false
                    notifyItemChanged(position)
                    if (status == 10208) {
                        showToast(GlobalUtil.getString(R.string.follow_too_many))
                    } else {
                        showToast(GlobalUtil.getString(R.string.follow_failed))
                    }
                }
            }

            override fun onFailure(e: Exception) {
                userList[position].isFollowing = false
                notifyItemChanged(position)
                showToast(GlobalUtil.getString(R.string.follow_failed))
            }
        })
    }

    @Synchronized
    private fun unfollowUser(position: Int) {
        val user = userList[position]
        UnfollowUser.getResponse(user.userId, object : Callback {
            override fun onResponse(response: Response) {
                if (response.status != 0) {
                    user.isFollowing = true
                    notifyItemChanged(position)
                    showToast(GlobalUtil.getString(R.string.unfollow_failed))
                }
            }

            override fun onFailure(e: Exception) {
                userList[position].isFollowing = true
                notifyItemChanged(position)
                showToast(GlobalUtil.getString(R.string.unfollow_failed))
            }
        })
    }

    class RecommendFollowingViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var rootLayout: LinearLayout = view as LinearLayout

        var avatar: ImageView = view.findViewById(R.id.avatar)

        var nickname: TextView = view.findViewById(R.id.nickname)

        var description: TextView = view.findViewById(R.id.description)

        var feedsAndFollowersCount: TextView = view.findViewById(R.id.feedsAndFollowersCount)

        var followsButton: Button = view.findViewById(R.id.followsButton)

    }

    companion object {

        const val TAG = "RecommendFollowingAdapter"
    }

}