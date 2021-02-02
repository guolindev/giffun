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

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.quxianggif.R
import com.quxianggif.common.adapter.SimpleListFeedAdapter
import com.quxianggif.core.extension.dp2px
import com.quxianggif.core.model.UserFeed
import com.quxianggif.user.ui.UserHomePageActivity

/**
 * 用户个人主页的适配器。
 *
 * @author guolin
 * @since 17/7/23
 */
class UserFeedAdapter(override var activity: UserHomePageActivity, feedList: MutableList<UserFeed>,
                      maxImageWidth: Int, layoutManager: RecyclerView.LayoutManager) : SimpleListFeedAdapter<UserFeed, UserHomePageActivity>(activity, feedList, maxImageWidth, layoutManager) {
    override var isLoadFailed: Boolean = false
        get() = activity.isLoadFailed

    override var isNoMoreData: Boolean = false
        get() = activity.isNoMoreData

    override fun bindFeedHolder(holder: SimpleListFeedAdapter.FeedViewHolder, position: Int) {
        setupFirstItemMarginTop(holder.cardView, position)
        super.bindFeedHolder(holder, position)
    }

    override fun bindRefeedHolder(holder: SimpleListFeedAdapter.RefeedViewHolder, position: Int) {
        setupFirstItemMarginTop(holder.cardView, position)
        super.bindRefeedHolder(holder, position)
    }

    override fun createFeedHolder(parent: ViewGroup): SimpleListFeedAdapter.FeedViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.user_feed_item, parent, false)
        val holder = SimpleListFeedAdapter.FeedViewHolder(view)
        initBaseFeedHolder(holder)
        return holder
    }

    override fun createRefeedHolder(parent: ViewGroup): SimpleListFeedAdapter.RefeedViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.user_refeed_item, parent, false)
        val holder = SimpleListFeedAdapter.RefeedViewHolder(view)
        initBaseFeedHolder(holder)
        return holder
    }

    override fun onLoad() {
        activity.onLoad()
    }

    private fun setupFirstItemMarginTop(cardView: CardView, position: Int) {
        val params = if (position == 0) {
            val layoutParams = cardView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = dp2px(35f)
            layoutParams
        } else {
            val layoutParams = cardView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = dp2px(10f)
            layoutParams
        }
        cardView.layoutParams = params
    }

    companion object {

        private const val TAG = "UserFeedAdapter"
    }

}