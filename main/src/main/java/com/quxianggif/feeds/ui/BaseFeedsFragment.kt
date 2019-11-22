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

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import com.quxianggif.R
import com.quxianggif.common.callback.InfiniteScrollListener
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.common.callback.PendingRunnable
import com.quxianggif.common.ui.BaseFragment
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.postDelayed
import com.quxianggif.event.CleanCacheEvent
import com.quxianggif.event.MessageEvent
import com.quxianggif.event.RefreshMainActivityFeedsEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 应用程序中所有Feed相关Fragment的基类。
 *
 * @author guolin
 * @since 17/7/25
 */
abstract class BaseFeedsFragment : BaseFragment() {

    /**
     * 判断是否正在加载更多Feeds。
     */
    internal var isLoadingMore = false

    lateinit var activity: MainActivity

    lateinit var swipeRefresh: SwipeRefreshLayout

    lateinit var recyclerView: RecyclerView

    internal lateinit var adapter: RecyclerView.Adapter<*>

    internal lateinit var loadDataListener: LoadDataListener

    internal lateinit var layoutManager: RecyclerView.LayoutManager

    var pendingRunnable = SparseArray<PendingRunnable>()

    var isLoadFailed: Boolean = false

    /**
     * 判断是否还有更多数据，当服务器端没有更多Feeds时，此值为true。
     */
    /**
     * 判断是否还有更多数据。
     * @return 当服务器端没有更多Feeds时，此值为true，否则此值为false。
     */
    var isNoMoreData = false
        internal set

    internal fun initViews(rootView: View) {
        recyclerView = rootView.findViewById(R.id.recyclerView)
        swipeRefresh = rootView.findViewById(R.id.swipeRefresh)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadDataListener = this as LoadDataListener
        activity = getActivity() as MainActivity
        // setup configurations and events
        setupRecyclerView()
        swipeRefresh.setColorSchemeResources(R.color.colorAccent)
        recyclerView.addOnScrollListener(object: InfiniteScrollListener(layoutManager) {

            override fun onLoadMore() {
                loadDataListener.onLoad()

            }

            override fun isDataLoading() = isLoadingMore

            override fun isNoMoreData() = isNoMoreData

        })
        swipeRefresh.setOnRefreshListener { refreshFeeds() }
        loadFeedsFromDB()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is RefreshMainActivityFeedsEvent) {
            if (isLoadFailed) { // 只要当加载失败的情况下，收到RefreshMainActivityFeedsEvent才会执行刷新，否则不进行刷新
                GifFun.getHandler().postDelayed(300) { // 略微进行延迟处理，使界面上可以看到波纹动画效果
                    reloadFeeds()
                }
            }
        } else if (messageEvent is CleanCacheEvent) {
            reloadFeeds()
        }
    }

    /**
     * 加载feeds完成，将feeds显示出来，将加载等待控件隐藏。
     */
    override fun loadFinished() {
        super.loadFinished()
        isLoadFailed = false
        recyclerView.visibility = View.VISIBLE
        swipeRefresh.visibility = View.VISIBLE
        if (swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = false
        }
    }

    /**
     * 加载feeds失败，将加载等待控件隐藏。
     */
    override fun loadFailed(msg: String?) {
        super.loadFailed(msg)
        isLoadFailed = true
        swipeRefresh.isRefreshing = false
        if (dataSetSize() == 0) {
            if (msg == null) {
                swipeRefresh.visibility = View.GONE
                showBadNetworkView(View.OnClickListener {
                    val event = RefreshMainActivityFeedsEvent()
                    EventBus.getDefault().post(event)
                })
            } else {
                showLoadErrorView(msg)
            }
        } else {
            adapter.notifyItemChanged(adapter.itemCount - 1)
        }
    }

    /**
     * 重新加载feeds，在加载过程中如果界面上没有元素则显示ProgressBar，如果界面上已经有元素则显示SwipeRefresh。
     */
    private fun reloadFeeds() {
        if (adapter.itemCount <= 1) {
            startLoading()
        } else {
            swipeRefresh.isRefreshing = true
        }
        refreshFeeds()
    }

    /**
     * 执行潜在的Pending任务。
     */
    fun executePendingRunnableList() {
        val size = pendingRunnable.size()
        if (size > 0) {
            for (i in 0 until size) {
                val index = pendingRunnable.keyAt(i)
                val runnable = pendingRunnable.get(index)
                runnable.run(index)
            }
            pendingRunnable.clear()
        }
    }

    /**
     * 将RecyclerView滚动到顶部
     */
    fun scrollToTop() {
        if (adapter.itemCount != 0) {
            recyclerView.smoothScrollToPosition(0)
        }
    }

    internal abstract fun setupRecyclerView()

    internal abstract fun loadFeeds(lastFeed: Long)

    internal abstract fun refreshFeeds()

    internal abstract fun loadFeedsFromDB()

    internal abstract fun dataSetSize(): Int

    companion object {

        private const val TAG = "BaseFeedsFragment"
    }

}
