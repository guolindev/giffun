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
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.searchModelIndex
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.WorldFeed
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.DeleteFeedEvent
import com.quxianggif.event.LikeFeedEvent
import com.quxianggif.event.MessageEvent
import com.quxianggif.event.ModifyUserInfoEvent
import com.quxianggif.feeds.adapter.WorldFeedAdapter
import com.quxianggif.feeds.view.SpaceItemDecoration
import com.quxianggif.network.model.FetchWorldFeeds
import com.quxianggif.network.model.OriginThreadCallback
import com.quxianggif.network.model.Response
import com.quxianggif.util.ResponseHandler
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findAll
import java.util.*

/**
 * 展示世界频道的Feeds内容。
 *
 * @author guolin
 * @since 17/7/24
 */
class WorldFeedsFragment : WaterFallFeedsFragment(), LoadDataListener {

    /**
     * RecyclerView的数据源，用于存储所有展示中的Feeds。
     */
    internal var feedList: MutableList<WorldFeed> = ArrayList()

    override fun setupRecyclerView() {
        super.setupRecyclerView()
        adapter = WorldFeedAdapter(this, feedList, imageWidth, layoutManager)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(adapter))
    }

    /**
     * 加载feeds。如果数据库有缓存则优先显示缓存内存，如果没有缓存则从网络获取feeds。
     */
    override fun loadFeeds(lastFeed: Long) {
        val isRefreshing = lastFeed <= 0
        FetchWorldFeeds.getResponse(lastFeed, object : OriginThreadCallback {
            override fun onResponse(response: Response) {
                handleFetchedFeeds(response, isRefreshing)
                isLoadingMore = false
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                if (isRefreshing) {
                    ResponseHandler.handleFailure(e)
                }
                activity.runOnUiThread {
                    loadFailed(null)
                    isLoadingMore = false
                }
            }
        })
    }

    /**
     * 刷新feeds。
     */
    override fun refreshFeeds() {
        loadFeeds(0)
    }

    override fun loadFeedsFromDB() {
        Thread(Runnable {
            val feeds = LitePal.findAll<WorldFeed>()
            if (feeds.isEmpty()) {
                refreshFeeds()
            } else {
                activity.runOnUiThread {
                    feedList.clear()
                    feedList.addAll(feeds)
                    adapter.notifyDataSetChanged()
                    loadFinished()
                }
                if (activity.isNeedToRefresh) {
                    isLoadingMore = true // 此处将isLoadingMore设为true，防止因为内容不满一屏自动触发加载更多事件，从而让刷新进度条提前消失
                    activity.runOnUiThread { swipeRefresh.isRefreshing = true }
                    GifFun.getHandler().postDelayed({ refreshFeeds() }, 1000) // 为了能看到刷新进度条，让刷新事件延迟1.5秒执行
                }
            }
        }).start()
    }

    override fun dataSetSize(): Int {
        return feedList.size
    }

    override fun onLoad() {
        if (!isLoadingMore && feedList.isNotEmpty()) {
            isLoadingMore = true
            isLoadFailed = false
            isLoadingMore = true
            val lastFeed = feedList[feedList.size - 1].feedId
            loadFeeds(lastFeed)
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
            if (messageEvent.from == LikeFeedEvent.FROM_WORLD) {
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
            val fetchWorldFeeds = response as FetchWorldFeeds
            val status = fetchWorldFeeds.status
            if (status == 0) {
                val feeds = fetchWorldFeeds.feeds
                if (isRefreshing) {
                    LitePal.deleteAll<WorldFeed>()
                    LitePal.saveAll(feeds)
                    activity.runOnUiThread {
                        feedList.clear()
                        feedList.addAll(feeds)
                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(0)
                        loadFinished()
                    }
                } else {
                    val oldFeedsCount = feedList.size
                    LitePal.saveAll(feeds)
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
                logWarn(TAG, "Fetch feeds failed. ${GlobalUtil.getResponseClue(status, fetchWorldFeeds.msg)}")
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

        private const val TAG = "WorldFeedsFragment"
    }

}