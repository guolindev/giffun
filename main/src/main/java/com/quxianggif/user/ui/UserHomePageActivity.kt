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

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quxianggif.R
import com.quxianggif.common.callback.InfiniteScrollListener
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.*
import com.quxianggif.core.model.UserFeed
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.*
import com.quxianggif.network.model.*
import com.quxianggif.report.ReportActivity
import com.quxianggif.user.adapter.UserFeedAdapter
import com.quxianggif.util.ColorUtils
import com.quxianggif.util.ResponseHandler
import com.quxianggif.util.UserUtil
import com.quxianggif.util.ViewUtils
import com.quxianggif.util.glide.CustomUrl
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_user_home_page.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 用户个人主页的Activity，在这里展示用户的个人信息，以及用户所发的所有Feeds。
 *
 * @author guolin
 * @since 17/7/15
 */
class UserHomePageActivity : BaseActivity(), LoadDataListener {

    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var adapter: UserFeedAdapter

    /**
     * RecyclerView的数据源，用于存储所有展示中的Feeds。
     */
    private lateinit var feedList: MutableList<UserFeed>

    /**
     * 当前主页用户的id。
     */
    private var mUserId: Long = 0

    /**
     * 当前主页用户的昵称。
     */
    var mNickname = ""

    /**
     * 当前主页用户的头像。
     */
    private var mAvatar = ""

    /**
     * 当前主页用户的个人简介。
     */
    private var mDescription = ""

    /**
     * 当前主页用户的背景图。
     */
    private var mBgImage = ""

    /**
     * 是否已经关注此用户。
     */
    private var isFollowed = false

    /**
     * 判断当前Fab按钮的关注状态
     */
    private var isFabFollowed = false

    private var isFollowInProgress = false

    private var isUserBgImageDark = false

    private var isToolbarAndStatusbarIconDark = false

    private var isUserBgImageLoaded = false

    /**
     * 判断是否还有更多数据，当服务器端没有更多数据时，此值为true。
     */
    /**
     * 判断是否还有更多数据。
     *
     * @return 当服务器端没有更多数据时，此值为true，否则此值为false。
     */
    var isNoMoreData = false
        private set

    var isLoadFailed = false

    /**
     * 判断是否正在加载Feeds。
     */
    private var isLoading = false

