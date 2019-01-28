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

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quxianggif.R
import com.quxianggif.common.ui.AlbumActivity
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.extension.showToastOnUiThread
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.DiscardDraftEvent
import com.quxianggif.event.FinishActivityEvent
import com.quxianggif.event.RefreshFollowingFeedsEvent
import com.quxianggif.event.SaveDraftEvent
import com.quxianggif.feeds.model.Draft
import com.quxianggif.network.exception.PostFeedException
import com.quxianggif.network.model.PostFeed
import com.quxianggif.network.model.ProgressCallback
import com.quxianggif.network.model.Response
import com.quxianggif.util.DateUtil
import com.quxianggif.util.DeviceInfo
import com.quxianggif.util.ResponseHandler
import com.quxianggif.util.glide.GifPlayTarget
import com.quxianggif.util.glide.GlideUtil
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.activity_post_feed.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * 编辑和分享GIF图的界面，用户可以在这里选择和预览要分享的GIF图片，以及为图片编辑说明文字，然后分享出去。
 *
 * @author guolin
 * @since 17/3/20
 */
class PostFeedActivity : BaseActivity(), View.OnClickListener, TextView.OnEditorActionListener {

    private lateinit var postFeedProgress: ProgressDialog

    var overrideWidth: Int = 0

    var overrideHeight: Int = 0

