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

import android.app.Activity
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.common.holder.LoadingMoreViewHolder
import com.quxianggif.core.extension.dp2px
import com.quxianggif.core.model.BaseFeed
import com.quxianggif.core.model.SearchItem
import com.quxianggif.core.model.User
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.feeds.ui.FeedDetailActivity
import com.quxianggif.feeds.ui.SearchActivity
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.DateUtil
import com.quxianggif.util.glide.CustomUrl
import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * SearchActivity的适配器，处理搜索到的用户和Feed的显示和点击事件。
 * @author guolin
 * @since 2018/8/2
 */
class SearchAdapter(var activity: SearchActivity, val searchItemList: MutableList<SearchItem>, val maxImageWidth: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val maxImageHeight: Int = dp2px(250f)

    private var imageWidth: Int = 0

    private var imageHeight: Int = 0

    private var isLoadFailed: Boolean = false
        get() = activity.isLoadFailed

    private var isNoMoreData: Boolean = false
        get() = activity.isNoMoreData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            SEARCH_USERS -> return createUserViewHolder(parent)
            SEARCH_FEEDS -> return createFeedViewHolder(parent)
            LOADING_MORE -> return createLoadingMoreHolder(parent)
        }
        throw IllegalArgumentException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> bindUserViewHolder(holder, position)
            is FeedViewHolder -> bindFeedViewHolder(holder, position)
            is LoadingMoreViewHolder -> bindLoadingMoreHolder(holder)
        }
    }

    override fun getItemCount() = if (searchItemList.isNotEmpty()) {
        searchItemList.size + 1
    } else {
        0
    }

    override fun getItemViewType(position: Int) : Int {
        if (position == itemCount - 1) {
            return LOADING_MORE
        }
        val searchItem = searchItemList[position]
        return when (searchItem) {
            is User -> SEARCH_USERS
            is BaseFeed -> SEARCH_FEEDS
            else -> throw IllegalArgumentException()
        }
    }

    fun clear() {
        searchItemList.clear()
        notifyDataSetChanged()
    }

    private fun createUserViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.followship_item, parent, false)
        val holder = UserViewHolder(view)
        holder.rootLayout.setOnClickListener {
            val position = holder.adapterPosition
            val user = searchItemList[position] as User
            UserHomePageActivity.actionStart(activity, holder.avatar, user.userId, user.nickname, user.avatar, user.bgImage)
        }
        return holder
    }

    private fun createFeedViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.search_feed_item, parent, false)
        val holder = FeedViewHolder(view)
        holder.cardView.setOnClickListener { v ->
            val position = holder.adapterPosition
            val feed = searchItemList[position] as BaseFeed
            val coverImage = v.findViewById<ImageView>(R.id.feedCover)
            FeedDetailActivity.actionStart(activity, coverImage, feed)
        }
        val userInfoListener = View.OnClickListener {
            val position = holder.adapterPosition
            val feed = searchItemList[position] as BaseFeed
            UserHomePageActivity.actionStart(activity, holder.avatar, feed.userId, feed.nickname, feed.avatar, feed.bgImage)
        }
        holder.avatar.setOnClickListener(userInfoListener)
        holder.nickname.setOnClickListener(userInfoListener)
        return holder
    }

    private fun createLoadingMoreHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val holder = LoadingMoreViewHolder.createLoadingMoreViewHolder(activity, parent)
        holder.failed.setOnClickListener {
            activity.onLoad()
        }
        return holder
    }

    private fun bindUserViewHolder(holder: UserViewHolder, position: Int) {
        val user = searchItemList[position] as User
        val builder = SpannableStringBuilder(user.nickname)
        val span = ForegroundColorSpan(ContextCompat.getColor(activity, R.color.colorPrimary))
        val index = user.nickname.toLowerCase().indexOf(activity.keyword.toLowerCase())
        if (index != -1) builder.setSpan(span, index, index + activity.keyword.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        holder.nickname.text = builder
        if (TextUtils.isEmpty(user.description)) {
            holder.description.text = GlobalUtil.getString(R.string.user_does_not_feed_anything)
        } else {
            holder.description.text = user.description
        }
        Glide.with(activity)
                .load(CustomUrl(user.avatar))
                .asBitmap()
                .transform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.avatar)
    }

    private fun bindFeedViewHolder(holder: FeedViewHolder, position: Int) {
        val feed = searchItemList[position] as BaseFeed
        calculateImageHeight(feed)
        holder.feedCover.layoutParams.width = imageWidth
        holder.feedCover.layoutParams.height = imageHeight
        Glide.with(activity)
                .load(feed.cover)
                .asBitmap()
                .override(feed.imgWidth, feed.imgHeight)
                .placeholder(R.drawable.loading_bg_rect)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .into(holder.feedCover)

        holder.nickname.text = feed.nickname
        val builder = SpannableStringBuilder(feed.content)
        val span = ForegroundColorSpan(ContextCompat.getColor(activity, R.color.colorPrimary))
        val index = feed.content.toLowerCase().indexOf(activity.keyword.toLowerCase())
        if (index != -1) builder.setSpan(span, index, index + activity.keyword.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        holder.feedContent.text = builder
        holder.postDate.text = DateUtil.getConvertedDate(feed.postDate)
        Glide.with(activity)
                .load(CustomUrl(feed.avatar))
                .asBitmap()
                .transform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.avatar)
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

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val rootLayout: LinearLayout = view as LinearLayout

        val avatar: ImageView = view.findViewById(R.id.avatar)

        val nickname: TextView = view.findViewById(R.id.nickname)

        val description: TextView = view.findViewById(R.id.description)

    }

    inner class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cardView: CardView = view as CardView

        val avatar: ImageView = view.findViewById(R.id.avatar)

        val nickname: TextView = view.findViewById(R.id.nickname)

        val postDate: TextView = view.findViewById(R.id.postDate)

        val feedCover: ImageView = view.findViewById(R.id.feedCover)

        val feedContent: TextView = view.findViewById(R.id.feedContent)

    }

    companion object {

        private const val TAG = "SearchAdapter"

        private const val SEARCH_USERS = 0

        private const val SEARCH_FEEDS = 1

        private const val LOADING_MORE = 2

    }

}
