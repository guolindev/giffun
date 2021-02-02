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

package com.quxianggif.comments.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.comments.adapter.CommentsAdapter
import com.quxianggif.common.callback.InfiniteScrollListener
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.common.view.SimpleDividerDecoration
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.dp2px
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.postDelayed
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.Comment
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.DeleteCommentEvent
import com.quxianggif.event.MessageEvent
import com.quxianggif.event.PostCommentEvent
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.LoadComments
import com.quxianggif.network.model.PostComment
import com.quxianggif.network.model.Response
import com.quxianggif.util.DateUtil
import com.quxianggif.util.ResponseHandler
import com.quxianggif.util.UserUtil
import com.quxianggif.util.glide.CustomUrl
import com.umeng.analytics.MobclickAgent
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_comments.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 评论界面。用于显示一条Feed下的所有评论内容。
 *
 * @author davy, guolin
 * @since 17/7/17
 */
class CommentsActivity : BaseActivity(), LoadDataListener {

    private lateinit var adapter: CommentsAdapter

    private lateinit var layoutManager: LinearLayoutManager

    private var mFeedId: Long = 0

    /**
     * RecyclerView的数据源，用于存储所有展示中的评论。
     */
    private val commentList = ArrayList<Comment>()

    /**
     * 判断是否正在加载更多评论。
     */
    private var isLoading = false

    var isLoadFailed = false

