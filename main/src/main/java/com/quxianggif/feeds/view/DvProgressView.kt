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

package com.quxianggif.feeds.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Build
import androidx.annotation.IntRange
import android.util.AttributeSet
import android.view.View

/**
 * 摄像机样式的自定义View，用于作用GIF图片的加载进度条。
 */
class DvProgressView : View {

    private lateinit var paint: Paint

    private var viewWidth: Int = 0

    private var viewHeight: Int = 0

    private var xfermode: PorterDuffXfermode? = null

    private lateinit var progressRect: RectF

    private lateinit var bitmap: Bitmap

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        progressRect = RectF(0f, 0f, 0f, 0f)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewWidth = width
        viewHeight = height
        progressRect.bottom = viewHeight.toFloat()
        val dvPath = createDvPath()
        bitmap = makePathBitmap(dvPath)
    }

    private fun makePathBitmap(path: Path): Bitmap {
        val bm = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        paint.color = Color.parseColor("#dbdcdc")
        c.drawPath(path, paint)
        return bm
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveLayer: Int = if (Build.VERSION.SDK_INT >= 21) {
            canvas.saveLayer(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
        } else {
            canvas.saveLayer(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        paint.xfermode = xfermode
        paint.color = Color.parseColor("#ffffff")
        canvas.drawRect(progressRect, paint)
        paint.xfermode = null
        canvas.restoreToCount(saveLayer)
    }

    private fun createDvPath(): Path {
        val dvBodyWidth = (viewWidth * 0.8).toInt()
        val dvBodyHeight = viewHeight
        //        int dvHeadWidth = viewWidth - dvBodyWidth;
        val dvHeadHeight = (viewHeight * 0.7).toInt()
        val dvNeckHeight = (viewHeight * 0.3).toInt()
        val cornerSize = (viewWidth * 0.05).toInt()

        val leftTopCorner = RectF(0f, 0f, (cornerSize * 2).toFloat(), (cornerSize * 2).toFloat())
        val rightTopCorner = RectF((dvBodyWidth - cornerSize * 2).toFloat(), 0f, dvBodyWidth.toFloat(), (cornerSize * 2).toFloat())
        val rightBottomCorner = RectF((dvBodyWidth - cornerSize * 2).toFloat(), (dvBodyHeight - cornerSize * 2).toFloat(), dvBodyWidth.toFloat(), dvBodyHeight.toFloat())
        val leftBottomCorner = RectF(0f, (dvBodyHeight - cornerSize * 2).toFloat(), (cornerSize * 2).toFloat(), dvBodyHeight.toFloat())

        val dvPath = Path()

        dvPath.moveTo(cornerSize.toFloat(), 0f)
        dvPath.lineTo((dvBodyWidth - cornerSize).toFloat(), 0f)
        dvPath.arcTo(rightTopCorner, 270f, 90f)

        dvPath.lineTo(dvBodyWidth.toFloat(), ((dvBodyHeight - dvNeckHeight) / 2).toFloat())
        dvPath.lineTo(viewWidth.toFloat(), ((dvBodyHeight - dvHeadHeight) / 2).toFloat())
        dvPath.lineTo(viewWidth.toFloat(), (dvHeadHeight + (dvBodyHeight - dvHeadHeight) / 2).toFloat())
        dvPath.lineTo(dvBodyWidth.toFloat(), (dvNeckHeight + (dvBodyHeight - dvNeckHeight) / 2).toFloat())
        dvPath.lineTo(dvBodyWidth.toFloat(), (dvBodyHeight - cornerSize).toFloat())

        dvPath.arcTo(rightBottomCorner, 0f, 90f)
        dvPath.lineTo(cornerSize.toFloat(), dvBodyHeight.toFloat())
        dvPath.arcTo(leftBottomCorner, 90f, 90f)
        dvPath.lineTo(0f, cornerSize.toFloat())
        dvPath.arcTo(leftTopCorner, 180f, 90f)
        dvPath.close()

        return dvPath
    }

    /**
     * 设置当前的加载进度。
     * @param progress
     * 当前的加载进度，值范围是0-100。
     */
    fun setProgress(@IntRange(from = 0, to = 100) progress: Int) {
        if (progress in 0..100) {
            progressRect.right = viewWidth * progress / 100.0f
            postInvalidate()
        }
    }

}