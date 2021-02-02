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

package com.quxianggif.user.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quxianggif.R
import com.quxianggif.common.callback.InfiniteScrollListener
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.common.ui.BaseFragment
import com.quxianggif.common.view.SimpleDividerDecoration
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.dp2px
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.User
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.network.model.GetFollowshipBase
import com.quxianggif.network.model.Response
import com.quxianggif.user.adapter.FollowshipAdapter
import com.quxianggif.util.ResponseHandler
import java.util.*

/**
 * 用户个人主页关注和粉丝列表的Fragment基类。
 *
 * @author guolin
 * @since 17/7/30
 */
abstract class BaseFollowshipFragment : BaseFragment(), LoadDataListener {

    lateinit var activity: FollowshipActivity

    lateinit var recyclerView: RecyclerView

    internal lateinit var adapter: RecyclerView.Adapter<*>

    internal lateinit var layoutManager: LinearLayoutManager

    /**
     * RecyclerView的数据源。
     */
    private var userList: MutableList<User> = ArrayList()

    /**
     * 判断是否正在加载更多数据。
     */
    internal var isLoadingMore = false

    var isLoadFailed = false

    /**
     * 判断是否还有更多数据。
     * @return 当服务器端没有更多数据时，此值为true，否则此值为false。
     */
    var isNoMoreData = false
        internal set

    private var page = 0

    /**
     * 判断当前是否是已登录用户本人的关注粉丝界面。
     * @return 如果是本人的关注粉丝返回true，否则返回false。
     */
    private val isCurrentUserFollowship: Boolean
        get() = activity.mUserId == GifFun.getUserId()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_followship, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        return super.onCreateView(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as FollowshipActivity
        setupRecyclerView()
        // setup configurations and events
        recyclerView.addOnScrollListener(object : InfiniteScrollListener(layoutManager) {
            override fun onLoadMore() {
                onLoad()
            }

            override fun isDataLoading() = isLoadingMore

            override fun isNoMoreData() = isNoMoreData

        })
        loadFollowships(page)
    }

    override fun onLoad() {
        if (!isLoadingMore) {
            isLoadFailed = false
            isNoMoreData = false
            isLoadingMore = true
            page++
            loadFollowships(page)
        }
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(activity)
        adapter = FollowshipAdapter(this, userList, layoutManager)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SimpleDividerDecoration(activity, dp2px(65f)))
    }

    /**
     *
     * 处理获取关注或粉丝列表请求的返回结果。
     *
     * @param response
     * 服务器响应的获取关注或粉丝请求的实体类。
     */
    internal fun handleFetchedFollowships(response: Response) {
        isNoMoreData = false
        if (!ResponseHandler.handleResponse(response)) {
            val getFollowshipBase = response as GetFollowshipBase
            val status = getFollowshipBase.status
            when (status) {
                0 -> {
                    recyclerView.stopScroll()
                    val users = getFollowshipBase.users
                    userList.addAll(users)
                    adapter.notifyDataSetChanged()
                    loadFinished()
                }
                10004 -> {
                    isNoMoreData = true
                    adapter.notifyItemChanged(adapter.itemCount - 1)
                    loadFinished()
                }
                else -> {
                    logWarn(TAG, "Fetch followships failed. " + GlobalUtil.getResponseClue(status, getFollowshipBase.msg))
                    showToast(GlobalUtil.getString(R.string.fetch_data_failed))
                    loadFailed(GlobalUtil.getString(R.string.fetch_data_failed) + ": " + response.status)
                }
            }
        } else {
            loadFailed(GlobalUtil.getString(R.string.unknown_error) + ": " + response.status)
        }
    }

    /**
     * 加载完成，将数据显示出来，将加载等待控件隐藏。
     */
    override fun loadFinished() {
        super.loadFinished()
        isLoadFailed = false
        if (userList.isEmpty()) {
            recyclerView.visibility = View.GONE
            if (this is FollowingsFragment) {
                if (isCurrentUserFollowship) {
                    showNoContentView(GlobalUtil.getString(R.string.you_follows_no_one))
                } else {
                    showNoContentView(GlobalUtil.getString(R.string.he_follows_no_one))
                }
            } else if (this is FollowersFragment) {
                if (isCurrentUserFollowship) {
                    showNoContentView(GlobalUtil.getString(R.string.no_one_follows_you))
                } else {
                    showNoContentView(GlobalUtil.getString(R.string.no_one_follows_him))
                }
            }
        } else {
            recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * 加载失败，将加载等待控件隐藏。
     */
    override fun loadFailed(msg: String?) {
        super.loadFailed(msg)
        isLoadFailed = true
        if (userList.isEmpty()) {
            if (TextUtils.isEmpty(msg)) {
                val refresh = Runnable {
                    startLoading()
                    loadFollowships(page)
                }
                showBadNetworkView(View.OnClickListener { GifFun.getHandler().postDelayed(refresh, 300) })
            } else {
                showLoadErrorView(msg!!)
            }
        } else {
            adapter.notifyItemChanged(adapter.itemCount - 1)
        }
    }

    internal abstract fun loadFollowships(page: Int)

    companion object {

        private const val TAG = "BaseFollowshipFragment"
    }

}
