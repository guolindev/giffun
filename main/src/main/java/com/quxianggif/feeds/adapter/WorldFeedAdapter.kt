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

import com.quxianggif.R
import com.quxianggif.common.adapter.WaterFallFeedAdapter
import com.quxianggif.core.model.WorldFeed
import com.quxianggif.feeds.ui.WorldFeedsFragment

/**
 * 世界模块的RecyclerView适配器，用于在界面上展示世界模块的数据，以及处理世界模块的相关功能。
 *
 * @author guolin
 * @since 17/5/27
 */
class WorldFeedAdapter(private val fragment: WorldFeedsFragment, feedList: List<WorldFeed>, imageWidth: Int,
                       layoutManager: RecyclerView.LayoutManager) : WaterFallFeedAdapter<WorldFeed>(fragment.activity, feedList, imageWidth, layoutManager) {

    override var isLoadFailed: Boolean = false
        get() = fragment.isLoadFailed

    override var isNoMoreData: Boolean = false
        get() = fragment.isNoMoreData

    override fun onLoad() {
        fragment.onLoad()
    }

    override fun createFeedHolder(parent: ViewGroup): WaterFallFeedAdapter.FeedViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.world_feed_item, parent, false)
        val holder = WorldFeedViewHolder(view)
        baseCreateFeedHolder(holder)
        return holder
    }

    override fun bindFeedHolder(holder: WaterFallFeedAdapter.FeedViewHolder, position: Int) {
        val viewHolder = holder as WorldFeedViewHolder
        baseBindFeedHolder(viewHolder, position)
    }

    private class WorldFeedViewHolder internal constructor(view: View) : WaterFallFeedAdapter.FeedViewHolder(view)

    companion object {

        private const val TAG = "WorldFeedAdapter"
    }

}