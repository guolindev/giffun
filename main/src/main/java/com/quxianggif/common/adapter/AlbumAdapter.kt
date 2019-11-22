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

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.quxianggif.R
import com.quxianggif.common.model.Image
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.core.util.ImageUtil
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

/**
 * 相册的适配器，在这里处理图片的缩略图展示，以及选中图片的逻辑。
 *
 * @author guolin
 * @since 17/4/1
 */
open class AlbumAdapter() : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    var mContext: Context? = null

    var mImageList: List<Image> = ArrayList()

    protected var mImageSize: Int = 0

    private var cropWidth: Int = 0

    private var cropHeight: Int = 0

    constructor(cropWidth: Int, cropHeight: Int) : this() {
        this.cropWidth = cropWidth
        this.cropHeight = cropHeight
    }

    /**
     * 将相册中的图片列表传入。
     *
     * @param imageList
     * 相册中图片路径的列表
     */
    fun setImageList(imageList: List<Image>) {
        mImageList = imageList
    }

    /**
     * 将每张图片的尺寸传入。
     *
     * @param imageSize
     * 图片的尺寸
     */
    fun setImageSize(imageSize: Int) {
        mImageSize = imageSize
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumAdapter.ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        val view = LayoutInflater.from(mContext).inflate(R.layout.album_image_item, parent, false)
        val holder = ViewHolder(view)
        holder.image.setOnClickListener {
            if (mContext is Activity) {
                val activity = mContext as Activity
                val position = holder.adapterPosition
                val image = mImageList[position]
                CropImage.activity(image.uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true)
                        .setAspectRatio(cropWidth, cropHeight)
                        .setActivityTitle(GlobalUtil.getString(R.string.crop))
                        .setRequestedSize(cropWidth, cropHeight)
                        .setCropMenuCropButtonIcon(R.drawable.ic_crop)
                        .start(activity)
            } else {
                showToast(GlobalUtil.getString(R.string.unknown_error))
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: AlbumAdapter.ViewHolder, position: Int) {
        holder.image.layoutParams.width = mImageSize
        holder.image.layoutParams.height = mImageSize
        val image = mImageList[position]
        if (image.mimeType == "image/gif") {
            Glide.with(mContext)
                 .load(image.uri)
                 .asBitmap()
                 .placeholder(R.drawable.album_loading_bg)
                 .override(mImageSize, mImageSize)
                 .into(holder.image)
        } else {
            Glide.with(mContext)
                 .load(image.uri)
                 .placeholder(R.drawable.album_loading_bg)
                 .override(mImageSize, mImageSize)
                 .into(holder.image)
        }
    }

    override fun getItemCount() = mImageList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        /**
         * 用于展示缩略图的ImageView
         */
        var image: ImageView = view.findViewById(R.id.albumImage)

    }

    companion object {

        private const val TAG = "AlbumAdapter"
    }

}
