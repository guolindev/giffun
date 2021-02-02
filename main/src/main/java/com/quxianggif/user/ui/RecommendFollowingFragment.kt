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
import androidx.recyclerview.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quxianggif.R
import com.quxianggif.common.ui.BaseFragment
import com.quxianggif.common.view.SimpleDividerDecoration
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.*
import com.quxianggif.core.model.User
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.FollowUserEvent
import com.quxianggif.event.MessageEvent
import com.quxianggif.event.RefreshFollowingFeedsEvent
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.FollowUser
import com.quxianggif.network.model.GetRecommendFollowing
import com.quxianggif.network.model.Response
import com.quxianggif.user.adapter.RecommendFollowingAdapter
import com.quxianggif.util.ResponseHandler
import kotlinx.android.synthetic.main.fragment_recommend_following.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 系统推荐关注用户列表的Fragment。
 *
 * @author guolin
 * @since 18/3/19
 */
class RecommendFollowingFragment : BaseFragment() {

    internal lateinit var adapter: RecyclerView.Adapter<*>

    internal lateinit var layoutManager: LinearLayoutManager

    internal lateinit var activity: RecommendFollowingActivity

    /**
     * RecyclerView的数据源。
     */
    internal var userList: MutableList<User> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recommend_following, container, false)
        EventBus.getDefault().register(this)
        return super.onCreateView(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as RecommendFollowingActivity
        setupRecyclerView()
        loadRecommendFollowing()
        followAll.setOnClickListener { followAll() }
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(activity)
        adapter = RecommendFollowingAdapter(activity, userList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SimpleDividerDecoration(activity, dp2px(66f)))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        for (user in userList) {
            if (user.isFollowing) {
                val event = RefreshFollowingFeedsEvent()
                EventBus.getDefault().post(event)
                break
            }
        }
    }

    /**
     * 加载系统推荐关注的数据。
     */
    private fun loadRecommendFollowing() {
        startLoading()
        GetRecommendFollowing.getResponse(object : Callback {
            override fun onResponse(response: Response) {
                if (!ResponseHandler.handleResponse(response)) {
                    val getRecommendFollowing = response as GetRecommendFollowing
                    val status = getRecommendFollowing.status
                    when (status) {
                        0 -> {
                            val users = getRecommendFollowing.users
                            val oldFeedsCount = userList.size
                            userList.addAll(users)
                            adapter.notifyItemRangeInserted(oldFeedsCount, users.size)
                            loadFinished()
                        }
                        10004 -> {
                            recommendContent.visibility = View.GONE
                            loading?.visibility = View.GONE
                            showNoContentView(GlobalUtil.getString(R.string.no_more_recommend))
                        }
                        else -> {
                            logWarn(TAG, "Fetch recommend following failed. " + GlobalUtil.getResponseClue(status, getRecommendFollowing.msg))
                            showToast(GlobalUtil.getString(R.string.fetch_data_failed))
                            loadFailed(GlobalUtil.getString(R.string.fetch_data_failed) + ": " + response.status)
                        }
                    }
                } else {
                    loadFailed(GlobalUtil.getString(R.string.unknown_error) + ": " + response.status)
                }
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                loadFailed(null)
                ResponseHandler.handleFailure(e)
            }
        })
    }

    /**
     * 一键关注所有系统推荐的用户。
     */
    private fun followAll() {
        val followList = ArrayList<User>()
        for (user in userList) {
            if (!user.isFollowing) {
                followList.add(user)
            }
        }
        if (followList.isEmpty()) {
            return
        }
        refreshFollowAllStatus(true)
        val followUserIds = LongArray(followList.size)
        for (i in followList.indices) {
            followUserIds[i] = followList[i].userId
        }
        FollowUser.getResponse(followUserIds, object : Callback {
            override fun onResponse(response: Response) {
                val status = response.status
                if (status == 0) {
                    for (user in userList) {
                        user.isFollowing = true
                    }
                    adapter.notifyDataSetChanged()
                    showToast(GlobalUtil.getString(R.string.follow_all_success))
                } else {
                    if (status == 10208) {
                        showToast(GlobalUtil.getString(R.string.follow_too_many))
                    } else {
                        showToast(GlobalUtil.getString(R.string.follow_failed))
                    }
                }
                refreshFollowAllStatus(false)
            }

            override fun onFailure(e: Exception) {
                showToast(GlobalUtil.getString(R.string.follow_failed))
                refreshFollowAllStatus(false)
            }
        })
    }

    override fun loadFinished() {
        super.loadFinished()
        recommendContent.visibility = View.VISIBLE
    }

    override fun loadFailed(msg: String?) {
        super.loadFailed(msg)
        recommendContent.visibility = View.GONE
        if (msg == null) {
            showBadNetworkView(View.OnClickListener {
                GifFun.getHandler().postDelayed(300) {
                    loadRecommendFollowing()
                }
            })
        } else {
            showLoadErrorView(msg)
        }
    }

    /**
     * 根据全部关注的状态来刷新当前界面。如果正在进行全部关注操作，是不能再点击任何其他关注按钮的。
     * @param isFollowingAll
     * 是否正在进行全部关注操作。
     */
    private fun refreshFollowAllStatus(isFollowingAll: Boolean) {
        if (isFollowingAll) {
            followAll.visibility = View.GONE
            followAllLoading.visibility = View.VISIBLE
            blockRecyclerView.visibility = View.VISIBLE
        } else {
            followAll.visibility = View.VISIBLE
            followAllLoading.visibility = View.GONE
            blockRecyclerView.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event is FollowUserEvent) {
            val userId = event.userId
            searchModelIndex(userList, userId) { index ->
                val user = userList[index]
                user.isFollowing = event.type == FollowUserEvent.FOLLOW_USER
                adapter.notifyItemChanged(index)
            }
        }
    }

    companion object {

        private const val TAG = "RecommendFollowingFragment"
    }

}