    /**
     * 判断是否还有更多数据。
     *
     * @return 当服务器端没有更多数据时，此值为true，否则此值为false。
     */
    var isNoMoreData = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
    }

    override fun setupViews() {
        super.setupViews()
        mFeedId = intent.getLongExtra(FEED_ID, 0)
        if (mFeedId <= 0) {
            showToast(GlobalUtil.getString(R.string.fetch_feed_details_failed))
            finish()
            return
        }
        setupToolbar()
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(SimpleDividerDecoration(this, dp2px(65f)))
        adapter = CommentsAdapter(this@CommentsActivity, commentList, layoutManager, mFeedId)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    postCommentLayout.requestFocus()
                    hideSoftKeyboard()
                }
            }
        })
        recyclerView.addOnScrollListener(object : InfiniteScrollListener(layoutManager) {

            override fun onLoadMore() {
                loadComments()
            }

            override fun isDataLoading() = isLoading

            override fun isNoMoreData() = isNoMoreData

        })

        Glide.with(activity)
                .load(CustomUrl(UserUtil.avatar))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .into(avatarMe)

        postComment.setOnClickListener {
            val content = commentEdit.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                showToast(GlobalUtil.getString(R.string.please_enter_comment))
            } else {
                postComment(content)
                postComment.isEnabled = false
                commentEdit.isEnabled = false
                hideSoftKeyboard()
                postCommentLayout.requestFocus()
            }
        }
        loadComments()
    }

    override fun onLoad() {
        loadComments()
    }

    /**
     * 加载评论内容。
     */
    fun loadComments() {
        isLoadFailed = false
        isNoMoreData = false
        isLoading = true
        var lastComment: Long = 0
        if (!commentList.isEmpty()) {
            lastComment = commentList[commentList.size - 1].commentId
        }
        LoadComments.getResponse(mFeedId, lastComment, object : Callback {
            override fun onResponse(response: Response) {
                if (activity == null) {
                    return
                }
                isLoading = false
                handleFetchedComments(response)
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                isLoading = false
                if (lastComment == 0L) {
                    ResponseHandler.handleFailure(e)
                }
                loadFailed(null)
            }
        })
    }

    /**
     * 发布一条评论。
     *
     * @param content 评论的具体内容
     */
    private fun postComment(content: String) {
        PostComment.getResponse(mFeedId, content, object : Callback {
            override fun onResponse(response: Response) {
                if (!ResponseHandler.handleResponse(response)) {
                    val status = response.status
                    if (status == 0) {
                        val position = layoutManager.findFirstVisibleItemPosition()
                        val postComment = response as PostComment
                        showToast(GlobalUtil.getString(R.string.post_comment_success))
                        val comment = Comment()
                        comment.commentId = postComment.commentId
                        comment.nickname = UserUtil.nickname
                        comment.avatar = UserUtil.avatar
                        comment.content = content
                        comment.postDate = System.currentTimeMillis()
                        comment.userId = GifFun.getUserId()
                        commentList.add(0, comment)
                        adapter.notifyItemInserted(0)
                        commentEdit.setText("")
                        loadFinished()
                        if (position == 0) { // 如果发评论时第一条评论可见，则将评论滚动到最新刚发出的一条
                            recyclerView.scrollToPosition(0)
                        }
                        val postCommentEvent = PostCommentEvent()
                        postCommentEvent.commentId = postComment.commentId
                        postCommentEvent.feedId = mFeedId
                        EventBus.getDefault().post(postCommentEvent)
                        MobclickAgent.onEvent(this@CommentsActivity, "10004")
                    } else if (status == 10402) {
                        val timeLeft = response.msg.toLong()
                        if (DateUtil.isBlockedForever(timeLeft)) {
                            showToast(GlobalUtil.getString(R.string.unable_to_post_comment_forever), Toast.LENGTH_LONG)
                        } else {
                            val tip = DateUtil.getTimeLeftTip(timeLeft)
                            showToast(String.format(GlobalUtil.getString(R.string.unable_to_post_comment), tip), Toast.LENGTH_LONG)
                        }
                    } else {
                        logWarn(TAG, "Post comment failed. " + GlobalUtil.getResponseClue(status, response.msg))
                        showToast(GlobalUtil.getString(R.string.post_comment_failed))
                    }
                }
                postComment.isEnabled = true
                commentEdit.isEnabled = true
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                ResponseHandler.handleFailure(e)
                postComment.isEnabled = true
                commentEdit.isEnabled = true
            }
        })
    }

    /**
     *
     * 处理获取评论请求的返回结果。
     *
     * @param response
     * 服务器响应的获取评论请求的实体类。
     */
    private fun handleFetchedComments(response: Response) {
        isNoMoreData = false
        if (!ResponseHandler.handleResponse(response)) {
            val loadComments = response as LoadComments
            val status = loadComments.status
            when (status) {
                0 -> {
                    val comments = loadComments.comments
                    recyclerView.stopScroll()
                    commentList.addAll(comments)
                    adapter.notifyDataSetChanged()
                    loadFinished()
                }
                10004 -> {
                    isNoMoreData = true
                    adapter.notifyItemChanged(adapter.dataItemCount)
                    loadFinished()
                }
                else -> {
                    logWarn(TAG, "Load comments failed. " + GlobalUtil.getResponseClue(status, loadComments.msg))
                    showToast(GlobalUtil.getString(R.string.load_comments_failed))
                    loadFailed(GlobalUtil.getString(R.string.load_comments_failed) + ": " + response.status)
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
        if (commentList.isEmpty()) {
            recyclerView.visibility = View.GONE
            showNoContentView(GlobalUtil.getString(R.string.no_comment))
        } else {
            recyclerView.visibility = View.VISIBLE
            hideNoContentView()
        }
        postCommentLayout.visibility = View.VISIBLE
    }

    /**
     * 加载评论失败，将加载等待控件隐藏。
     */
    override fun loadFailed(msg: String?) {
        super.loadFailed(msg)
        isLoadFailed = true
        if (commentList.isEmpty()) {
            postCommentLayout.visibility = View.GONE
            if (msg == null) {
                showBadNetworkView(View.OnClickListener {
                    GifFun.getHandler().postDelayed(300) {
                        startLoading()
                        loadComments()
                    }
                })
            } else {
                showLoadErrorView(msg)
            }
        } else {
            adapter.notifyItemChanged(adapter.itemCount - 1)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is DeleteCommentEvent) {
            if (commentList.isEmpty()) {
                loadFinished()
            }
        } else {
            super.onMessageEvent(messageEvent)
        }
    }

    companion object {

        private const val TAG = "CommentsActivity"

        const val FEED_ID = "feed_id"

        fun actionStart(activity: Activity, feedId: Long) {
            val intent = Intent(activity, CommentsActivity::class.java)
            intent.putExtra(FEED_ID, feedId)
            activity.startActivity(intent)
        }
    }

}