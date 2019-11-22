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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quxianggif.R
import com.quxianggif.common.holder.LoadingMoreViewHolder
import com.quxianggif.common.transitions.TransitionUtils
import com.quxianggif.common.view.CheckableImageButton
import com.quxianggif.core.model.WaterFallFeed
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.LikeFeedEvent
import com.quxianggif.feeds.adapter.HotFeedAdapter
import com.quxianggif.feeds.adapter.WorldFeedAdapter
import com.quxianggif.feeds.ui.FeedDetailActivity
import com.quxianggif.network.model.LikeFeed
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.ViewUtils
import com.quxianggif.util.glide.CustomUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

/**
 * 瀑布流列表Feed数据显示的适配器。
 * @author guolin
 * @since 2018/2/15
 */
abstract class WaterFallFeedAdapter<T : WaterFallFeed>(protected var activity: Activity, private val feedList: List<T>, private val imageWidth: Int,
                                                       private val layoutManager: RecyclerView.LayoutManager?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * 获取RecyclerView数据源中元素的数量。
     * @return RecyclerView数据源中元素的数量。
     */
    private val dataItemCount: Int
        get() = feedList.size

    abstract var isNoMoreData: Boolean

    abstract var isLoadFailed: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_FEEDS -> return createFeedHolder(parent)
            TYPE_LOADING_MORE -> return createLoadingMoreHolder(parent)
        }
        throw IllegalArgumentException()
    }

    private fun createLoadingMoreHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val holder =  LoadingMoreViewHolder.createLoadingMoreViewHolder(activity, parent)
        holder.failed.setOnClickListener {
            onLoad()
            notifyItemChanged(itemCount - 1)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FeedViewHolder) {
            bindFeedHolder(holder, position)
        } else if (holder is LoadingMoreViewHolder) {
            bindLoadingMoreHolder(holder)
        }
    }

    protected fun baseCreateFeedHolder(holder: FeedViewHolder) {
        holder.cardView.setOnClickListener { v ->
            if (AndroidVersion.hasLollipopMR1()) {
                setFabTransition(holder.feedCover)
            }
            val position = holder.adapterPosition
            val feed = feedList[position]
            if (feed.coverLoadFailed) {
                loadFeedCover(feed, holder, calculateImageHeight(feed))
                return@setOnClickListener
            }
            if (!feed.coverLoaded) {
                return@setOnClickListener
            }
            val coverImage = v.findViewById<ImageView>(R.id.feedCover)
            FeedDetailActivity.actionStart(activity, coverImage, feed)
        }
        holder.likesLayout.setOnClickListener {
            val position = holder.adapterPosition
            val feed = feedList[position]
            var likesCount = feed.likesCount
            val event = LikeFeedEvent()
            if (this@WaterFallFeedAdapter is WorldFeedAdapter) {
                event.from = LikeFeedEvent.FROM_WORLD
            } else if (this@WaterFallFeedAdapter is HotFeedAdapter) {
                event.from = LikeFeedEvent.FROM_HOT
            }
            event.feedId = feed.feedId
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
        val userInfoListener = View.OnClickListener {
            val position = holder.adapterPosition
            val feed = feedList[position]
            UserHomePageActivity.actionStart(activity, holder.avatar, feed.userId, feed.nickname, feed.avatar, feed.bgImage)
        }
        holder.avatar.setOnClickListener(userInfoListener)
        holder.nickname.setOnClickListener(userInfoListener)
    }

    protected fun baseBindFeedHolder(holder: FeedViewHolder, position: Int) {
        val feed = feedList[position]
        holder.feedContent.text = feed.content
        holder.nickname.text = feed.nickname
        holder.likesCount.text = GlobalUtil.getConvertedNumber(feed.likesCount)
        val imageHeight = calculateImageHeight(feed)
        holder.feedCover.layoutParams.width = imageWidth
        holder.feedCover.layoutParams.height = imageHeight

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

        loadFeedCover(feed, holder, imageHeight)
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

    private fun calculateImageHeight(feed: WaterFallFeed): Int {
        val originalWidth = feed.imgWidth
        val originalHeight = feed.imgHeight
        return imageWidth * originalHeight / originalWidth
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
        return if (position < dataItemCount && dataItemCount > 0) {
            TYPE_FEEDS
        } else TYPE_LOADING_MORE
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams
        if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = holder.itemViewType == TYPE_LOADING_MORE
        }
    }

    private fun loadFeedCover(feed: T, holder: FeedViewHolder, imageHeight: Int) {
        Glide.with(activity)
                .load(feed.cover)
                .override(imageWidth, imageHeight)
                .placeholder(R.drawable.loading_bg_rect)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
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

    abstract fun bindFeedHolder(holder: FeedViewHolder, position: Int)

    open class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cardView: CardView = view as CardView

        val feedCover: ImageView = view.findViewById(R.id.feedCover)

        val feedContent: TextView = view.findViewById(R.id.feedContent)

        val avatar: ImageView = view.findViewById(R.id.avatar)

        val nickname: TextView = view.findViewById(R.id.nickname)

        val likes: ImageView = view.findViewById(R.id.likes)

        val likesCount: TextView = view.findViewById(R.id.likesCount)

        val likesLayout: LinearLayout = view.findViewById(R.id.likesLayout)

    }

    companion object {

        private const val TAG = "WaterFallFeedAdapter"

        const val TYPE_FEEDS = 0

        private const val TYPE_LOADING_MORE = 1
    }

}
