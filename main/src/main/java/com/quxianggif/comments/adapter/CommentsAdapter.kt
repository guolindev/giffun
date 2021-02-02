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

package com.quxianggif.comments.adapter

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.comments.ui.CommentsActivity
import com.quxianggif.common.holder.CommentHolder
import com.quxianggif.common.holder.LoadingMoreViewHolder
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.Comment
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.DeleteCommentEvent
import com.quxianggif.event.GoodCommentEvent
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.DeleteComment
import com.quxianggif.network.model.GoodComment
import com.quxianggif.network.model.Response
import com.quxianggif.report.ReportActivity
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.DateUtil
import com.quxianggif.util.PopupUtil
import com.quxianggif.util.glide.CustomUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * 评论界面RecyclerView的适配器。
 *
 * @author davy, guolin
 * @since 17/7/17
 */
class CommentsAdapter(private val activity: CommentsActivity, private val comments: MutableList<Comment>, private val layoutManager: RecyclerView.LayoutManager?, private val mFeedId: Long) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * 获取RecyclerView数据源中元素的数量。
     *
     * @return RecyclerView数据源中元素的数量。
     */
    val dataItemCount: Int
        get() = comments.size

    private var isLoadFailed: Boolean = false
        get() = activity.isLoadFailed

    private var isNoMoreData: Boolean = false
        get() = activity.isNoMoreData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_COMMENTS -> createCommentsHolder(parent)
        TYPE_LOADING_MORE -> createLoadingMoreHolder(parent)
        else -> throw IllegalArgumentException()
    }

    private fun createCommentsHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.comment_item, parent, false)
        val holder = CommentHolder(view)
        holder.avatar.setOnClickListener {
            val position = holder.adapterPosition
            val comment = comments[position]
            UserHomePageActivity.actionStart(activity, holder.avatar, comment.userId, comment.nickname, comment.avatar, comment.bgImage)
        }
        holder.goodImage.setOnClickListener {
            val position = holder.adapterPosition
            val comment = comments[position]
            var goodsCount = comment.goodsCount
            val event = GoodCommentEvent()
            event.commentId = comment.commentId
            if (comment.isGoodAlready) {
                comment.isGoodAlready = false
                goodsCount--
                if (goodsCount < 0) {
                    goodsCount = 0
                }
                comment.goodsCount = goodsCount
                event.type = GoodCommentEvent.UNGOOD_COMMENT
            } else {
                comment.isGoodAlready = true
                comment.goodsCount = ++goodsCount
                event.type = GoodCommentEvent.GOOD_COMMENT
            }
            notifyItemChanged(position)
            GoodComment.getResponse(comment.commentId, null)
            event.goodsCount = goodsCount
            EventBus.getDefault().post(event)
        }
        holder.moreButton?.setOnClickListener {
            val commentPosition = holder.adapterPosition
            val comment = comments[commentPosition]
            val expandMenuItems = getExpandMenuItems(comment)
            val pair = PopupUtil.showCommentExpandMenu(activity,
                    expandMenuItems, holder.moreButton)
            val window = pair.first
            val expandMenuList = pair.second
            expandMenuList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                window.dismiss()
                val action = expandMenuItems[position]
                if (GlobalUtil.getString(R.string.report) == action) {
                    ReportActivity.actionReportComment(activity, comment.commentId)
                } else if (GlobalUtil.getString(R.string.delete) == action) {
                    doDeleteAction(commentPosition, comment.commentId)
                }
            }
        }
        return holder
    }

    private fun createLoadingMoreHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val holder = LoadingMoreViewHolder.createLoadingMoreViewHolder(activity, parent)
        holder.failed.setOnClickListener {
            activity.onLoad()
            notifyItemChanged(itemCount - 1)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_COMMENTS -> bindCommentsHolder(holder as CommentHolder, position)
            TYPE_LOADING_MORE -> bindLoadingMoreHolder(holder as LoadingMoreViewHolder)
        }
    }

    private fun bindCommentsHolder(holder: CommentHolder, position: Int) {
        val comment = comments[position]
        val avatar = comment.avatar
        Glide.with(activity)
                .load(CustomUrl(avatar))
                .bitmapTransform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.avatar)
        holder.nickname.text = comment.nickname
        holder.content.text = comment.content
        holder.postDate.text = DateUtil.getConvertedDate(comment.postDate)

        if (comment.goodsCount > 0) {
            holder.goodCount.visibility = View.VISIBLE
            holder.goodCount.text = GlobalUtil.getConvertedNumber(comment.goodsCount)
        } else {
            holder.goodCount.visibility = View.INVISIBLE
        }
        if (comment.isGoodAlready) {
            holder.goodImage.setImageResource(R.drawable.ic_gooded)
        } else {
            holder.goodImage.setImageResource(R.drawable.ic_good)
        }

        if (layoutManager != null) {
            val visibleItemCount = layoutManager.childCount
            if (visibleItemCount >= dataItemCount - 1) {
                activity.onLoad()
            }
        }
    }

    private fun bindLoadingMoreHolder(holder: LoadingMoreViewHolder) {
        when {
            isNoMoreData -> {
                holder.progress.visibility = View.GONE
                holder.failed.visibility = View.GONE
                holder.end.visibility = View.VISIBLE
            }
            isLoadFailed -> {
                holder.progress.visibility = View.GONE
                holder.failed.visibility = View.VISIBLE
                holder.end.visibility = View.GONE
            }
            else -> {
                holder.progress.visibility = View.VISIBLE
                holder.failed.visibility = View.GONE
                holder.end.visibility = View.GONE
            }
        }
    }

    /**
     * 元素的总数是fetchAllComments的数量+1（1是底部的加载更多视图）。
     */
    override fun getItemCount(): Int {
        return comments.size + 1
    }

    /**
     * 根据位置返回不同的view type。
     */
    override fun getItemViewType(position: Int): Int {
        return if (position < dataItemCount && dataItemCount > 0) {
            TYPE_COMMENTS
        } else TYPE_LOADING_MORE
    }

    /**
     * 执行删除评论的逻辑。
     * @param position
     * 要删除的评论的position
     * @param commentId
     * 要删除的评论的id
     */
    private fun doDeleteAction(position: Int, commentId: Long) {
        val builder = AlertDialog.Builder(activity, R.style.GifFunAlertDialogStyle)
        builder.setMessage(GlobalUtil.getString(R.string.delete_comment_confirm))
        builder.setPositiveButton(GlobalUtil.getString(R.string.ok)) { _, _ ->
            DeleteComment.getResponse(commentId, object : Callback {
                override fun onResponse(response: Response) {
                    if (response.status == 0) {
                        // 删除成功，发出删除事件，以更新相关界面数据。
                        val deleteCommentEvent = DeleteCommentEvent()
                        deleteCommentEvent.commentId = commentId
                        deleteCommentEvent.feedId = mFeedId
                        EventBus.getDefault().post(deleteCommentEvent)
                    } else {
                        showToast(GlobalUtil.getString(R.string.delete_failed))
                        logWarn(TAG, "Delete comment failed. " + GlobalUtil.getResponseClue(response.status, response.msg))
                    }
                }

                override fun onFailure(e: Exception) {
                    logWarn(TAG, e.message, e)
                    showToast(GlobalUtil.getString(R.string.delete_failed))
                }
            })
            comments.removeAt(position)
            notifyItemRemoved(position)
        }
        builder.setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
        builder.create().show()
    }

    /**
     * 获取扩展菜单中的子项，如果是自己所发的评论就显示删除项，否则不显示删除项。
     * @return 扩展菜单中的子项。
     */
    private fun getExpandMenuItems(comment: Comment): List<String> {
        val expandMenuItems = ArrayList<String>()
        expandMenuItems.add(GlobalUtil.getString(R.string.report))
        if (comment.userId == GifFun.getUserId()) {
            expandMenuItems.add(GlobalUtil.getString(R.string.delete))
        }
        return expandMenuItems
    }

    companion object {

        private const val TAG = "CommentsAdapter"

        private const val TYPE_COMMENTS = 0

        private const val TYPE_LOADING_MORE = 1
    }

}