    private var gifPlayTarget: GifPlayTarget? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_feed)
        val draft: Draft? = intent.getParcelableExtra(INTENT_DRAFT)
        if (draft != null) {
            val gifPath = draft.gifPath
            val content = draft.content
            if (gifPath.isNotBlank()) {
                preloadSelectedGif(gifPath)
            }
            if (content.isNotBlank()) {
                contentEdit.setText(content)
                contentEdit.setSelection(content.length)
            }
        }
    }

    /**
     * 初始化布局控件。
     */
    override fun setupViews() {
        setupToolbar()
        chooseGifLayout.setOnClickListener(this)
        closeButton.setOnClickListener(this)
        selectedGif.setOnClickListener(this)
        contentEdit.setOnEditorActionListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            chooseGifLayout.id -> SelectGifActivity.actionStartForResult(this, PICK_GIF)
            closeButton.id -> hideGifLayout()
            selectedGif.id ->
                // 点击图片可以暂停和继续GIF图播放
                gifPlayTarget?.let {
                    if (it.isRunning) {
                        it.pausePlaying()
                    } else {
                        it.resumePlaying()
                    }
                }
            else -> {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_post_feed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.post_feed -> {
                postFeed()
                return true
            }
            android.R.id.home -> {
                exit()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_GIF -> if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val path = data.getStringExtra(AlbumActivity.IMAGE_PATH)
                    // 选择好GIF图之后，开始对图片进行预加载
                    preloadSelectedGif(path)
                }
            }
            else -> {
            }
        }
    }

    override fun onEditorAction(textView: TextView, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            postFeed()
        }
        return false
    }

    override fun onBackPressed() {
        exit()
    }

    private fun exit() {
        if (contentEdit.text.toString().isNotBlank() || gifPlayTarget?.gifPath != null) {
            val dialog = AlertDialog.Builder(this, R.style.GifFunAlertDialogStyle)
                    .setMessage(GlobalUtil.getString(R.string.save_to_draft_or_not))
                    .setPositiveButton(GlobalUtil.getString(R.string.save)) { _, _ ->
                        var gifPath = gifPlayTarget?.gifPath
                        if (gifPath == null) {
                            gifPath = ""
                        }
                        val draft = Draft(gifPath, contentEdit.text.toString(), Date())
                        draft.save()
                        val event = SaveDraftEvent()
                        EventBus.getDefault().post(event)
                        finish()
                        showToast(GlobalUtil.getString(R.string.save_to_draft_success))
                    }
                    .setNegativeButton(GlobalUtil.getString(R.string.give_up)) { _, _ ->
                        val event = DiscardDraftEvent()
                        EventBus.getDefault().post(event)
                        finish()
                    }
                    .create()
            dialog.show()
        } else {
            finish()
        }
    }

    /**
     * 分享GIF图，将GIF图发送到服务器
     */
    private fun postFeed() {
        if (gifPlayTarget == null) {
            showToast(GlobalUtil.getString(R.string.select_a_gif))
            return
        }
        gifPlayTarget?.let {
            val feedContent = contentEdit.text.toString().trim()
            if (feedContent.isBlank()) {
                showToast(GlobalUtil.getString(R.string.say_something))
                return
            }
            hideSoftKeyboard()
            postFeedProgress = ProgressDialog(this).apply {
                setTitle(GlobalUtil.getString(R.string.posting))
                setMessage(GlobalUtil.getString(R.string.posting_please_wait))
                setCancelable(false)
                show()
            }
            if (it.gifPath == null || it.firstFrame == null) {
                return
            }
            PostFeed.getResponse(it.gifPath, feedContent, it.firstFrame, object : ProgressCallback {
                override fun onProgress(percent: Double) {
                    val percentInt = (percent * 100).toInt()
                    postFeedProgress.setMessage(String.format(GlobalUtil.getString(R.string.uploading_gif), percentInt))
                }

                override fun onResponse(response: Response) {
                    postFeedProgress.dismiss()
                    if (activity == null) {
                        return
                    }
                    if (!ResponseHandler.handleResponse(response)) {
                        val postFeed = response as PostFeed
                        val status = postFeed.status
                        if (status == 0) {
                            val coverUrl = postFeed.coverUrl
                            val gifUrl = postFeed.gifUrl
                            GlideUtil.saveBitmapToCache(it.firstFrame, coverUrl)
                            GlideUtil.saveImagePathToCache(it.gifPath, gifUrl)
                            val refreshFollowingFeedsEvent = RefreshFollowingFeedsEvent()
                            EventBus.getDefault().post(refreshFollowingFeedsEvent)
                            val finishActivityEvent = FinishActivityEvent()
                            finishActivityEvent.activityClass = DraftActivity::class.java
                            EventBus.getDefault().post(finishActivityEvent)
                            MobclickAgent.onEvent(this@PostFeedActivity, "10001")
                            showToast(GlobalUtil.getString(R.string.post_success))
                            finish()
                        } else if (status == 10301) {
                            val timeLeft = postFeed.msg.toLong()
                            if (DateUtil.isBlockedForever(timeLeft)) {
                                showToast(GlobalUtil.getString(R.string.unable_to_post_feed_forever), Toast.LENGTH_LONG)
                            } else {
                                val tip = DateUtil.getTimeLeftTip(timeLeft)
                                showToast(String.format(GlobalUtil.getString(R.string.unable_to_post_feed), tip), Toast.LENGTH_LONG)
                            }
                        } else {
                            logWarn(TAG, "Post feed failed. " + GlobalUtil.getResponseClue(status, postFeed.msg))
                            showToast(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.unknown_error))
                        }
                    }
                }

                override fun onFailure(e: Exception) {
                    logWarn(TAG, e.message, e)
                    postFeedProgress.dismiss()
                    if (e is PostFeedException) {
                        when (e.message) {
                            PostFeedException.GIF_PATH_OR_FEED_CONTENT_IS_NULL -> {
                                showToastOnUiThread(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.field_missing))
                                return
                            }
                            PostFeedException.GIF_FORMAT_IS_INCORRECT -> {
                                showToastOnUiThread(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.gif_format_error))
                                return
                            }
                            PostFeedException.GIF_IS_LARGER_THAN_20_MB -> {
                                showToastOnUiThread(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.gif_larger_than_20_mb))
                                return
                            }
                            PostFeedException.GIF_COVER_IS_UNREACHABLE -> {
                                showToastOnUiThread(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.gif_cover_is_unreachable))
                                return
                            }
                            PostFeedException.GIF_WIDTH_OR_HEIGHT_IS_INVALID -> {
                                showToastOnUiThread(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.gif_width_or_height_invalid))
                                return
                            }
                            PostFeedException.GIF_IS_TOO_WIDE_OR_TOO_NARROW -> {
                                showToastOnUiThread(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.gif_is_too_wide_or_too_narrow))
                                return
                            }
                            PostFeedException.GIF_MD5_EXCEPTION -> {
                                showToastOnUiThread(GlobalUtil.getString(R.string.post_failed) + GlobalUtil.getString(R.string.gif_md5_exception))
                                return
                            }
                            PostFeedException.LOGIN_STATUS_EXPIRED -> return
                            else -> {
                            }
                        }
                    }
                    ResponseHandler.handleFailure(e)
                }
            })
        }
    }

    /**
     * 对选择的GIF图进行预加载，从而可以得到图片的宽高，然后再来计算图片的缩放比例。
     * @param path
     * GIF图片的路径。
     */
    private fun preloadSelectedGif(path: String) {
        Glide.with(this)
                .load(path)
                .asGif()
                .listener(object : RequestListener<String, GifDrawable> {
                    override fun onException(e: Exception?, model: String, target: Target<GifDrawable>, isFirstResource: Boolean): Boolean {
                        showToast(GlobalUtil.getString(R.string.gif_format_error))
                        return true
                    }

                    override fun onResourceReady(resource: GifDrawable, model: String, target: Target<GifDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        calculateWidgetValues(resource, path)
                        return true
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .preload()
    }

    /**
     * 获取GIF图片原始的宽度和高度，从而计算图片的缩放比例。计算出最终缩放后的宽高之后，再将图片加载出来。
     * @param gifDrawable
     * GIF图片的Drawable对象。
     * @param path
     * GIF图片的路径。
     */
    private fun calculateWidgetValues(gifDrawable: GifDrawable, path: String) {
        val firstFrame = gifDrawable.firstFrame
        if (firstFrame != null) {
            val gifWidth = firstFrame.width
            val gifHeight = firstFrame.height
            if (gifWidth / (gifHeight * 1.0) > 2.5 || gifHeight / (gifWidth * 1.0) > 2.5) {
                showToastOnUiThread(GlobalUtil.getString(R.string.gif_is_too_wide_or_too_narrow))
                return
            }
            val maxWidth = DeviceInfo.screenWidth.toDouble() // 图片允许的最大宽度是屏幕的宽度
            val maxHeight = DeviceInfo.screenHeight * 0.65 // 图片的允许最大高度是屏幕高度的65%，因为要为标题栏和输入框留下空间
            val widthRatio = maxWidth / gifWidth
            val heightRatio = maxHeight / gifHeight
            val ratio = Math.min(widthRatio, heightRatio)

            overrideWidth = (gifWidth * ratio).toInt()
            overrideHeight = (gifHeight * ratio).toInt()
            selectedGifLayout.layoutParams.height = overrideHeight
            selectedGif.layoutParams.width = overrideWidth
            selectedGif.layoutParams.height = overrideHeight

            val loopForever = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(getString(R.string.key_loop_gif_play), true)
            gifPlayTarget = GifPlayTarget(selectedGif, path, firstFrame, loopForever)
            Glide.with(this)
                    .load(path)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .listener(object : RequestListener<String, GlideDrawable> {
                        override fun onException(e: Exception, model: String, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                            closeButton.visibility = View.VISIBLE
                            postWallLayout.setBackgroundColor(ContextCompat.getColor(this@PostFeedActivity, R.color.black))
                            return false
                        }
                    })
                    .into<GifPlayTarget>(gifPlayTarget)
            showGifLayout()
        } else {
            showToast(GlobalUtil.getString(R.string.unknown_error))
        }
    }

    /**
     * 显示选中GIF图布局，隐藏选择GIF图布局。
     */
    private fun showGifLayout() {
        selectedGifLayout.visibility = View.VISIBLE
        chooseGifLayout.visibility = View.GONE
    }

    /**
     * 显示选择GIF图布局，隐藏选中GIF图布局。
     */
    private fun hideGifLayout() {
        selectedGif.setImageDrawable(null)
        selectedGifLayout.visibility = View.GONE
        closeButton.visibility = View.GONE
        chooseGifLayout.visibility = View.VISIBLE
        postWallLayout.setBackgroundResource(R.drawable.post_wall_repeat)
        gifPlayTarget = null
    }

    companion object {

        private const val TAG = "PostFeedActivity"

        private const val INTENT_DRAFT = "intent_draft"

        private const val PICK_GIF = 1

        fun actionStart(activity: Activity) {
            val intent = Intent(activity, PostFeedActivity::class.java)
            activity.startActivity(intent)
        }

        fun actionStart(activity: Activity, draft: Draft) {
            val intent = Intent(activity, PostFeedActivity::class.java)
            intent.putExtra(INTENT_DRAFT, draft)
            activity.startActivity(intent)
        }
    }

}