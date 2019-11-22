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

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.comments.adapter.HotCommentsAdapter
import com.quxianggif.comments.ui.CommentsActivity
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.BaseFeed
import com.quxianggif.core.model.Comment
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.GoodCommentEvent
import com.quxianggif.event.PostCommentEvent
import com.quxianggif.feeds.ui.FeedDetailActivity
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.PostComment
import com.quxianggif.network.model.Response
import com.quxianggif.util.DateUtil
import com.quxianggif.util.ResponseHandler
import com.quxianggif.util.UserUtil
import com.quxianggif.util.glide.CustomUrl
import com.umeng.analytics.MobclickAgent
import jp.wasabeef.glide.transformations.CropCircleTransformation
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Feed详情界面的Adapter，这里用于展示除了GIF图之外的其他Feed相关信息，主要包括详情区、热评区和评论区三部分内容。
 *
 * @author guolin
 * @since 17/6/4
 */
class FeedDetailMoreAdapter(private val activity: FeedDetailActivity, private val detailInfo: View, private val feed: BaseFeed) : RecyclerView.Adapter<FeedDetailMoreAdapter.SimpleViewHolder>() {

    private var commentEdit: EditText? = null

    private var avatarMe: ImageView? = null

    private var postComment: Button? = null

    private var hotComments: MutableList<Comment>? = null

    private var hotCommentsAdapter: HotCommentsAdapter? = null

