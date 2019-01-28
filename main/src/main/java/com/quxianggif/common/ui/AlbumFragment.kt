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

package com.quxianggif.common.ui

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.GridLayoutManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.quxianggif.R
import com.quxianggif.common.adapter.AlbumAdapter
import com.quxianggif.core.extension.logError
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.feeds.ui.GifAlbumFragment
import kotlinx.android.synthetic.main.fragment_album.*
import java.io.File
import java.util.*

/**
 * 相册界面，可以在这里选择图片。
 *
 * @author guolin
 * @since 17/3/20
 */
open class AlbumFragment : BaseFragment() {

    /**
     * 相册中图片路径的列表。
     */
    var imageList: MutableList<String> = ArrayList()

    /**
     * 控制相册每行展示几张图片。
     */
    /**
     * 获取相册每行展示几张图片。
     * @return 相册每行展示几张图片。
     */
    open var columnCount = 3

    /**
     * 指定相册中图片路径的列。
     */
    var projection = arrayOf(MediaStore.Images.Media.DATA)

    /**
     * 过滤相册中图片的类型，默认不过滤。
     */
    var selection: String? = null

    var selectionArgs: Array<String>? = null

    private var activity: Activity? = null

    var adapter: AlbumAdapter? = null

    private var cropWidth: Int = 0

    private var cropHeight: Int = 0

    /**
     * 通过获取屏幕宽度来计算出相册中每张图片的大小。
     *
     * @return 计算后得出的每张图片的大小。
     */
    private val imageSize: Int
        get() {
            val windowManager = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay?.getMetrics(metrics)
            return metrics.widthPixels / columnCount
        }

    fun setCropSize(cropWidth: Int, cropHeight: Int) {
        this.cropWidth = cropWidth
        this.cropHeight = cropHeight
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater.inflate(R.layout.fragment_album, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity()
        val layoutManager = GridLayoutManager(context, columnCount)
        albumGridView.layoutManager = layoutManager
        if (adapter == null) {
            adapter = AlbumAdapter(cropWidth, cropHeight)
        }
        adapter?.setImageSize(imageSize)
        loadImages()
    }

    /**
     * 开启线程，开始加载相册中的图片。
     */
    private fun loadImages() {
        Thread(ImageLoaderRunnable()).start()
    }

    private inner class ImageLoaderRunnable : Runnable {
        override fun run() {
            var cursor: Cursor? = null
            try {
                cursor = activity?.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        selection, selectionArgs, MediaStore.Images.Media.DATE_ADDED)
                if (cursor != null) {
                    imageList.clear()
                    if (cursor.moveToLast()) {
                        do {
                            val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                            val file = File(path)
                            if (file.length() > 5 * 1024) {
                                imageList.add(path)
                            }
                        } while (cursor.moveToPrevious())
                    }
                }

            } catch (e: Exception) {
                logError(TAG, e.message, e)
            } finally {
                cursor?.close()
            }
            loadComplete()
        }
    }

    /**
     * 所有相册中的图片加载完成，通知刷新界面。
     */
    open fun loadComplete() {
        activity?.runOnUiThread {
            if (imageList.isEmpty()) {
                albumGridView.visibility = View.GONE
                progressBar.visibility = View.GONE
                val tip: String = if (this@AlbumFragment is GifAlbumFragment) {
                    GlobalUtil.getString(R.string.gif_album_is_empty)
                } else {
                    GlobalUtil.getString(R.string.album_is_empty)
                }
                showNoContentView(tip)
            } else {
                adapter?.setImageList(imageList)
                albumGridView.adapter = adapter
                progressBar.visibility = View.GONE
                albumGridView.visibility = View.VISIBLE
                adapter?.notifyDataSetChanged()
            }
        }
    }

    companion object {

        private const val TAG = "AlbumFragment"
    }

}