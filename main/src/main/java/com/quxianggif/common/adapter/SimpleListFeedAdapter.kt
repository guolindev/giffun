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

package com.quxianggif.common.adapter

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quxianggif.R
import com.quxianggif.comments.ui.CommentsActivity
import com.quxianggif.common.holder.LoadingMoreViewHolder
import com.quxianggif.common.transitions.TransitionUtils
import com.quxianggif.common.view.CheckableImageButton
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.dp2px
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.BaseFeed
import com.quxianggif.core.model.FollowingFeed
import com.quxianggif.core.model.SimpleListFeed
import com.quxianggif.core.model.WorldFeed
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.DeleteFeedEvent
import com.quxianggif.event.LikeFeedEvent
import com.quxianggif.feeds.adapter.FollowingFeedAdapter
import com.quxianggif.feeds.ui.FeedDetailActivity
import com.quxianggif.feeds.ui.RepostFeedActivity
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.DeleteFeed
import com.quxianggif.network.model.LikeFeed
import com.quxianggif.network.model.Response
import com.quxianggif.report.ReportActivity
import com.quxianggif.user.adapter.UserFeedAdapter
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.DateUtil
import com.quxianggif.util.PopupUtil
import com.quxianggif.util.ViewUtils
import com.quxianggif.util.glide.CustomUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.extension.deleteAllAsync
import java.util.*

/**
 * 单列列表Feed数据显示的适配器。
 *
 * @author guolin
 * @since 17/10/15
 */