    /**
     * 热评的数量。初始数量为-1，表示正在加载中。-2表示热评加载失败。
     */
    private var commentCount = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        when (viewType) {
            DETAIL_INFO -> return SimpleViewHolder(detailInfo)
            LOADING_COMMENTS -> return createLoadingCommentsHolder(parent)
            HOT_COMMENTS -> return createHotCommentsHolder(parent)
            NO_COMMENT -> return createNoCommentHolder(parent)
            ENTER_COMMENT -> return createEnterCommentHolder(parent)
        }
        throw IllegalArgumentException()
    }

    private fun createLoadingCommentsHolder(parent: ViewGroup): SimpleViewHolder {
        val loadingView = LayoutInflater.from(activity).inflate(R.layout.loading_comments, parent, false)
        return SimpleViewHolder(loadingView)
    }

    private fun createHotCommentsHolder(parent: ViewGroup): SimpleViewHolder {
        val hotCommentsView = LayoutInflater.from(activity).inflate(R.layout.top_comments, parent, false)
        hotComments?.let { it ->
            val viewAllComments = hotCommentsView.findViewById<TextView>(R.id.viewAllComments)
            val recyclerView = hotCommentsView.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            hotCommentsAdapter = HotCommentsAdapter(activity, it)
            recyclerView.adapter = hotCommentsAdapter
            viewAllComments.setOnClickListener { _ -> CommentsActivity.actionStart(activity, feed.feedId) }
        }
        return SimpleViewHolder(hotCommentsView)
    }

    private fun createNoCommentHolder(parent: ViewGroup): SimpleViewHolder {
        val noCommentView = LayoutInflater.from(activity).inflate(R.layout.no_comment, parent, false)
        val noCommentText = noCommentView.findViewById<TextView>(R.id.noCommentText)
        if (commentCount == -2) {
            noCommentText.text = GlobalUtil.getString(R.string.your_network_bad_comment_load_failed)
        }
        return SimpleViewHolder(noCommentView)
    }

    private fun createEnterCommentHolder(parent: ViewGroup): SimpleViewHolder {
        val enterCommentView = LayoutInflater.from(activity).inflate(R.layout.enter_comment, parent, false)
        commentEdit = enterCommentView.findViewById(R.id.commentEdit)
        avatarMe = enterCommentView.findViewById(R.id.avatarMe)
        postComment = enterCommentView.findViewById(R.id.postComment)

        Glide.with(activity)
                .load(CustomUrl(UserUtil.avatar))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .into(avatarMe)
        postComment!!.setOnClickListener {
            val content = commentEdit!!.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                showToast(GlobalUtil.getString(R.string.please_enter_comment))
            } else {
                postComment(content)
                commentEdit!!.isEnabled = false
                postComment!!.isEnabled = false
                activity.hideSoftKeyboard()
                enterCommentView.requestFocus()
            }
        }
        return SimpleViewHolder(enterCommentView)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {}

    override fun getItemCount(): Int {
        return 3 // detail区 + 热评区 + 评论区
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> DETAIL_INFO
        1 -> if (commentCount == -1) {
            LOADING_COMMENTS
        } else if (commentCount == 0 || commentCount == -2) {
            NO_COMMENT
        } else {
            HOT_COMMENTS
        }
        2 -> ENTER_COMMENT
        else -> super.getItemViewType(position)
    }

    /**
     * 将加载出来的热评传入Adapter当中。
     * @param comments
     * 热评的List集合。
     */
    fun setHotComments(comments: MutableList<Comment>?) {
        if (comments == null) {
            commentCount = -2 // -2表示热评加载失败
        } else if (comments.isEmpty()) {
            commentCount = 0
        } else {
            commentCount = comments.size
            if (hotComments == null) {
                hotComments = comments
            } else {
                hotComments?.clear()
                hotComments?.addAll(comments)
            }
        }
        notifyDataSetChanged()
        hotCommentsAdapter?.notifyDataSetChanged()
    }

    /**
     * 刷新热门评论区别点赞的状态。
     * @param commentId
     * 评论的ID值
     * @param goodType
     * 点赞的类型，是点赞还是取消点赞
     * @param goodCount
     * 当前评论点赞的数量
     */
    fun refreshCommentsGood(commentId: Long, goodType: Int, goodCount: Int) {
        Thread(Runnable {
            if (hotCommentsAdapter != null && hotComments != null && !hotComments!!.isEmpty()) {
                for (i in hotComments!!.indices) {
                    val comment = hotComments!![i]
                    if (comment.commentId == commentId) {
                        comment.isGoodAlready = goodType == GoodCommentEvent.GOOD_COMMENT
                        comment.goodsCount = goodCount
                        GifFun.getHandler().post { hotCommentsAdapter!!.notifyItemChanged(i) }
                        return@Runnable
                    }
                }
            }
        }).start()
    }

    /**
     * 删除热门评论区的评论。
     *
     * @param commentId
     * 评论的ID值
     */
    fun deleteHotComment(commentId: Long) {
        Thread(Runnable {
            val adapter = hotCommentsAdapter
            val comments = hotComments
            if (adapter != null && comments != null && comments.isNotEmpty()) {
                var deleteIndex = -1
                for (i in comments.indices) {
                    val comment = comments[i]
                    if (comment.commentId == commentId) {
                        deleteIndex = i
                        break
                    }
                }
                if (deleteIndex != -1) {
                    val index = deleteIndex
                    comments.removeAt(index)
                    GifFun.getHandler().post {
                        if (comments.isEmpty()) {
                            activity.fetchFeedDetails()
                        } else {
                            adapter.notifyItemRemoved(index)
                        }
                    }
                }
            }
        }).start()
    }

    inner class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 发布一条评论。
     *
     * @param content 评论的具体内容
     */
    private fun postComment(content: String) {
        PostComment.getResponse(feed.feedId, content, object : Callback {
            override fun onResponse(response: Response) {
                if (!ResponseHandler.handleResponse(response)) {
                    val status = response.status
                    if (status == 0) {
                        val postComment = response as PostComment
                        showToast(GlobalUtil.getString(R.string.post_comment_success))
                        commentEdit!!.setText("")
                        if (hotComments == null || hotComments!!.isEmpty()) {
                            val comment = Comment()
                            comment.commentId = postComment.commentId
                            comment.nickname = UserUtil.nickname
                            comment.avatar = UserUtil.avatar
                            comment.content = content
                            comment.postDate = System.currentTimeMillis()
                            comment.userId = GifFun.getUserId()
                            val list = ArrayList<Comment>()
                            list.add(comment)
                            setHotComments(list)
                        }
                        val postCommentEvent = PostCommentEvent()
                        postCommentEvent.commentId = postComment.commentId
                        postCommentEvent.feedId = feed.feedId
                        EventBus.getDefault().post(postCommentEvent)
                        MobclickAgent.onEvent(activity, "10004")
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
                commentEdit!!.isEnabled = true
                postComment!!.isEnabled = true
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                ResponseHandler.handleFailure(e)
                commentEdit!!.isEnabled = true
                postComment!!.isEnabled = true
            }
        })
    }

    /**
     * 使评论输入框获取焦点并弹出键盘。
     */
    fun showSoftKeyboard() {
        Thread(Runnable {
            var timeCost: Long = 0
            val timeStart = System.currentTimeMillis()
            while (timeCost < 1000) {
                if (commentEdit != null) {
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    activity.runOnUiThread { activity.showSoftKeyboard(commentEdit) }
                    break
                }
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                timeCost = System.currentTimeMillis() - timeStart
            }
        }).start()
    }

    companion object {

        private const val TAG = "FeedDetailMoreAdapter"

        private const val DETAIL_INFO = 0

        private const val LOADING_COMMENTS = 1

        private const val HOT_COMMENTS = 2

        private const val NO_COMMENT = 3

        private const val ENTER_COMMENT = 4
    }

}
