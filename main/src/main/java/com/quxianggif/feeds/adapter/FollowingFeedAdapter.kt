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

package com.quxianggif.feeds.adapter

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quxianggif.R
import com.quxianggif.common.adapter.SimpleListFeedAdapter
import com.quxianggif.core.model.FollowingFeed
import com.quxianggif.feeds.ui.FollowingFeedsFragment
import com.quxianggif.user.ui.UserHomePageActivity

/**
 * 关注模块的RecyclerView适配器，用于在界面上展示关注的数据。
 *
 * @author guolin
 * @since 17/7/25
 */
class FollowingFeedAdapter(private val fragment: FollowingFeedsFragment, feedList: MutableList<FollowingFeed>,
                           maxImageWidth: Int, layoutManager: RecyclerView.LayoutManager) : SimpleListFeedAdapter<FollowingFeed, Activity>(fragment.activity, feedList, maxImageWidth, layoutManager) {
    override var isLoadFailed: Boolean = false
        get() = fragment.isLoadFailed

    override var isNoMoreData: Boolean = false
        get() = fragment.isNoMoreData


    override fun createFeedHolder(parent: ViewGroup): SimpleListFeedAdapter.FeedViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.user_feed_item, parent, false)
        val holder = SimpleListFeedAdapter.FeedViewHolder(view)
        initBaseFeedHolder(holder)
        setupUserClick(holder)
        return holder
    }

    override fun createRefeedHolder(parent: ViewGroup): SimpleListFeedAdapter.RefeedViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.user_refeed_item, parent, false)
        val holder = SimpleListFeedAdapter.RefeedViewHolder(view)
        initBaseFeedHolder(holder)
        setupUserClick(holder)
        return holder
    }

    private fun setupUserClick(holder: SimpleListFeedAdapter.SimpleListFeedViewHolder) {
        val onUserClick = View.OnClickListener {
            val position = holder.adapterPosition
            val feed = feedList[position]
            UserHomePageActivity.actionStart(activity, holder.avatar, feed.userId, feed.nickname, feed.avatar, feed.bgImage)
        }
        holder.avatar.setOnClickListener(onUserClick)
        holder.nickname.setOnClickListener(onUserClick)
        holder.postDate.setOnClickListener(onUserClick)
    }

    override fun onLoad() {
        fragment.onLoad()
    }

}
