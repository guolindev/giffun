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
import android.os.Bundle
import android.provider.MediaStore
import com.quxianggif.R
import com.quxianggif.common.ui.AlbumFragment
import com.quxianggif.core.extension.showToastOnUiThread
import com.quxianggif.core.util.SharedUtil
import com.quxianggif.feeds.adapter.GifAlbumAdapter
import kotlinx.android.synthetic.main.activity_select_gif.*

/**
 * GIF相册界面，可以在这里选择要分享的GIF图，长按可预览GIF。
 *
 * @author guolin
 * @since 17/3/20
 */
class GifAlbumFragment : AlbumFragment() {

    /**
     * 控制相册每行展示几张图片。
     */
    override var columnCount = 2

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val activity = activity as Activity
        if (activity is SelectGifActivity) {
            activity.currentFragment = this
            activity.toolbarTitleText.text = getString(R.string.gif_album)
        }
        adapter = GifAlbumAdapter()
        selection = MediaStore.Images.Media.MIME_TYPE + " = ?"
        selectionArgs = arrayOf("image/gif")
        super.onActivityCreated(savedInstanceState)
    }

    override fun loadComplete() {
        super.loadComplete()
        if (imageList.isNotEmpty()) {
            // 当相册不为空时，首先打开此界面弹出一个小提示
            val openFirst = SharedUtil.read(OPEN_GIF_ALBUM_FIRST, true)
            if (openFirst) {
                showToastOnUiThread(getString(R.string.tip_long_press_to_preview))
                SharedUtil.save(OPEN_GIF_ALBUM_FIRST, false)
            }
        }
    }

    companion object {

        private const val TAG = "GifAlbumFragment"

        private const val OPEN_GIF_ALBUM_FIRST = "open_gif_album_first"
    }
}
