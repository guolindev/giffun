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

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.quxianggif.R
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.common.callback.PendingRunnable
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.dp2px
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.postDelayed
import com.quxianggif.core.extension.searchModelIndex
import com.quxianggif.core.model.FollowingFeed
import com.quxianggif.core.model.RefFeed
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.*
import com.quxianggif.feeds.adapter.FollowingFeedAdapter
import com.quxianggif.network.model.FetchFollowingFeeds
import com.quxianggif.network.model.OriginThreadCallback
import com.quxianggif.network.model.Response
import com.quxianggif.user.ui.RecommendFollowingActivity
import com.quxianggif.util.ResponseHandler
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findAll
import java.util.*
import kotlin.concurrent.thread

/**
 * 展示关注用户所发的Feeds。
 *
 * @author guolin
 * @since 17/7/24
 */
class FollowingFeedsFragment : BaseFeedsFragment(), LoadDataListener {

    /**
     * RecyclerView的数据源，用于存储所有展示中的Feeds。
     */
    internal var feedList: MutableList<FollowingFeed> = ArrayList()

    /**
     * 通过获取屏幕宽度来计算出每张图片最大的宽度。
     *
     * @return 计算后得出的每张图片最大的宽度。
     */
    private/* CardView margin *//* ImageView margin */ val maxImageWidth: Int
        get() {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay?.getMetrics(metrics)
            val columnWidth = metrics.widthPixels
            return columnWidth - dp2px((24 + 20).toFloat())
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_following_feeds, container, false)
        initViews(view)
        EventBus.getDefault().register(this)
        return super.onCreateView(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    override fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(activity)
        adapter = FollowingFeedAdapter(this, feedList, maxImageWidth, layoutManager)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun loadFeeds(lastFeed: Long) {
        val isRefreshing = lastFeed <= 0
        FetchFollowingFeeds.getResponse(lastFeed, object : OriginThreadCallback {
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

    override fun refreshFeeds() {
        loadFeeds(0)
    }

    override fun loadFeedsFromDB() {
        thread {
            val feeds = LitePal.findAll<FollowingFeed>(true)
            if (feeds.isEmpty()) {
                refreshFeeds()
            } else {
                activity.runOnUiThread {
                    feedList.clear()
                    feedList.addAll(feeds)
                    recyclerView.recycledViewPool.clear()
                    adapter.notifyDataSetChanged()
                    loadFinished()
                }
                if (activity.isNeedToRefresh) {
                    isLoadingMore = true // 此处将isLoadingMore设为true，防止因为内容不满一屏自动触发加载更多事件，从而让刷新进度条提前消失
                    activity.runOnUiThread { swipeRefresh.isRefreshing = true }
                    GifFun.getHandler().postDelayed(1000) { refreshFeeds() } // 为了能看到刷新进度条，让刷新事件延迟1.5秒执行
                }
            }
        }
    }

    override fun onLoad() {
        if (!isLoadingMore) {
            if (feedList.isNotEmpty()) {
                isLoadingMore = true
                isLoadFailed = false
                isNoMoreData = false
                val lastFeed = feedList[feedList.size - 1].feedId
                loadFeeds(lastFeed)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is DeleteFeedEvent) {
            val feedId = messageEvent.feedId
            if (feedList.isEmpty()) {
                loadFinished()
            }
            // 如果是从用户个人主页删除的Feed，则需要在关注Feed列表中找到同样的Feed并删除，但如果是在关注Feed列表中删除的Feed，就不需要再进行一这步操作了。
            if (messageEvent.type == DeleteFeedEvent.DELETE_FROM_USER_HOME_PAGE) {
                searchModelIndex(feedList, feedId) { index ->
                    feedList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    if (feedList.isEmpty()) {
                        loadFinished()
                    }
                }
            }
        } else if (messageEvent is LikeFeedEvent) {
            if (messageEvent.from == LikeFeedEvent.FROM_FOLLOWING) {
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
        } else if (messageEvent is RefreshFollowingFeedsEvent) {
            if (feedList.isEmpty()) {
                startLoading()
            } else {
                swipeRefresh.isRefreshing = true
            }
            refreshFeeds()
        } else {
            super.onMessageEvent(messageEvent)
        }
    }

    override fun dataSetSize(): Int {
        return feedList.size
    }

    /**
     *
     * 处理获取关注列表feeds请求的返回结果。
     *
     * @param response
     * 服务器响应的获取feeds请求的实体类。
     * @param isRefreshing
     * true表示刷新请求，false表示加载更多请求。
     */
    private fun handleFetchedFeeds(response: Response, isRefreshing: Boolean) {
        isNoMoreData = false
        if (!ResponseHandler.handleResponse(response)) {
            val fetchFollowingFeeds = response as FetchFollowingFeeds
            val status = fetchFollowingFeeds.status
            if (status == 0) {
                val feeds = fetchFollowingFeeds.feeds
                if (isRefreshing) {
                    LitePal.deleteAll<FollowingFeed>()
                    LitePal.deleteAll<RefFeed>()
                    saveCachedFeeds(feeds)
                    activity.runOnUiThread {
                        feedList.clear()
                        feedList.addAll(feeds)
                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(0)
                        loadFinished()
                    }
                } else {
                    saveCachedFeeds(feeds)
                    activity.runOnUiThread {
                        feedList.addAll(feeds)
                        recyclerView.stopScroll()
                        adapter.notifyDataSetChanged()
                        loadFinished()
                    }
                }
            } else if (status == 10004) {
                isNoMoreData = true
                activity.runOnUiThread {
                    if (isRefreshing) {
                        feedList.clear()
                    } else {
                        adapter.notifyItemChanged(adapter.itemCount - 1)
                    }
                    loadFinished()
                }
            } else {
                logWarn(TAG, "Fetch feeds failed. " + GlobalUtil.getResponseClue(status, fetchFollowingFeeds.msg))
                activity.runOnUiThread {
                    loadFailed(GlobalUtil.getString(R.string.fetch_data_failed) + ": " + response.status)
                }
            }
        } else {
            activity.runOnUiThread { loadFailed(GlobalUtil.getString(R.string.unknown_error) + ": " + response.status) }
        }
    }

    override fun loadFinished() {
        super.loadFinished()
        if (feedList.isEmpty()) {
            swipeRefresh.visibility = View.GONE
            recyclerView.visibility = View.GONE
            showNoContentViewWithButton(GlobalUtil.getString(R.string.your_following_feeds_is_empty),
                    GlobalUtil.getString(R.string.go_to_follow),
                    View.OnClickListener { RecommendFollowingActivity.actionStart(activity) })
        } else {
            hideNoContentView()
        }
    }

    private fun saveCachedFeeds(feeds: List<FollowingFeed>) {
        feeds.forEach {
            if (it.feedType == 1) {
                it.refFeed?.save()
            }
            it.save()
        }
        LitePal.saveAll(feeds)
    }

    companion object {

        private const val TAG = "FollowingFeedsFragment"
    }

}
