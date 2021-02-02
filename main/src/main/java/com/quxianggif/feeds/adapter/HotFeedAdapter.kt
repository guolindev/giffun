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

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.quxianggif.R
import com.quxianggif.common.adapter.WaterFallFeedAdapter
import com.quxianggif.core.model.HotFeed
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.comments.ui.CommentsActivity
import com.quxianggif.feeds.ui.HotFeedsFragment

/**
 * 热门Feed模块的RecyclerView适配器，用于在界面上展示热门Feed数据。
 *
 * @author guolin
 * @since 17/5/27
 */
class HotFeedAdapter(private val fragment: HotFeedsFragment, private val feedList: List<HotFeed>, imageWidth: Int,
                     layoutManager: RecyclerView.LayoutManager) : WaterFallFeedAdapter<HotFeed>(fragment.activity, feedList, imageWidth, layoutManager) {

    override var isLoadFailed: Boolean = false
        get() = fragment.isLoadFailed

    override var isNoMoreData: Boolean = false
        get() = fragment.isNoMoreData

    override fun onLoad() {
        fragment.onLoad()
    }

    override fun createFeedHolder(parent: ViewGroup): WaterFallFeedAdapter.FeedViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.hot_feed_item, parent, false)
        val holder = HotFeedViewHolder(view)
        holder.commentsLayout.setOnClickListener {
            val feedPosition = holder.adapterPosition
            val feed = feedList[feedPosition]
            CommentsActivity.actionStart(activity, feed.feedId)
        }
        baseCreateFeedHolder(holder)
        return holder
    }

    override fun bindFeedHolder(holder: WaterFallFeedAdapter.FeedViewHolder, position: Int) {
        val viewHolder = holder as HotFeedViewHolder
        val feed = feedList[position]
        viewHolder.commentsCount.text = GlobalUtil.getConvertedNumber(feed.commentsCount)
        baseBindFeedHolder(viewHolder, position)
    }

    private class HotFeedViewHolder internal constructor(view: View) : WaterFallFeedAdapter.FeedViewHolder(view) {

        val commentsCount: TextView = view.findViewById(R.id.commentsCount)

        val commentsLayout: LinearLayout = view.findViewById(R.id.commentsLayout)

    }

    companion object {

        private const val TAG = "HotFeedAdapter"
    }

}