abstract class SimpleListFeedAdapter<T : SimpleListFeed, A : Activity>(protected open var activity: A, protected val feedList: MutableList<T>, private val maxImageWidth: Int,
                                                         private val layoutManager: RecyclerView.LayoutManager?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val maxImageHeight: Int = dp2px(250f)

    private var imageWidth: Int = 0

    private var imageHeight: Int = 0

    /**
     * 获取RecyclerView数据源中元素的数量。
     * @return RecyclerView数据源中元素的数量。
     */
    val dataItemCount: Int
        get() = feedList.size

    abstract var isNoMoreData: Boolean

    abstract var isLoadFailed: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_FEEDS -> return createFeedHolder(parent)
            TYPE_REFEEDS -> return createRefeedHolder(parent)
            TYPE_LOADING_MORE -> return createLoadingMoreHolder(parent)
        }
        throw IllegalArgumentException()
    }

    protected fun initBaseFeedHolder(holder: SimpleListFeedViewHolder) {
        holder.cardView.setOnClickListener { v ->
            val position = holder.adapterPosition
            if (AndroidVersion.hasLollipopMR1()) {
                setFabTransition(holder.feedCover)
            }
            val feed: BaseFeed?
            val simpleListFeed = feedList[position]
            feed = if (simpleListFeed.feedType == 1) {
                simpleListFeed.refFeed()
            } else {
                simpleListFeed
            }
            if (feed == null) {
                showToast(GlobalUtil.getString(R.string.ref_feed_deleted))
                return@setOnClickListener
            } else if (feed.coverLoadFailed) {
                loadFeedCover(feed, holder)
                return@setOnClickListener
            } else if (!feed.coverLoaded) {
                return@setOnClickListener
            }
            val coverImage = v.findViewById<ImageView>(R.id.feedCover)
            FeedDetailActivity.actionStart(activity, coverImage, feed)
        }
        holder.expandButton.setOnClickListener {
            val feedPosition = holder.adapterPosition
            val feed = feedList[feedPosition]
            val expandMenuItems = getExpandMenuItems(feed)
            val pair = PopupUtil.showUserFeedExpandMenu(activity, expandMenuItems, holder.expandButton)
            val window = pair.first
            val expandMenuList = pair.second
            expandMenuList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                window.dismiss()
                val action = expandMenuItems[position]
                when (action) {
                    GlobalUtil.getString(R.string.share) -> showToast(GlobalUtil.getString(R.string.currently_not_supported))
                    GlobalUtil.getString(R.string.report) -> ReportActivity.actionReportFeed(activity, feed.feedId)
                    GlobalUtil.getString(R.string.delete) -> doDeleteAction(feedPosition, feed.feedId)
                }
            }
        }
        holder.likesLayout.setOnClickListener {
            val position = holder.adapterPosition
            val feed = feedList[position]
            var likesCount = feed.likesCount
            val event = LikeFeedEvent()
            event.feedId = feed.feedId
            if (this@SimpleListFeedAdapter is FollowingFeedAdapter) {
                event.from = LikeFeedEvent.FROM_FOLLOWING
            } else if (this@SimpleListFeedAdapter is UserFeedAdapter) {
                event.from = LikeFeedEvent.FROM_USER_HOME
            }
            if (feed.isLikedAlready) {
                feed.isLikedAlready = false
                likesCount -= 1
                if (likesCount < 0) {
                    likesCount = 0
                }
                feed.likesCount = likesCount
                event.type = LikeFeedEvent.UNLIKE_FEED
            } else {
                feed.isLikedAlready = true
                feed.likesCount = ++likesCount
                event.type = LikeFeedEvent.LIKE_FEED
            }
            notifyItemChanged(position)
            LikeFeed.getResponse(feed.feedId, null)
            event.likesCount = likesCount
            EventBus.getDefault().post(event)
        }
        holder.repostLayout.setOnClickListener(View.OnClickListener {
            val feedPosition = holder.adapterPosition
            val feed: BaseFeed?
            val itemType = getItemViewType(feedPosition)
            feed = if (itemType == TYPE_REFEEDS) {
                feedList[feedPosition].refFeed()
            } else {
                feedList[feedPosition]
            }
            if (feed == null) {
                showToast(GlobalUtil.getString(R.string.ref_feed_deleted))
                return@OnClickListener
            }
            RepostFeedActivity.actionStart(activity, feed)
        })
        holder.commentLayout.setOnClickListener {
            val feedPosition = holder.adapterPosition
            val feed = feedList[feedPosition]
            CommentsActivity.actionStart(activity, feed.feedId)
        }
    }

    private fun createLoadingMoreHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val holder = LoadingMoreViewHolder.createLoadingMoreViewHolder(activity, parent)
        holder.failed.setOnClickListener {
            onLoad()
            notifyItemChanged(itemCount - 1)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FeedViewHolder -> bindFeedHolder(holder, position)
            is RefeedViewHolder -> bindRefeedHolder(holder, position)
            is LoadingMoreViewHolder -> bindLoadingMoreHolder(holder)
        }
    }

    protected open fun bindFeedHolder(holder: FeedViewHolder, position: Int) {
        val feed = feedList[position]
        bindFeedCover(holder, feed)
        bindFeedBasicInfo(holder, feed)
    }

    protected open fun bindRefeedHolder(holder: RefeedViewHolder, position: Int) {
        val feed = feedList[position]
        val refFeed = feed.refFeed()
        if (refFeed != null) {
            holder.refFeedContent.visibility = View.VISIBLE
            holder.feedCover.visibility = View.VISIBLE
            holder.refFeedDeleted.visibility = View.GONE
            holder.refFeedContent.text = getRefFeedContent(refFeed)
            holder.refFeedContent.movementMethod = LinkMovementMethod.getInstance()
            bindFeedCover(holder, refFeed)
        } else {
            holder.refFeedContent.visibility = View.GONE
            holder.feedCover.visibility = View.GONE
            holder.refFeedDeleted.visibility = View.VISIBLE
        }
        bindFeedBasicInfo(holder, feed)
    }

    private fun getRefFeedContent(refFeed: BaseFeed): SpannableString {
        val nickname = refFeed.nickname
        val spanString = SpannableString(nickname + ": " + refFeed.content)
        spanString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                UserHomePageActivity.actionStart(activity, null, refFeed.userId, refFeed.nickname, refFeed.avatar, refFeed.bgImage)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.isUnderlineText = false //去除超链接的下划线
            }
        }, 0, nickname.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        spanString.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity, R.color.refeed_nickname)),
                0, nickname.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spanString
    }

    /**
     * 加载Feed的封面。
     */
    private fun bindFeedCover(holder: SimpleListFeedViewHolder, feed: BaseFeed) {
        calculateImageHeight(feed)
        holder.feedCover.layoutParams.width = imageWidth
        holder.feedCover.layoutParams.height = imageHeight
        loadFeedCover(feed, holder)
    }

    /**
     * 加载Feed的基础数据信息。
     */
    private fun bindFeedBasicInfo(holder: SimpleListFeedViewHolder, feed: SimpleListFeed) {
        holder.nickname.text = feed.nickname
        holder.feedContent.text = feed.content
        holder.postDate.text = DateUtil.getConvertedDate(feed.postDate)
        holder.likesCount.text = feed.likesCount.toString()

        if (AndroidVersion.hasLollipop()) {
            val imageButton = holder.likes as CheckableImageButton
            imageButton.isChecked = feed.isLikedAlready
        } else {
            if (feed.isLikedAlready) {
                holder.likes.setImageResource(R.drawable.ic_liked)
            } else {
                holder.likes.setImageResource(R.drawable.ic_like)
            }
        }
        if (feed.avatar.isBlank()) {
            Glide.with(activity)
                 .load(R.drawable.avatar_default)
                 .bitmapTransform(CropCircleTransformation(activity))
                 .placeholder(R.drawable.loading_bg_circle)
                 .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                 .into(holder.avatar)
        } else {
            Glide.with(activity)
                 .load(CustomUrl(feed.avatar))
                 .bitmapTransform(CropCircleTransformation(activity))
                 .placeholder(R.drawable.loading_bg_circle)
                 .error(R.drawable.avatar_default)
                 .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                 .into(holder.avatar)
        }

        if (layoutManager != null) {
            val visibleItemCount = layoutManager.childCount
            if (visibleItemCount >= dataItemCount - 1) {
                onLoad()
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

    private fun calculateImageHeight(feed: BaseFeed) {
        val originalWidth = feed.imgWidth
        val originalHeight = feed.imgHeight
        imageWidth = maxImageWidth
        imageHeight = imageWidth * originalHeight / originalWidth
        if (imageHeight > maxImageHeight) {
            imageHeight = maxImageHeight
            imageWidth = imageHeight * originalWidth / originalHeight
        }
    }

    /**
     * 元素的总数是Feeds的数量+1（1是底部的加载更多视图）。
     */
    override fun getItemCount(): Int {
        return dataItemCount + 1
    }

    /**
     * 根据位置返回不同的view type。
     */
    override fun getItemViewType(position: Int): Int {
        if (position < dataItemCount && dataItemCount > 0) {
            val feed = feedList[position]
            return if (feed.feedType == 1) { // 转发类型
                TYPE_REFEEDS
            } else {
                TYPE_FEEDS
            }
        }
        return TYPE_LOADING_MORE
    }

    /**
     * 获取扩展菜单中的子项，如果是自己所发的Feed就显示删除项，否则不显示删除项。
     * @return 扩展菜单中的子项。
     */
    private fun getExpandMenuItems(feed: SimpleListFeed): List<String> {
        val expandMenuItems = ArrayList<String>()
        expandMenuItems.add(GlobalUtil.getString(R.string.report))
        if (feed.userId == GifFun.getUserId()) {
            expandMenuItems.add(GlobalUtil.getString(R.string.delete))
        }
        return expandMenuItems
    }

    /**
     * 执行删除Feed的逻辑。
     * @param position
     * 要删除的Feed的position
     * @param feedId
     * 要删除的Feed的id
     */
    private fun doDeleteAction(position: Int, feedId: Long) {
        val builder = AlertDialog.Builder(activity, R.style.GifFunAlertDialogStyle)
        builder.setMessage(GlobalUtil.getString(R.string.delete_feed_confirm))
        builder.setPositiveButton(GlobalUtil.getString(R.string.ok)) { _, _ ->
            DeleteFeed.getResponse(feedId, object : Callback {
                override fun onResponse(response: Response) {
                    if (response.status == 0) {
                        // 删除成功，发出删除事件，以更新相关界面数据。
                        val deleteFeedEvent = DeleteFeedEvent()
                        deleteFeedEvent.feedId = feedId
                        if (this@SimpleListFeedAdapter is UserFeedAdapter) {
                            deleteFeedEvent.type = DeleteFeedEvent.DELETE_FROM_USER_HOME_PAGE
                        } else if (this@SimpleListFeedAdapter is FollowingFeedAdapter) {
                            deleteFeedEvent.type = DeleteFeedEvent.DELETE_FROM_FOLLOWING_PAGE
                        }
                        EventBus.getDefault().post(deleteFeedEvent)
                    } else {
                        showToast(GlobalUtil.getString(R.string.delete_failed))
                        logWarn(TAG, "Delete feed failed. " + GlobalUtil.getResponseClue(response.status, response.msg))

                    }
                }

                override fun onFailure(e: Exception) {
                    logWarn(TAG, e.message, e)
                    showToast(GlobalUtil.getString(R.string.delete_failed))
                }
            })
            feedList.removeAt(position)
            notifyItemRemoved(position)
            LitePal.deleteAllAsync<WorldFeed>("feedid = ?", feedId.toString()).listen(null)
            LitePal.deleteAllAsync<FollowingFeed>("feedid = ?", feedId.toString()).listen(null)
        }
        builder.setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
        builder.create().show()
    }

    private fun loadFeedCover(feed: BaseFeed, holder: SimpleListFeedViewHolder) {
        Glide.with(activity)
                .load(feed.cover)
                .override(feed.imgWidth, feed.imgHeight)
                .placeholder(R.drawable.loading_bg_rect)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: java.lang.Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                        feed.coverLoaded = false
                        feed.coverLoadFailed = true
                        return false
                    }

                    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        feed.coverLoaded = true
                        feed.coverLoadFailed = false
                        return false
                    }

                })
                .into(holder.feedCover)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setFabTransition(item: View) {
        val fab = activity.findViewById<View>(R.id.composeFab)
        if (!ViewUtils.viewsIntersect(item, fab)) return

        val reenter = TransitionInflater.from(activity)
                .inflateTransition(R.transition.compose_fab_reenter)
        reenter.addListener(object : TransitionUtils.TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                activity.window.reenterTransition = null
            }
        })
        activity.window.reenterTransition = reenter
    }

    abstract fun onLoad()

    abstract fun createFeedHolder(parent: ViewGroup): FeedViewHolder

    abstract fun createRefeedHolder(parent: ViewGroup): RefeedViewHolder

    class FeedViewHolder(view: View) : SimpleListFeedViewHolder(view)

    class RefeedViewHolder(view: View) : SimpleListFeedViewHolder(view) {

        internal val refFeedContent: TextView = view.findViewById(R.id.refFeedContent)

        internal val refFeedDeleted: TextView = view.findViewById(R.id.refFeedDeleted)

    }

    open class SimpleListFeedViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        val cardView: CardView = view as CardView

        val avatar: ImageView = view.findViewById(R.id.avatar)

        val nickname: TextView = view.findViewById(R.id.nickname)

        val postDate: TextView = view.findViewById(R.id.postDate)

        val expandButton: ImageView = view.findViewById(R.id.expandButton)

        val feedCover: ImageView = view.findViewById(R.id.feedCover)

        val feedContent: TextView = view.findViewById(R.id.feedContent)

        val likesCount: TextView = view.findViewById(R.id.likesCount)

        val likes: ImageView = view.findViewById(R.id.likes)

        val likesLayout: FrameLayout = view.findViewById(R.id.likesLayout)

        val repostLayout: FrameLayout = view.findViewById(R.id.repostLayout)

        val commentLayout: FrameLayout = view.findViewById(R.id.commentLayout)

    }

    companion object {

        private const val TAG = "SimpleListFeedAdapter"

        private const val TYPE_FEEDS = 0

        private const val TYPE_REFEEDS = 2

        private const val TYPE_LOADING_MORE = 1
    }

}
