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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.quxianggif.R
import com.quxianggif.common.adapter.AlbumAdapter
import com.quxianggif.common.ui.AlbumActivity
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.core.util.ImageUtil
import com.quxianggif.util.glide.GifAlbumTarget
import com.quxianggif.util.glide.GlideUtil

/**
 * GIF相册的适配器，在这里处理GIF图的缩略图展示、长按预览、以及选中图片的逻辑。
 *
 * @author guolin
 * @since 17/4/1
 */
class GifAlbumAdapter : AlbumAdapter() {

    private var playingGif: GifDrawable? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumAdapter.ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        val view = LayoutInflater.from(mContext).inflate(R.layout.album_image_item, parent, false)
        val holder = AlbumAdapter.ViewHolder(view)
        holder.image.setOnClickListener(View.OnClickListener {
            if (mContext is Activity) {
                val activity = mContext as Activity
                val position = holder.adapterPosition
                val image = mImageList[position]
//                if (!ImageUtil.isGifValid(imagePath)) {
//                    showToast(GlobalUtil.getString(R.string.gif_format_error))
//                    return@OnClickListener
//                }
                if (image.size > GifFun.GIF_MAX_SIZE) {
                    showToast(GlobalUtil.getString(R.string.gif_larger_than_20_mb))
                    return@OnClickListener
                }
                val intent = Intent()
                intent.putExtra(AlbumActivity.IMAGE_URI, image.uri)
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            } else {
                showToast(GlobalUtil.getString(R.string.unknown_error))
            }
        })
        holder.image.setOnLongClickListener {
            playingGif = GlideUtil.getGifDrawable(holder.image)
            // 如果是GIF图，按住情况下播放图片
            playingGif?.start()
            false
        }
        holder.image.setOnTouchListener(View.OnTouchListener { _, event ->
            // 检查是否是我们关心的事件，如果不是直接抛弃
            val action = event.action
            if (!(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL))
                return@OnTouchListener false

            if (playingGif != null) {
                when (action) {
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        playingGif!!.stop()
                        playingGif = null
                    }
                }
                return@OnTouchListener true
            }
            false
        })
        return holder
    }

    override fun onBindViewHolder(holder: AlbumAdapter.ViewHolder, position: Int) {
        holder.image.layoutParams.width = mImageSize
        holder.image.layoutParams.height = mImageSize
        val image = mImageList[position]
        // 使用Glide加载GIF图
        Glide.with(mContext)
                .load(image.uri)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.album_loading_bg)
                .override(mImageSize, mImageSize)
                .into(GifAlbumTarget(holder.image, false))
    }

    companion object {
        private const val TAG = "GifAlbumAdapter"
    }

}