    /**
     * 监听AppBarLayout的滑动，根据滑动的状态进行相应的界面效果切换。
     */
    private var titleOffsetChangeListener: AppBarLayout.OnOffsetChangedListener = object : AppBarLayout.OnOffsetChangedListener {

        var scrollRange = -1

        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange
            }
            if (scrollRange + verticalOffset < dp2px(8f)) {
                collapsingToolbar.title = mNickname
            } else {
                collapsingToolbar.title = " "
            }
            if (!isUserBgImageLoaded) { // 如果用户背景图还没加载出来，则不执行以下代码。
                return
            }
            if (collapsingToolbar.height + verticalOffset < collapsingToolbar.scrimVisibleHeightTrigger) { // 用户信息和状态栏合并
                // 先判断背景图是否是深色的，因为深色情况下不用改变状态栏和Toolbar的颜色，保持默认即可。
                if (!isUserBgImageDark && isToolbarAndStatusbarIconDark) {
                    setToolbarAndStatusbarIconIntoLight()
                    isToolbarAndStatusbarIconDark = false
                }
            } else { // 用户信息和状态栏分离
                // 先判断背景图是否是深色的，因为深色情况下不用改变状态栏和Toolbar的颜色，保持默认即可。
                if (!isUserBgImageDark && !isToolbarAndStatusbarIconDark) {
                    setToolbarAndStatusbarIconIntoDark()
                    isToolbarAndStatusbarIconDark = true
                }
            }
        }

    }

    private var userBgLoadListener: RequestListener<Any, GlideDrawable> = object : RequestListener<Any, GlideDrawable> {

        override fun onException(e: Exception?, model: Any, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onResourceReady(glideDrawable: GlideDrawable?, model: Any, target: Target<GlideDrawable>,
                                     isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            if (glideDrawable == null) {
                return false
            }
            val bitmap = glideDrawable.toBitmap()
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            if (bitmapWidth <= 0 || bitmapHeight <= 0) {
                return false
            }
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    .setRegion(0, 0, bitmapWidth - 1, (bitmapHeight * 0.1).toInt()) // 测量图片头部的颜色，以确定状态栏和导航栏的颜色
                    .generate { palette ->
                        isUserBgImageDark = ColorUtils.isBitmapDark(palette, bitmap)
                        if (isUserBgImageDark) {
                            isToolbarAndStatusbarIconDark = false
                            setToolbarAndStatusbarIconIntoLight()
                        } else {
                            isToolbarAndStatusbarIconDark = true
                            setToolbarAndStatusbarIconIntoDark()
                        }
                        isUserBgImageLoaded = true
                    }

            val left = (bitmapWidth * 0.2).toInt()
            val right = bitmapWidth - left
            val top = bitmapHeight / 2
            val bottom = bitmapHeight - 1
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    .setRegion(left, top, right, bottom) // 测量图片下半部分的颜色，以确定用户信息的颜色
                    .generate { palette ->
                        val isDark = ColorUtils.isBitmapDark(palette, bitmap)
                        val color: Int
                        color = if (isDark) {
                            ContextCompat.getColor(this@UserHomePageActivity, R.color.white_text)
                        } else {
                            ContextCompat.getColor(this@UserHomePageActivity, R.color.black_text)
                        }
                        userNickname.setTextColor(color)
                        userFeedsText.setTextColor(color)
                        userDescription.setTextColor(color)
                        userFollowingsText.setTextColor(color)
                        userFollowersText.setTextColor(color)
                    }
            return false
        }
    }

    /**
     * 通过获取屏幕宽度来计算出每张图片最大的宽度。
     *
     * @return 计算后得出的每张图片最大的宽度。
     */
    private/* CardView margin *//* ImageView margin */ val maxImageWidth: Int
        get() {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay?.getMetrics(metrics)
            val columnWidth = metrics.widthPixels
            return columnWidth - dp2px((24 + 20).toFloat())
        }

    /**
     * 判断当前是否是已登录用户本人的个人主页。
     * @return 如果是本人的个人主页返回true，否则返回false。
     */
    private val isCurrentUserHomePage: Boolean
        get() = mUserId == GifFun.getUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        mUserId = intent.getLongExtra(USER_ID, 0)
        mNickname = intent.getStringExtra(NICKNAME)
        mAvatar = intent.getStringExtra(AVATAR)
        mBgImage = intent.getStringExtra(BG_IMAGE)
        setContentView(R.layout.activity_user_home_page)
    }

    override fun setupViews() {
        setupToolbar()

        userCountsLayout.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
        progressBarLayout.visibility = View.VISIBLE

        // setup RecyclerView
        layoutManager = LinearLayoutManager(this)
        feedList = ArrayList()
        adapter = UserFeedAdapter(this, feedList, maxImageWidth, layoutManager)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.addOnScrollListener(object : InfiniteScrollListener(layoutManager) {
            override fun onLoadMore() {
                loadUserFeeds()
            }

            override fun isDataLoading() = isLoading

            override fun isNoMoreData() = isNoMoreData

        })

        collapsingToolbar.title = " " // 使用这种方式来隐藏title
        if (AndroidVersion.hasLollipop()) {
            userNickname.letterSpacing = 0.1f
        }

        setupUserInfo()

        // setup events
        userAvatar.setOnClickListener {
            if (!TextUtils.isEmpty(mAvatar)) {
                activity?.let { it1 -> BrowserPhotoActivity.actionStart(it1, mAvatar, userAvatar) }
            }
        }
        userFeedsText.setOnClickListener { appBar.setExpanded(false, true) }
        userFollowingsText.setOnClickListener { FollowshipActivity.actionFollowings(this@UserHomePageActivity, mUserId, mNickname) }
        userFollowersText.setOnClickListener { FollowshipActivity.actionFollowers(this@UserHomePageActivity, mUserId, mNickname) }
        fab.setOnClickListener {
            if (isCurrentUserHomePage) {
                onEditFabClicked()
            } else {
                onFollowsFabClicked()
            }
        }
        appBar.addOnOffsetChangedListener(titleOffsetChangeListener)
        appBar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                appBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // 数据没加载出来之前，需要先禁用AppBarLayout的滑动功能。
                setAppBarLayoutCanDrag(false)
            }
        })

        loadUserFeeds()
    }

    /**
     * 加载并显示用户个人主页上的基本信息。
     */
    private fun setupUserInfo() {
        userNickname.text = mNickname
        if (!TextUtils.isEmpty(mDescription)) {
            userDescription.visibility = View.VISIBLE
            userDescription.text = String.format(GlobalUtil.getString(R.string.description_content), mDescription)
        } else {
            userDescription.visibility = View.INVISIBLE
        }
        isUserBgImageLoaded = false
        Glide.with(this)
                .load(CustomUrl(mAvatar))
                .bitmapTransform(CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .into(userAvatar)
        if (TextUtils.isEmpty(mBgImage)) {
            if (!TextUtils.isEmpty(mAvatar)) {
                Glide.with(this)
                        .load(CustomUrl(mAvatar))
                        .bitmapTransform(BlurTransformation(this, 15))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .listener(userBgLoadListener)
                        .into(userBgImage)
            }
        } else {
            Glide.with(this)
                    .load(mBgImage)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(userBgLoadListener)
                    .into(userBgImage)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null && !isCurrentUserHomePage) {
            val followUserItem =  menu.findItem(R.id.follow_user)
            val unfollowUserItem = menu.findItem(R.id.unfollow_user)
            if (isFabFollowed) {
                followUserItem.isEnabled = false
                followUserItem.isVisible = false
                unfollowUserItem.isEnabled = true
                unfollowUserItem.isVisible = true
            } else {
                unfollowUserItem.isEnabled = false
                unfollowUserItem.isVisible = false
                followUserItem.isEnabled = true
                followUserItem.isVisible = true
            }
            return true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isCurrentUserHomePage) {
            menuInflater.inflate(R.menu.menu_user_home_page, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.report_user -> {
                ReportActivity.actionReportUser(this, mUserId)
                return true
            }
            R.id.follow_user -> {
                onFollowsFabClicked()
                return true
            }
            R.id.unfollow_user -> {
                onFollowsFabClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLoad() {
        isNoMoreData = false
        isLoadFailed =false
        loadUserFeeds()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is LikeFeedEvent) {
            if (messageEvent.from == LikeFeedEvent.FROM_USER_HOME) {
                return
            }
            val feedId = messageEvent.feedId
            searchModelIndex(feedList, feedId) { index ->
                val feed = feedList[index]
                feed.isLikedAlready = messageEvent.type == LikeFeedEvent.LIKE_FEED
                feed.likesCount = messageEvent.likesCount
                adapter.notifyItemChanged(index)
            }
        } else if (messageEvent is DeleteFeedEvent) {
            updateFeedCountAfterDelete()
            if (feedList.isEmpty()) {
                loadFinished()
            }
        } else if (messageEvent is ModifyUserInfoEvent) {
            if (messageEvent.modifyNickname) {
                mNickname = UserUtil.nickname
            }
            if (messageEvent.modifyAvatar) {
                mAvatar = UserUtil.avatar
            }
            if (messageEvent.modifyBgImage) {
                mBgImage = UserUtil.bgImage
            }
            if (messageEvent.modifyDescription) {
                mDescription = UserUtil.description
            }
            if (messageEvent.modifyAvatar || messageEvent.modifyBgImage || messageEvent.modifyDescription || messageEvent.modifyNickname) {
                setupUserInfo()
            }
            if (messageEvent.modifyNickname || messageEvent.modifyAvatar) {
                feedList.clear()
                loadUserFeeds()
            }
        } else if (messageEvent is LoadOriginAvatarEvent) {
            // 用户浏览了头像大图，此时可以将个人主页的头像更新为清晰版。
            Glide.with(this)
                    .load(CustomUrl(mAvatar))
                    .skipMemoryCache(true)
                    .bitmapTransform(CropCircleTransformation(this))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.loading_bg_circle)
                    .error(R.drawable.avatar_default)
                    .into(userAvatar)
        } else {
            super.onMessageEvent(messageEvent)
        }
    }

    /**
     * 在删除Feed之后，更新用户的分享数量。
     */
    private fun updateFeedCountAfterDelete() {
        try {
            val content = userFeedsText.text.toString()
            val numbers = content.getNumbersFromString()
            var feedCount = Integer.parseInt(numbers)
            userFeedsText.text = String.format(GlobalUtil.getString(R.string.user_feeds_count), --feedCount)
        } catch (e: Exception) {
            logWarn(TAG, e.message, e)
        }

    }

    /**
     * 设置Toolbar和状态栏上的图标为深色。
     */
    private fun setToolbarAndStatusbarIconIntoDark() {
        ViewUtils.setLightStatusBar(window, userBgImage)
        toolbar?.let { ViewUtils.setToolbarIconColor(this, it, true) }
    }

    /**
     * 设置Toolbar和状态栏上的图标颜色为浅色。
     */
    private fun setToolbarAndStatusbarIconIntoLight() {
        ViewUtils.clearLightStatusBar(window, userBgImage)
        toolbar?.let { ViewUtils.setToolbarIconColor(this, it, false) }
    }

    private fun onFollowsFabClicked() {
        if (!isFollowInProgress) {
            if (isFollowed) {
                unfollowUser()
            } else {
                followUser()
            }
        }
    }

    private fun onEditFabClicked() {
        ModifyUserInfoActivity.actionStart(this)
    }

    private fun loadUserFeeds() {
        isLoading = true
        var lastFeed: Long = 0
        val isLoadMore: Boolean
        if (!feedList.isEmpty()) {
            lastFeed = feedList[feedList.size - 1].feedId
            isLoadMore = true
        } else {
            isLoadMore = false
        }
        FetchUserFeeds.getResponse(mUserId, lastFeed, object : Callback {
            override fun onResponse(response: Response) {
                if (activity == null) {
                    return
                }
                handleFetchedFeeds(response, isLoadMore)
                isLoading = false
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                loadFailed(null)
                if (!isLoadMore) {
                    ResponseHandler.handleFailure(e)
                }
                isLoading = false
            }
        })
    }

    /**
     *
     * 处理获取用户Feeds请求的返回结果。
     *
     * @param response
     * 服务器响应的获取用户Feeds请求的实体类。
     */
    private fun handleFetchedFeeds(response: Response, isLoadMore: Boolean) {
        isNoMoreData = false
        if (!ResponseHandler.handleResponse(response)) {
            val fetchUserFeeds = response as FetchUserFeeds
            val status = fetchUserFeeds.status
            when (status) {
                0 -> {
                    val feeds = fetchUserFeeds.feeds
                    if (!isLoadMore) {
                        showUserInformation(fetchUserFeeds)
                        feedList.clear()
                    } else {
                        recyclerView.stopScroll()
                    }
                    feedList.addAll(feeds)
                    adapter.notifyDataSetChanged()
                    loadFinished()
                }
                10004 -> {
                    if (!isLoadMore) {
                        showUserInformation(fetchUserFeeds)
                    }
                    isNoMoreData = true
                    adapter.notifyItemChanged(adapter.dataItemCount)
                    loadFinished()
                }
                else -> {
                    logWarn(TAG, "Load user feeds failed. " + GlobalUtil.getResponseClue(status, fetchUserFeeds.msg))
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
        if (feedList.isEmpty()) {
            progressBarLayout.visibility = View.GONE
            recyclerView.visibility = View.GONE
            if (isCurrentUserHomePage) {
                showNoContentView(GlobalUtil.getString(R.string.you_posts_nothing))
            } else {
                showNoContentView(GlobalUtil.getString(R.string.he_posts_nothing))
            }
            setAppBarLayoutCanDrag(false)
        } else {
            progressBarLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            setAppBarLayoutCanDrag(true)
        }
    }

    /**
     * 加载feeds失败，将加载等待控件隐藏。
     */
    override fun loadFailed(msg: String?) {
        super.loadFailed(msg)
        isLoadFailed = true
        progressBarLayout.visibility = View.GONE
        if (feedList.isEmpty()) {
            setAppBarLayoutCanDrag(false)
            if (msg == null) {
                val refresh = Runnable {
                    startLoading()
                    progressBarLayout.visibility = View.VISIBLE
                    loadUserFeeds()
                }
                showBadNetworkView(View.OnClickListener { GifFun.getHandler().postDelayed(refresh, 400) })
            } else {
                showLoadErrorView(msg)
            }
        } else {
            adapter.notifyItemChanged(adapter.itemCount - 1)
        }
    }

    /**
     * 将获取到的用户信息显示到界面上。
     */
    private fun showUserInformation(fetchUserFeeds: FetchUserFeeds) {
        userFeedsText.text = String.format(GlobalUtil.getString(R.string.user_feeds_count),
                GlobalUtil.getConvertedNumber(fetchUserFeeds.feedsCount))
        userFollowingsText.text = String.format(GlobalUtil.getString(R.string.user_followings_count),
                GlobalUtil.getConvertedNumber(fetchUserFeeds.followingsCount))
        userFollowersText.text = String.format(GlobalUtil.getString(R.string.user_followers_count),
                GlobalUtil.getConvertedNumber(fetchUserFeeds.followersCount))
        userCountsLayout.visibility = View.VISIBLE
        isFollowed = fetchUserFeeds.isFollowing
        mNickname = fetchUserFeeds.nickname
        mDescription = fetchUserFeeds.description
        mAvatar = fetchUserFeeds.avatar
        mBgImage = fetchUserFeeds.bgImage
        setupUserInfo()
        popFab()
    }

    /**
     * 使用pop动画的方式将fab按钮显示出来。
     */
    private fun popFab() {
        if (GifFun.getUserId() == mUserId) {
            fab.setImageResource(R.drawable.ic_edit)
            fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent))
        } else {
            if (isFollowed) {
                setFabFollowed()
            } else {
                setFabUnFollowed()
            }
        }
        fab.show()
        fab.alpha = 0f
        fab.scaleX = 0f
        fab.scaleY = 0f
        val animator = ObjectAnimator.ofPropertyValuesHolder(
                fab,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
        animator.startDelay = 200
        animator.start()
    }

    /**
     * 关注当前个人主页的用户。
     */
    private fun followUser() {
        setFabFollowed()
        setFollowersCountChange(1)
        isFollowInProgress = true
        FollowUser.getResponse(mUserId, object : Callback {
            override fun onResponse(response: Response) {
                isFollowInProgress = false
                if (activity == null) {
                    return
                }
                val status = response.status
                if (status == 0) {
                    isFollowed = true
                } else {
                    if (status == 10208) {
                        showToast(GlobalUtil.getString(R.string.follow_too_many))
                    } else {
                        showToast(GlobalUtil.getString(R.string.follow_failed))
                    }
                    setFabUnFollowed()
                    setFollowersCountChange(-1)
                }
            }

            override fun onFailure(e: Exception) {
                isFollowInProgress = false
                setFabUnFollowed()
                setFollowersCountChange(-1)
                showToast(GlobalUtil.getString(R.string.follow_failed))
            }
        })
    }

    /**
     * 取关当前个人主页的用户。
     */
    private fun unfollowUser() {
        val builder = AlertDialog.Builder(this, R.style.GifFunAlertDialogStyle)
        builder.setMessage(GlobalUtil.getString(R.string.unfollow_confirm))
        builder.setPositiveButton(GlobalUtil.getString(R.string.ok)) { _, _ ->
            setFabUnFollowed()
            setFollowersCountChange(-1)
            isFollowInProgress = true
            UnfollowUser.getResponse(mUserId, object : Callback {
                override fun onResponse(response: Response) {
                    isFollowInProgress = false
                    if (activity == null) {
                        return
                    }
                    if (response.status == 0) {
                        isFollowed = false
                    } else {
                        setFabFollowed()
                        setFollowersCountChange(1)
                        showToast(GlobalUtil.getString(R.string.unfollow_failed))
                    }
                }

                override fun onFailure(e: Exception) {
                    isFollowInProgress = false
                    setFabFollowed()
                    setFollowersCountChange(1)
                    showToast(GlobalUtil.getString(R.string.unfollow_failed))
                }
            })
        }
        builder.setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
        builder.create().show()
    }

    /**
     * 将当前界面的Fab按钮设置为已关注。
     */
    private fun setFabFollowed() {
        fab.setImageResource(R.drawable.ic_followed)
        fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary))
        isFabFollowed = true
        val event = FollowUserEvent()
        event.userId = mUserId
        event.type = FollowUserEvent.FOLLOW_USER
        EventBus.getDefault().post(event)
    }

    /**
     * 将当前界面的Fab按钮设置为未关注。
     */
    private fun setFabUnFollowed() {
        fab.setImageResource(R.drawable.ic_follow)
        fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent))
        isFabFollowed = false
        val event = FollowUserEvent()
        event.userId = mUserId
        event.type = FollowUserEvent.UNFOLLOW_USER
        EventBus.getDefault().post(event)
    }

    /**
     * 当关注状态发生变化的时候，刷新当前界面用户的粉丝数量。
     * @param changeCount
     * 用户最新的粉丝数量。
     */
    private fun setFollowersCountChange(changeCount: Int) {
        try {
            val followerInfo = userFollowersText.text.toString()
            val followerCount = Integer.parseInt(followerInfo.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            userFollowersText.text = String.format(GlobalUtil.getString(R.string.user_followers_count),
                    followerCount + changeCount)
        } catch (e: Exception) {
            logError(TAG, e.message, e)
        }

    }

    /**
     * 设置AppBarLayout是否可拖动。
     * @param canDrag
     * true表示可以拖动，false表示不可以。
     */
    private fun setAppBarLayoutCanDrag(canDrag: Boolean) {
        val params = appBar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior?
        behavior?.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return canDrag
            }
        })
    }

    companion object {

        private const val TAG = "UserHomePageActivity"

        const val USER_ID = "user_id"

        const val NICKNAME = "NICKNAME"

        const val AVATAR = "avatar"

        const val BG_IMAGE = "bg_image"

        fun actionStart(activity: Activity, image: View?, userId: Long, nickname: String, avatar: String, bgImage: String) {
            val intent = Intent(activity, UserHomePageActivity::class.java)
            intent.putExtra(USER_ID, userId)
            intent.putExtra(NICKNAME, nickname)
            intent.putExtra(AVATAR, avatar)
            intent.putExtra(BG_IMAGE, bgImage)
            activity.startActivity(intent)

            //        if (AndroidVersion.hasLollipop()) {
            //            ActivityOptions options =
            //                    ActivityOptions.makeSceneTransitionAnimation(activity,
            //                            Pair.create(image, GlobalUtil.getString(R.string.transition_user_home_page_avatar)));
            //
            //            activity.startActivity(intent, options.toBundle());
            //        } else {
            //            activity.startActivity(intent);
            //        }
        }
    }

}
