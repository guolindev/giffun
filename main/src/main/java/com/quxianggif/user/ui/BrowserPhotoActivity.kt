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

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.transition.Transition
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.quxianggif.R
import com.quxianggif.common.callback.SimpleTransitionListener
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.common.view.photoview.PhotoView
import com.quxianggif.core.extension.logDebug
import com.quxianggif.core.extension.logError
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.LoadOriginAvatarEvent
import com.quxianggif.util.glide.CustomUrl
import com.quxianggif.util.glide.GlideUtil
import org.greenrobot.eventbus.EventBus

/**
 * 浏览用户头像大图.
 *
 * @author davy
 * @since 17/8/9
 */
class BrowserPhotoActivity : BaseActivity() {

    private lateinit var photoView: PhotoView

    private var avatarUrl: String = ""

    private var avatarSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_browser_photo)
    }

    override fun setupViews() {
        photoView = findViewById(R.id.photo)
        avatarUrl = intent.getStringExtra(URL)
        if (AndroidVersion.hasLollipop()) {
            postponeEnterTransition()
            window.sharedElementEnterTransition.addListener(object : SimpleTransitionListener() {
                override fun onTransitionEnd(transition: Transition) {
                    if (AndroidVersion.hasLollipop()) {
                        transition.removeListener(this)
                        loadOriginPhoto()
                    }
                }
            })
        }
        Glide.with(activity)
                .load(CustomUrl(avatarUrl))
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .error(R.drawable.avatar_default)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
                        photoView.setImageBitmap(resource)
                        avatarSize = resource.height
                        if (AndroidVersion.hasLollipop()) {
                            startPostponedEnterTransition()
                        } else {
                            loadOriginPhoto()
                        }
                    }

                    override fun onLoadFailed(e: Exception, errorDrawable: Drawable) {
                        if (AndroidVersion.hasLollipop()) {
                            startPostponedEnterTransition()
                        }
                        super.onLoadFailed(e, errorDrawable)
                    }
                })
    }

    /**
     * 加载原尺寸的头像图片。
     */
    private fun loadOriginPhoto() {
        val avatarSizeFromUrl = parseAvatarSizeFromUrl()
        if (avatarSizeFromUrl == 0 || avatarSize > avatarSizeFromUrl) {
            return
        }
        val originUrl = CustomUrl(avatarUrl).cacheKey
        GlideUtil.removeImageCache(originUrl)
        Glide.with(activity)
                .load(originUrl)
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.avatar_default)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
                        photoView.setImageBitmap(resource)
                        val event = LoadOriginAvatarEvent()
                        EventBus.getDefault().post(event)
                    }
                })
    }

    /**
     * 解析头像URL中包含的图片尺寸。
     * @return 头像URL中包含的图片尺寸
     */
    private fun parseAvatarSizeFromUrl(): Int {
        try {
            if (!TextUtils.isEmpty(avatarUrl)) {
                val index = avatarUrl.lastIndexOf("/")
                if (index >= 0) {
                    val sizeStr = avatarUrl.substring(index + 1)
                    logDebug(TAG, "size str is $sizeStr")
                    return Integer.parseInt(sizeStr)
                }
            }
        } catch (e: Exception) {
            logError(TAG, e.message, e)
        }

        return 0
    }

    companion object {

        private const val TAG = "BrowserPhotoActivity"

        private const val URL = "url"

        fun actionStart(activity: Activity, url: String, imageView: ImageView) {
            val intent = Intent(activity, BrowserPhotoActivity::class.java)
            intent.putExtra(URL, url)
            if (AndroidVersion.hasLollipop()) {
                val options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        imageView, GlobalUtil.getString(R.string.transition_browse_photo))
                activity.startActivity(intent, options.toBundle())
            } else {
                activity.startActivity(intent)
            }
        }
    }

}