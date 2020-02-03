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

package com.quxianggif.util.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView

import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget

/**
 * 用于对GIF图播放进行控制的Target，默认情况下GIF图永远循环播放，可以通过参数来指定只播放一次，并且提供了暂停和继续播放GIF图的接口。
 *
 * @author guolin
 * @since 17/4/18
 */
class GifPlayTarget @JvmOverloads constructor(view: ImageView,
                                              /**
                                               * 获取选中的GIF图的路径。
                                               * @return 选中的GIF图的路径。
                                               */
                                              val gifUri: Uri? = null,
                                              /**
                                               * 获取选中的GIF图的第一帧的Bitmap。
                                               * @return 选中的GIF图的第一帧的Bitmap。
                                               */
                                              val firstFrame: Bitmap? = null,

                                              playForever: Boolean = true) : GlideDrawableImageViewTarget(view, if (playForever) GifDrawable.LOOP_FOREVER else 1) {

    private var gifDrawable: GifDrawable? = null

    private var url: String? = null

    private var delayRatio = 1.0

    /**
     * 判断当前GIF图是否正在播放。
     * @return 正在播放返回true，否则返回false。
     */
    val isRunning: Boolean
        get() = gifDrawable != null && gifDrawable!!.isRunning

    constructor(view: ImageView, playForever: Boolean) : this(view, null, null, playForever) {}

    override fun onResourceReady(resource: GlideDrawable, animation: GlideAnimation<in GlideDrawable>) {
        super.onResourceReady(resource, animation)
        url?.let { ProgressInterceptor.removeListener(it) }
        if (resource is GifDrawable) {
            gifDrawable = resource
            gifDrawable!!.decoder.setDelayRatio(delayRatio)
        }
    }

    override fun onLoadFailed(e: Exception, errorDrawable: Drawable) {
        super.onLoadFailed(e, errorDrawable)
        url?.let { ProgressInterceptor.removeListener(it) }
    }

    override fun onStart() {}

    override fun onStop() {}

    /**
     * 设置网络请求下载进度监听器。
     * @param url
     * Glide请求的url地址。
     * @param listener
     * 下载进度的监听器。
     */
    fun setProgressListener(url: String, listener: ProgressListener) {
        this.url = url
        ProgressInterceptor.addListener(url, listener)
    }

    /**
     * 设置GIF的播放速度。
     * @param playSpeed
     * 1代表1/3速度播放，2代表1/2速度播放，3代表正常速度播放，4代表1.5倍速度播放，5代表2倍速度播放。
     */
    fun setGifPlaySpeed(playSpeed: String) {
        when (playSpeed) {
            "1" -> delayRatio = 3.0
            "2" -> delayRatio = 2.0
            "3" -> delayRatio = 1.0
            "4" -> delayRatio = 1 / 1.5
            "5" -> delayRatio = 1 / 2.0
        }
    }

    /**
     * 恢复GIF播放。
     */
    fun resumePlaying() {
        super.onStart()
    }

    /**
     * 暂停GIF播放。
     */
    fun pausePlaying() {
        super.onStop()
    }

}
