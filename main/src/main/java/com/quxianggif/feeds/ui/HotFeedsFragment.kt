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

package com.quxianggif.feeds.ui

import com.quxianggif.R
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.common.callback.PendingRunnable
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.searchModelIndex
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.HotFeed
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.*
import com.quxianggif.feeds.adapter.HotFeedAdapter
import com.quxianggif.feeds.view.SpaceItemDecoration
import com.quxianggif.network.model.FetchHotFeeds
import com.quxianggif.network.model.OriginThreadCallback
import com.quxianggif.network.model.Response
import com.quxianggif.util.ResponseHandler
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 展示热门频道的Feeds内容。
 *
 * @author guolin
 * @since 18/2/20
 */
class HotFeedsFragment : WaterFallFeedsFragment(), LoadDataListener {

    /**
     * RecyclerView的数据源，用于存储所有展示中的Feeds。
     */
    internal var feedList: MutableList<HotFeed> = ArrayList()

    override fun setupRecyclerView() {
        super.setupRecyclerView()
        adapter = HotFeedAdapter(this, feedList, imageWidth, layoutManager)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(adapter as HotFeedAdapter))
    }

    /**
     * 加载feeds。如果数据库有缓存则优先显示缓存内存，如果没有缓存则从网络获取feeds。
     */
    override fun loadFeeds(lastFeed: Long) {
        val isRefreshing = lastFeed == 0L /* lastFeed等于0表示刷新 */
        val callback = object : OriginThreadCallback {
            override fun onResponse(response: Response) {
                handleFetchedFeeds(response, isRefreshing)
                isLoadingMore = false
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                if (isRefreshing) {
                    ResponseHandler.handleFailure(e)
                }
                activity.runOnUiThread{
                    loadFailed(null)
                    isLoadingMore = false
                }
            }
        }
        if (lastFeed > 0) { // 此处lastFeed > 0表示加载更多热门feeds
            FetchHotFeeds.getLoadingMoreResponse(callback)
        } else { // lastFeed等于0表示刷新热门feeds
            FetchHotFeeds.getResponse(callback)
        }
    }

    /**
     * 刷新feeds。
     */
    override fun refreshFeeds() {
        loadFeeds(0)
    }

    override fun loadFeedsFromDB() {
        // 由于服务器接口设计原因，热门feed不能缓存到数据库中，因此这里直接刷新feeds列表
        refreshFeeds()
    }

    override fun dataSetSize(): Int {
        return feedList.size
    }

    override fun onLoad() {
        if (!isLoadingMore) {
            if (feedList.isNotEmpty()) {
                isLoadingMore = true
                isLoadFailed = false
                isLoadingMore = true
                loadFeeds(1) // 此处lastFeed恒定为1，表示加载更多。
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is DeleteFeedEvent) {
            val feedId = messageEvent.feedId
            searchModelIndex(feedList, feedId) { index ->
                feedList.removeAt(index)
                adapter.notifyItemRemoved(index)
            }
        } else if (messageEvent is LikeFeedEvent) {
            if (messageEvent.from == LikeFeedEvent.FROM_HOT) {
                return
            }
            val feedId = messageEvent.feedId
            searchModelIndex(feedList, feedId) { index ->
                // 对于Feed点赞状态同步要延迟执行，等到ViewPager切换到相应的界面时再执行，否则会出现状态同步的问题
                val runnable = object : PendingRunnable {
                    override fun run(index: Int) {
                        val feed = feedList[index]
                        feed.isLikedAlready = messageEvent.type == LikeFeedEvent.LIKE_FEED
                        feed.likesCount = messageEvent.likesCount
                        adapter.notifyItemChanged(index)
                    }
                }
                pendingRunnable.put(index, runnable)
            }
        } else if (messageEvent is ModifyUserInfoEvent) {
            if (messageEvent.modifyNickname || messageEvent.modifyAvatar) {
                swipeRefresh.isRefreshing = true
                refreshFeeds()
            }
        } else if (messageEvent is PostCommentEvent) {
            val feedId = messageEvent.feedId
            searchModelIndex(feedList, feedId) { index ->
                val hotFeed = feedList[index]
                val commentsCount = hotFeed.commentsCount
                hotFeed.commentsCount = commentsCount + 1
                adapter.notifyItemChanged(index)
            }
        } else if (messageEvent is DeleteCommentEvent) {
            val feedId = messageEvent.feedId
            searchModelIndex(feedList, feedId) { index ->
                val hotFeed = feedList[index]
                val commentsCount = hotFeed.commentsCount
                hotFeed.commentsCount = commentsCount - 1
                adapter.notifyItemChanged(index)
            }
        } else {
            super.onMessageEvent(messageEvent)
        }
    }

    /**
     *
     * 处理获取世界频道feeds请求的返回结果。
     *
     * @param response
     * 服务器响应的获取feeds请求的实体类。
     * @param isRefreshing
     * true表示刷新请求，false表示加载更多请求。
     */
    private fun handleFetchedFeeds(response: Response, isRefreshing: Boolean) {
        isNoMoreData = false
        if (!ResponseHandler.handleResponse(response)) {
            val fetchHotFeeds = response as FetchHotFeeds
            val status = fetchHotFeeds.status
            if (status == 0) {
                val feeds = fetchHotFeeds.feeds
                if (isRefreshing) {
                    activity.runOnUiThread {
                        feedList.clear()
                        feedList.addAll(feeds)
                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(0)
                        loadFinished()
                    }
                } else {
                    val oldFeedsCount = feedList.size
                    activity.runOnUiThread {
                        feedList.addAll(feeds)
                        recyclerView.stopScroll()
                        adapter.notifyItemRangeInserted(oldFeedsCount, feeds.size)
                        loadFinished()
                    }
                }
            } else if (status == 10004) {
                isNoMoreData = true
                activity.runOnUiThread {
                    adapter.notifyItemChanged(adapter.itemCount - 1)
                    loadFinished()
                }
            } else {
                logWarn(TAG, "Fetch feeds failed. " + GlobalUtil.getResponseClue(status, fetchHotFeeds.msg))
                activity.runOnUiThread {
                    showToast(GlobalUtil.getString(R.string.fetch_data_failed))
                    loadFailed(GlobalUtil.getString(R.string.fetch_data_failed) + ": " + response.status)
                }
            }
        } else {
            activity.runOnUiThread { loadFailed(GlobalUtil.getString(R.string.unknown_error) + ": " + response.status) }
        }
    }

    companion object {

        private const val TAG = "HotFeedsFragment"
    }

}