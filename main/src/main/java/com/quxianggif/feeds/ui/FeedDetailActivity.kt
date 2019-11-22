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

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.transition.Transition
import android.util.Pair
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.quxianggif.R
import com.quxianggif.common.callback.PermissionListener
import com.quxianggif.common.callback.SimpleTransitionListener
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.common.ui.ShareDialogFragment
import com.quxianggif.common.view.CheckableImageButton
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logDebug
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.extension.showToastOnUiThread
import com.quxianggif.core.model.BaseFeed
import com.quxianggif.core.model.Comment
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.core.util.ImageUtil
import com.quxianggif.core.util.NetworkUtil
import com.quxianggif.event.*
import com.quxianggif.feeds.adapter.FeedDetailMoreAdapter
import com.quxianggif.network.model.*
import com.quxianggif.report.ReportActivity
import com.quxianggif.settings.ui.SettingsActivity
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.*
import com.quxianggif.util.glide.CustomUrl
import com.quxianggif.util.glide.GifPlayTarget
import com.quxianggif.util.glide.GlideUtil
import com.quxianggif.util.glide.ProgressListener
import com.umeng.analytics.MobclickAgent
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_feed_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream

/**
 * Feed详情页面，在这里显示GIF图、评论、点赞等内容。
 *
 * @author guolin
 * @since 17/6/1
 */
class FeedDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mFeed: BaseFeed

    private var targetImgWidth: Int = 0

    private var targetImgHeight: Int = 0

    private lateinit var detailInfo: ViewGroup

    private lateinit var likes: ImageView

    private lateinit var likesText: TextView

    private lateinit var feedContentLayout: LinearLayout

    private var followsButton: Button? = null

    private lateinit var adapter: FeedDetailMoreAdapter

    private lateinit var preferences: SharedPreferences

    private lateinit var gifPlayTarget: GifPlayTarget

    private lateinit var mGifUrl: String

    private var likesCount: Int = 0

    private var isLiked: Boolean = false

    private var isFollowing: Boolean = false

    private var isFollowInProgress: Boolean = false

    private var isFeedDetailLoaded: Boolean = false

    private var isForwardAndDoNotAlertAgainChecked = false

    private var gifLoadStatus = GIF_LOADING

    /**
     * 记录是否循环播放设置的原始值。
     */
    private var loopForeverInit: Boolean = false

    /**
     * 记录上次的是否循环播放设置的值。
     */
    private var loopForeverLast: Boolean = false

    private var loopForever: Boolean = false

    /**
     * 记录GIF播放速度的原始值。
     */
    private var gifPlaySpeedInit: String = ""

    /**
     * 记录上次GIF播放速度的值。
     */
    private var gifPlaySpeedLast: String = ""

    private var gifPlaySpeed: String = ""

    private var fabOffset = 0

    /**
     * 判断当前Feed是否是当前用户本人所发。
     *
     * @return 如果是本人所发返回true，否则返回false。
     */
    private val isCurrentUserPost: Boolean
        get() = GifFun.getUserId() == mFeed.userId

    private val scrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val scrollY = detailInfo.top
            feedGif.offset = scrollY
            shareFab.setOffset(fabOffset + scrollY)

            val gifVisibleHeight = targetImgHeight + feedGif.offset
            val imageBackgroundParams = gifBackground.layoutParams
            imageBackgroundParams.height = gifVisibleHeight
            gifBackground.layoutParams = imageBackgroundParams
            val gifImageLayoutParams = gifFrontLayout.layoutParams
            gifImageLayoutParams.height = gifVisibleHeight
            gifFrontLayout.layoutParams = gifImageLayoutParams
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            feedGif.isImmediatePin = newState == RecyclerView.SCROLL_STATE_SETTLING
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                rootLayout.requestFocus()
                hideSoftKeyboard()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_detail)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        loopForeverInit = preferences.getBoolean(getString(R.string.key_loop_gif_play), true)
        loopForeverLast = loopForeverInit
        gifPlaySpeedInit = preferences.getString(getString(R.string.key_gif_play_speed), "3") ?: ""
        gifPlaySpeedLast = gifPlaySpeedInit
    }

    override fun onResume() {
        super.onResume()
        loopForever = preferences.getBoolean(getString(R.string.key_loop_gif_play), true)
        gifPlaySpeed = preferences.getString(getString(R.string.key_gif_play_speed), "3") ?: ""
        if (loopForever != loopForeverLast || gifPlaySpeed != gifPlaySpeedLast) {
            fetchGifUrl() // 当对GIF播放设置做成更改时，需要重新加载GIF图
            loopForeverLast = loopForever
            gifPlaySpeedLast = gifPlaySpeed
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.feedGif ->
                // 点击图片可以暂停和继续GIF图播放
                if (gifLoadStatus == GIF_LOAD_SUCCESS) {
                    if (gifPlayTarget.isRunning) {
                        gifPlayTarget.pausePlaying()
                    } else {
                        gifPlayTarget.resumePlaying()
                    }
                } else if (gifLoadStatus == GIF_FETCH_URL_FAILED) {
                    fetchGifUrl()
                } else if (gifLoadStatus == GIF_LOAD_FAILED) {
                    gifFrontLayout.visibility = View.VISIBLE
                    loadGif(mGifUrl)
                }
            R.id.shareFab -> {
                if (gifLoadStatus != GIF_LOAD_SUCCESS) {
                    showToast(getString(R.string.unable_to_share_before_gif_loaded))
                } else if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                    showToast(getString(R.string.unable_to_share_without_sdcard))
                } else {
                    val gifCacheFile = GlideUtil.getCacheFile(mFeed.gif)
                    if (gifCacheFile != null) {
                        ShareDialogFragment().showDialog(this, gifCacheFile.path)
                    } else {
                        showToast(getString(R.string.unable_to_share_before_gif_loaded))
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_feed_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                exit()
                return true
            }
            R.id.gif_play_control -> {
                SettingsActivity.actionStartGIFSettings(this)
                return true
            }
            R.id.save_to_phone -> {
                if (AndroidVersion.hasQ()) {
                    saveGifToSDCard()
                } else {
                    handlePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionListener {
                        override fun onGranted() {
                            saveGifToSDCard()
                        }

                        override fun onDenied(deniedPermissions: List<String>) {
                            var allNeverAskAgain = true
                            for (deniedPermission in deniedPermissions) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this@FeedDetailActivity, deniedPermission)) {
                                    allNeverAskAgain = false
                                    break
                                }
                            }
                            // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                            if (allNeverAskAgain) {
                                val dialog = AlertDialog.Builder(this@FeedDetailActivity, R.style.GifFunAlertDialogStyle)
                                        .setMessage(GlobalUtil.getString(R.string.allow_storage_permission_please))
                                        .setPositiveButton(GlobalUtil.getString(R.string.settings)) { _, _ ->
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            val uri = Uri.fromParts("package", GlobalUtil.appPackage, null)
                                            intent.data = uri
                                            activity!!.startActivityForResult(intent, SelectGifActivity.REQUEST_PERMISSION_SETTING)
                                        }
                                        .setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
                                        .create()
                                dialog.show()
                            } else {
                                showToast(GlobalUtil.getString(R.string.must_agree_permission_to_save))
                            }
                        }
                    })
                }
                return true
            }
            R.id.report -> {
                ReportActivity.actionReportFeed(this, mFeed.feedId)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        exit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is DeleteFeedEvent) {
            val feedId = messageEvent.feedId
            if (feedId == mFeed.feedId) {
                finish()
            }
        } else if (messageEvent is GoodCommentEvent) {
            adapter.refreshCommentsGood(messageEvent.commentId, messageEvent.type, messageEvent.goodsCount)
        } else if (messageEvent is DeleteCommentEvent) {
            adapter.deleteHotComment(messageEvent.commentId)
        } else {
            super.onMessageEvent(messageEvent)
        }
    }

    override fun setupViews() {
        if (AndroidVersion.hasLollipop()) {
            postponeEnterTransition()
        }
        if (loadFeed()) {
            setupToolbar()
            title = ""

            calculateImageSize()
            feedGif.layoutParams.width = targetImgWidth
            feedGif.layoutParams.height = targetImgHeight
            gifBackground.layoutParams.height = targetImgHeight
            gifFrontLayout.layoutParams.height = targetImgHeight

            // setup RecyclerView
            val layoutManager = LinearLayoutManager(this)
            detailRecyclerView.layoutManager = layoutManager
            detailRecyclerView.setHasFixedSize(true)
            setupDetailView()
            adapter = FeedDetailMoreAdapter(this, detailInfo, mFeed)
            detailRecyclerView.adapter = adapter
            detailRecyclerView.addOnScrollListener(scrollListener)
            feedGif.setOnClickListener(this)
            shareFab.setOnClickListener(this)

            Glide.with(this)
                    .load(mFeed.cover)
                    .priority(Priority.IMMEDIATE)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(object : RequestListener<String, GlideDrawable> {
                        override fun onException(e: Exception?, model: String, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                            if (e != null) {
                                logWarn(TAG, e.message, e)
                            } else {
                                logWarn(TAG, "Load cover failed with exception null. url is ${mFeed.cover}")
                            }
                            showToast(GlobalUtil.getString(R.string.load_image_failed))
                            finishSelf()
                            return true
                        }

                        override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                            return false
                        }
                    })
                    .into(object : SimpleTarget<GlideDrawable>(targetImgWidth, targetImgHeight) {
                        override fun onResourceReady(resource: GlideDrawable,
                                                     glideAnimation: GlideAnimation<in GlideDrawable>) {
                            feedGif.setImageDrawable(resource)
                            if (AndroidVersion.hasLollipop()) {
                                startPostponedEnterTransition()
                                window.statusBarColor = ContextCompat.getColor(this@FeedDetailActivity, R.color.black)
                            }
                            fetchGifUrl()
                        }
                    })

            if (AndroidVersion.hasLollipop()) {
                window.enterTransition.addListener(object : SimpleTransitionListener() {
                    override fun onTransitionEnd(transition: Transition) {
                        shareFab.setOffset(fabOffset)
                        popFab()
                        fetchFeedDetails()
                    }
                })
            } else {
                fetchFeedDetails()
            }
            shareFab.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    shareFab.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    calculateFabPosition()
                }
            })
            MobclickAgent.onEvent(this, "10003")
        } else {
            finish()
        }
    }

    /**
     * 读取传入的Feed实例。
     * @return 读取成功返回true，失败返回false。
     */
    private fun loadFeed(): Boolean {
        val feed = intent.getParcelableExtra<BaseFeed>(FEED)
        if (feed != null) {
            mFeed = feed
            return true
        }
        showToast(GlobalUtil.getString(R.string.load_feed_failed))
        return false
    }

    private fun calculateImageSize() {
        val imgWidth = mFeed.imgWidth
        val imgHeight = mFeed.imgHeight
        val screenWidth = DeviceInfo.screenWidth
        val screenHeight = DeviceInfo.screenHeight
        targetImgWidth = screenWidth
        targetImgHeight = screenWidth * imgHeight / imgWidth
        val maxImgHeight = screenHeight * 0.65
        if (targetImgHeight > maxImgHeight) {
            targetImgHeight = maxImgHeight.toInt()
            targetImgWidth = imgWidth * targetImgHeight / imgHeight
        }
    }

    private fun calculateFabPosition() {
        fabOffset = feedGif.height + feedContentLayout.height - (shareFab.height / 2)
        shareFab.setOffset(fabOffset)
        shareFab.setMinOffset(feedGif.minimumHeight - (shareFab.height / 2))
    }

    /**
     * 使用pop动画的方式将fab按钮显示出来。
     */
    private fun popFab() {
        shareFab.show()
        shareFab.alpha = 0f
        shareFab.scaleX = 0f
        shareFab.scaleY = 0f
        val animator = ObjectAnimator.ofPropertyValuesHolder(
                shareFab,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
        animator.startDelay = 300
        animator.start()
    }

    private fun setupDetailView() {
        detailInfo = layoutInflater.inflate(R.layout.feed_detail_info, detailRecyclerView, false) as ViewGroup

        val spacingView = detailInfo.findViewById<View>(R.id.spacingView)
        val favoriteLayout = detailInfo.findViewById<LinearLayout>(R.id.favoriteLayout)
        likes = favoriteLayout.findViewById(R.id.likes)
        likesText = favoriteLayout.findViewById(R.id.likesText)
        feedContentLayout = detailInfo.findViewById(R.id.feedContentLayout)
        val refeedLayout = detailInfo.findViewById<LinearLayout>(R.id.refeedLayout)
        val commentLayout = detailInfo.findViewById<LinearLayout>(R.id.commentLayout)
        val feedContent = detailInfo.findViewById<TextView>(R.id.feedContent)

        val viewStub: ViewStub? = if (isCurrentUserPost) {
            detailInfo.findViewById(R.id.feedDetailMe)
        } else {
            detailInfo.findViewById(R.id.feedDetailUser)
        }

        if (viewStub != null) {
            val userDetailRootLayout = viewStub.inflate()
            val userLayout = userDetailRootLayout.findViewById<LinearLayout>(R.id.userLayout)
            val avatar = userDetailRootLayout.findViewById<ImageView>(R.id.avatar)
            val nickname = userDetailRootLayout.findViewById<TextView>(R.id.nickname)
            val postDate = userDetailRootLayout.findViewById<TextView>(R.id.postDate)
            if (!isCurrentUserPost) {
                followsButton = userDetailRootLayout.findViewById(R.id.followsButton)
            }

            Glide.with(this)
                    .load(CustomUrl(mFeed.avatar))
                    .placeholder(R.drawable.loading_bg_circle)
                    .error(R.drawable.avatar_default)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(CropCircleTransformation(this))
                    .into(avatar)
            nickname.text = mFeed.nickname
            postDate.text = DateUtil.getConvertedDate(mFeed.postDate)

            val onUserClick = View.OnClickListener { UserHomePageActivity.actionStart(this@FeedDetailActivity, avatar, mFeed.userId, mFeed.nickname, mFeed.avatar, mFeed.bgImage) }
            userLayout.setOnClickListener(onUserClick)
            postDate.setOnClickListener(onUserClick)
            avatar.setOnClickListener(onUserClick)
        }

        spacingView.layoutParams.height = targetImgHeight
        if (AndroidVersion.hasLollipop()) {
            val imageButton = likes as CheckableImageButton
            imageButton.isChecked = mFeed.isLikedAlready
        } else {
            if (mFeed.isLikedAlready) {
                likes.setImageResource(R.drawable.ic_liked)
            } else {
                likes.setImageResource(R.drawable.ic_like)
            }
        }
        likes.isClickable = false
        likesText.text = String.format(getString(R.string.number_likes), GlobalUtil.getConvertedNumber(mFeed.likesCount))
        feedContent.text = mFeed.content

        commentLayout.setOnClickListener {
            detailRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
            adapter.showSoftKeyboard()
        }
        favoriteLayout.setOnClickListener {
            if (isFeedDetailLoaded) {
                likeFeed()
            }
        }
        refeedLayout.setOnClickListener { RepostFeedActivity.actionStart(activity!!, mFeed) }
        followsButton?.setOnClickListener {
            if (!isFollowInProgress) {
                if (isFollowing) {
                    unfollowUser()
                } else {
                    followUser()
                }
            }
        }
    }

    /**
     * 关注用户。
     */
    private fun followUser() {
        followsButton?.setBackgroundResource(R.drawable.followed_button_bg)
        isFollowInProgress = true
        FollowUser.getResponse(mFeed.userId, object : Callback {
            override fun onResponse(response: Response) {
                isFollowInProgress = false
                if (activity == null) {
                    return
                }
                val status = response.status
                if (status == 0) {
                    isFollowing = true
                } else {
                    if (status == 10208) {
                        showToast(GlobalUtil.getString(R.string.follow_too_many))
                    } else {
                        showToast(GlobalUtil.getString(R.string.follow_failed))
                    }
                    followsButton?.setBackgroundResource(R.drawable.follow_button_bg)
                }
            }

            override fun onFailure(e: Exception) {
                isFollowInProgress = false
                followsButton?.setBackgroundResource(R.drawable.follow_button_bg)
                showToast(GlobalUtil.getString(R.string.follow_failed))
            }
        })
    }

    /**
     * 取消对用户的关注。
     */
    private fun unfollowUser() {
        val builder = AlertDialog.Builder(this, R.style.GifFunAlertDialogStyle)
        builder.setMessage(GlobalUtil.getString(R.string.unfollow_confirm))
        builder.setPositiveButton(GlobalUtil.getString(R.string.ok)) { _, _ ->
            followsButton?.setBackgroundResource(R.drawable.follow_button_bg)
            isFollowInProgress = true
            UnfollowUser.getResponse(mFeed.userId, object : Callback {
                override fun onResponse(response: Response) {
                    isFollowInProgress = false
                    if (activity == null) {
                        return
                    }
                    if (response.status == 0) {
                        isFollowing = false
                    } else {
                        followsButton?.setBackgroundResource(R.drawable.followed_button_bg)
                        showToast(GlobalUtil.getString(R.string.unfollow_failed))
                    }
                }

                override fun onFailure(e: Exception) {
                    isFollowInProgress = false
                    followsButton?.setBackgroundResource(R.drawable.followed_button_bg)
                    showToast(GlobalUtil.getString(R.string.unfollow_failed))
                }
            })
        }
        builder.setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
        builder.create().show()
    }

    /**
     * 对Feed进行点赞或取消点赞。
     */
    private fun likeFeed() {
        var likesCount = mFeed.likesCount
        val event = LikeFeedEvent()
        event.from = LikeFeedEvent.FROM_FEED_DETAIL
        event.feedId = mFeed.feedId
        if (mFeed.isLikedAlready) {
            mFeed.isLikedAlready = false
            if (AndroidVersion.hasLollipop()) {
                val imageButton = likes as CheckableImageButton?
                imageButton?.isChecked = false
            } else {
                likes.setImageResource(R.drawable.ic_like)
            }
            likesCount--
            if (likesCount < 0) {
                likesCount = 0
            }
            event.type = LikeFeedEvent.UNLIKE_FEED
        } else {
            mFeed.isLikedAlready = true
            if (AndroidVersion.hasLollipop()) {
                val imageButton = likes as CheckableImageButton?
                imageButton?.isChecked = true
            } else {
                likes.setImageResource(R.drawable.ic_liked)
            }
            likesCount++
            event.type = LikeFeedEvent.LIKE_FEED
        }
        event.likesCount = likesCount
        EventBus.getDefault().post(event)
        mFeed.likesCount = likesCount
        likesText.text = String.format(getString(R.string.number_likes), GlobalUtil.getConvertedNumber(likesCount))
        LikeFeed.getResponse(mFeed.feedId, null)
    }

    private fun showFeedDetailInfo() {
        if (!isCurrentUserPost && followsButton != null) { // 浏览自己所发Feed时不会显示关注按钮
            followsButton?.visibility = View.VISIBLE
            if (isFollowing) {
                followsButton?.setBackgroundResource(R.drawable.followed_button_bg)
            } else {
                followsButton?.setBackgroundResource(R.drawable.follow_button_bg)
            }
            AnimUtils.popAnim(followsButton, 0, 100)
        }
        var feedChanged = false
        if (likesCount != mFeed.likesCount) {
            feedChanged = true
            mFeed.likesCount = likesCount
            likesText.text = String.format(getString(R.string.number_likes), GlobalUtil.getConvertedNumber(likesCount))
        }
        if (isLiked != mFeed.isLikedAlready) {
            feedChanged = true
            mFeed.isLikedAlready = isLiked
            if (AndroidVersion.hasLollipop()) {
                val imageButton = likes as CheckableImageButton?
                imageButton?.isChecked = mFeed.isLikedAlready
            } else {
                if (mFeed.isLikedAlready) {
                    likes.setImageResource(R.drawable.ic_liked)
                } else {
                    likes.setImageResource(R.drawable.ic_like)
                }
            }
        }
        if (feedChanged) {
            val event = LikeFeedEvent()
            event.from = LikeFeedEvent.FROM_FEED_DETAIL
            event.feedId = mFeed.feedId
            event.likesCount = likesCount
            event.type = if (isLiked) LikeFeedEvent.LIKE_FEED else LikeFeedEvent.UNLIKE_FEED
            EventBus.getDefault().post(event)
        }
    }

    /**
     * 获取当前Feed的详情。
     */
    fun fetchFeedDetails() {
        FetchFeedDetails.getResponse(mFeed.feedId, object : Callback {
            override fun onResponse(response: Response) {
                if (activity == null) {
                    return
                }
                isFeedDetailLoaded = true
                if (!ResponseHandler.handleResponse(response)) {
                    val status = response.status
                    if (status == 0) {
                        val fetchFeedDetails = response as FetchFeedDetails
                        val comments = fetchFeedDetails.comments
                        adapter.setHotComments(comments as MutableList<Comment>?)
                        likesCount = fetchFeedDetails.likesCount
                        isLiked = fetchFeedDetails.isLiked
                        isFollowing = fetchFeedDetails.isFollowing
                        showFeedDetailInfo()
                    } else if (status == 10305) {
                        showToast(GlobalUtil.getString(R.string.feed_is_deleted))
                        finishSelf()
                    } else {
                        adapter.setHotComments(null)
                        showToast(GlobalUtil.getString(R.string.fetch_feed_details_failed) + ": " + status)
                        logWarn(TAG, "fetch feed details failed. " + GlobalUtil.getResponseClue(status, response.msg))
                    }
                } else {
                    adapter.setHotComments(null)
                    showToast(GlobalUtil.getString(R.string.fetch_feed_details_failed) + ": " + response.status)
                    logWarn(TAG, "fetch feed details failed. code is " + response.status)
                }
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                isFeedDetailLoaded = true
                adapter.setHotComments(null)
                ResponseHandler.handleFailure(e)
            }
        })
    }

    private fun fetchGifUrl() {
        val url = mFeed.gif
        if (GlideUtil.isSourceCached(url)) {
            loadGif(url)
        } else {
            val network = NetworkUtil.checkNetwork()
            if (network == NetworkUtil.MOBILE) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@FeedDetailActivity)
                val alertForBigGif = prefs.getBoolean(getString(R.string.key_alert_for_big_gif), true)
                logDebug(TAG, "alert for big gif " + alertForBigGif + " , gif size is " + mFeed.fsize)
                val isBigGif = mFeed.fsize > 4 * 1024 * 1024 /* 大于4M的GIF图片被视为大图 */
                if (alertForBigGif && isBigGif) {
                    val dialogView = LayoutInflater.from(this@FeedDetailActivity).inflate(R.layout.dialog_alert_for_big_gif, null)
                    val checkBox = dialogView.findViewById<CheckBox>(R.id.checkbox)
                    val dialog = AlertDialog.Builder(this, R.style.GifFunAlertDialogStyle)
                            .setTitle(GlobalUtil.getString(R.string.remind))
                            .setMessage(GlobalUtil.getString(R.string.you_are_playing_big_gif_with_roaming))
                            .setView(dialogView)
                            .setPositiveButton(GlobalUtil.getString(R.string.forward)) { _, _ ->
                                if (isForwardAndDoNotAlertAgainChecked) {
                                    prefs.edit().putBoolean(getString(R.string.key_alert_for_big_gif), false).apply()
                                }
                                sendFetchGifUrlRequest(url)
                            }
                            .setNegativeButton(GlobalUtil.getString(R.string.cancel)) { _, _ -> finishSelf() }
                            .setOnCancelListener { finishSelf() }
                            .create()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        isForwardAndDoNotAlertAgainChecked = isChecked
                    }
                } else {
                    sendFetchGifUrlRequest(url)
                }
            } else {
                sendFetchGifUrlRequest(url)
            }
        }
    }

    private fun sendFetchGifUrlRequest(url: String) {
        gifFrontLayout.visibility = View.VISIBLE
        popProgressView()
        AuthorizeUrl.getResponse(url, mFeed.feedId, object : Callback {
            override fun onResponse(response: Response) {
                if (activity == null) {
                    return
                }
                if (!ResponseHandler.handleResponse(response)) {
                    val status = response.status
                    if (status == 0) {
                        val authorizeUrl = response as AuthorizeUrl
                        val gifUrl = authorizeUrl.authorizeUrl
                        logDebug(TAG, "gif url is $gifUrl")
                        loadGif(gifUrl)
                    } else {
                        gifFrontLayout.visibility = View.INVISIBLE
                        logWarn(TAG, "Load gif failed. " + GlobalUtil.getResponseClue(status, response.msg))
                        showToast(GlobalUtil.getString(R.string.load_gif_failed))
                    }
                } else {
                    gifFrontLayout.visibility = View.INVISIBLE
                    showToast(GlobalUtil.getString(R.string.load_gif_failed))
                    logWarn(TAG, "Load gif failed. code is " + response.status)
                }
            }

            override fun onFailure(e: Exception) {
                gifLoadStatus = GIF_FETCH_URL_FAILED
                gifFrontLayout.visibility = View.INVISIBLE
                ResponseHandler.handleFailure(e)
            }
        })
    }

    /**
     * 使用pop动画的方式将fab按钮显示出来。
     */
    private fun popProgressView() {
        gifProgressView.visibility = View.VISIBLE
        gifProgressView.alpha = 0f
        gifProgressView.scaleX = 0f
        gifProgressView.scaleY = 0f
        val animator = ObjectAnimator.ofPropertyValuesHolder(
                gifProgressView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
        animator.startDelay = 200
        animator.start()
    }

    private fun loadGif(gifUrl: String) {
        mGifUrl = gifUrl
        val loopForever = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_loop_gif_play), true)
        gifPlayTarget = GifPlayTarget(feedGif, loopForever)
        gifPlayTarget.setGifPlaySpeed(gifPlaySpeed)
        gifPlayTarget.setProgressListener(gifUrl, object : ProgressListener {
            override fun onProgress(progress: Int) {
                gifProgressView.setProgress(progress)
            }
        })

        Glide.with(this)
                .load(CustomUrl(gifUrl))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(feedGif.drawable)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .listener(object : RequestListener<CustomUrl, GlideDrawable> {
                    override fun onException(e: Exception?, model: CustomUrl, target: Target<GlideDrawable>,
                                             isFirstResource: Boolean): Boolean {
                        logWarn(TAG, "Glide load gif failed: $gifUrl")
                        if (e != null) {
                            logWarn(TAG, e.message, e)
                        } else {
                            logWarn(TAG, "Load gif failed with exception null. url is $gifUrl")
                        }
                        gifLoadStatus = GIF_LOAD_FAILED
                        gifFrontLayout.visibility = View.INVISIBLE
                        showToast(GlobalUtil.getString(R.string.load_gif_failed))
                        return true
                    }

                    override fun onResourceReady(resource: GlideDrawable, model: CustomUrl, target: Target<GlideDrawable>,
                                                 isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        gifLoadStatus = GIF_LOAD_SUCCESS
                        gifFrontLayout.visibility = View.INVISIBLE
                        return false
                    }
                })
                .into<GifPlayTarget>(gifPlayTarget)
    }

    private fun finishSelf() {
        if (AndroidVersion.hasLollipop()) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    private fun saveGifToSDCard() {
        Thread(Runnable {
            try {
                if (gifLoadStatus != GIF_LOAD_SUCCESS) {
                    showToastOnUiThread(getString(R.string.unable_to_save_before_gif_loaded))
                    return@Runnable
                }
                saveGifToAlbum()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun saveGifToAlbum() {
        val gifCacheFile = GlideUtil.getCacheFile(mFeed.gif)
        if (gifCacheFile == null || !gifCacheFile.exists()) {
            showToastOnUiThread(GlobalUtil.getString(R.string.gif_file_not_exist))
            return
        }
        val name = GlobalUtil.currentDateString + ".gif"
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/gif")
        if (AndroidVersion.hasQ()) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        } else {
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$name")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            logWarn(TAG, "uri is null")
            showToastOnUiThread(GlobalUtil.getString(R.string.save_failed))
            return
        }
        val outputStream = contentResolver.openOutputStream(uri)
        if (outputStream == null) {
            logWarn(TAG, "outputStream is null")
            showToastOnUiThread(GlobalUtil.getString(R.string.save_failed))
            return
        }
        val fis = FileInputStream(gifCacheFile)
        val bis = BufferedInputStream(fis)
        val bos = BufferedOutputStream(outputStream)
        val buffer = ByteArray(1024)
        var bytes = bis.read(buffer)
        while (bytes >= 0) {
            bos.write(buffer, 0 , bytes)
            bos.flush()
            bytes = bis.read(buffer)
        }
        bos.close()
        bis.close()
        showToastOnUiThread(GlobalUtil.getString(R.string.save_gif_to_album_success), Toast.LENGTH_LONG)
    }

    /**
     * 通过点击返回键或者返回按钮退出时，检查GIF播放设置是否有变更，如果有变更则弹出对话框询问用户是否保存更改。
     */
    private fun exit() {
        if (loopForever != loopForeverInit || gifPlaySpeed != gifPlaySpeedInit) {
            val builder = AlertDialog.Builder(this, R.style.GifFunAlertDialogStyle)
            builder.setMessage(GlobalUtil.getString(R.string.save_gif_play_control_modify_or_not))
            builder.setPositiveButton(GlobalUtil.getString(R.string.save)) { _, _ ->
                // 用户选择保存则不用做任何事情，直接finish即可
                finishSelf()
            }
            builder.setNegativeButton(GlobalUtil.getString(R.string.ignore)) { _, _ ->
                // 用户选择忽略，则需要将设置改回初始值
                PreferenceManager.getDefaultSharedPreferences(this@FeedDetailActivity)
                        .edit()
                        .putBoolean(getString(R.string.key_loop_gif_play), loopForeverInit)
                        .putString(getString(R.string.key_gif_play_speed), gifPlaySpeedInit)
                        .apply()
                finishSelf()
            }
            builder.create().show()
        } else {
            finishSelf()
        }
    }

    companion object {

        private const val TAG = "FeedDetailActivity"

        const val FEED = "feed"

        private const val GIF_LOADING = 0

        private const val GIF_LOAD_SUCCESS = 1

        private const val GIF_FETCH_URL_FAILED = 2

        private const val GIF_LOAD_FAILED = 3

        fun actionStart(activity: Activity, image: View, feed: BaseFeed) {
            val intent = Intent(activity, FeedDetailActivity::class.java)
            intent.putExtra(FEED, feed)

            if (AndroidVersion.hasLollipop()) {
                val options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        Pair.create(image, GlobalUtil.getString(R.string.transition_feed_detail)),
                        Pair.create(image, GlobalUtil.getString(R.string.transition_feed_detail_bg)),
                        Pair.create(image, GlobalUtil.getString(R.string.transition_feed_detail_image_bg)))

                activity.startActivity(intent, options.toBundle())
            } else {
                activity.startActivity(intent)
            }
        }
    }

}
