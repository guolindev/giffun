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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.widget.ImageView

import com.quxianggif.R
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.util.AnimUtils
import com.quxianggif.util.ColorUtils

/**
 * Feed详情页用于展示GIF图的控件，支持Parallax偏移效果。
 *
 * @author guolin
 * @since 17/6/8
 */
class ParallaxImageView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {
    private val scrimPaint: Paint
    private var imageOffset: Int = 0
    private var minOffset: Int = 0
    private val clipBound = Rect()
    private var scrimAlpha = 0f
    private val maxScrimAlpha = 0.3f
    private var scrimColor = Color.TRANSPARENT
    private val parallaxFactor = -0.5f
    var isPinned = false
        set(isPinned) {
            if (this.isPinned != isPinned) {
                field = isPinned
                refreshDrawableState()
                if (isPinned && isImmediatePin) {
                    jumpDrawablesToCurrentState()
                }
            }
        }
    var isImmediatePin = false

    var offset: Int
        get() = translationY.toInt()
        set(offset) {
            var offsetin = offset
            offsetin = Math.max(minOffset, offsetin)
            if (offsetin.toFloat() != translationY) {
                translationY = offsetin.toFloat()
                imageOffset = (offsetin * parallaxFactor).toInt()
                clipBound.set(0, -offsetin, width, height)
                if (AndroidVersion.hasJellyBeanMR2()) {
                    clipBounds = clipBound
                }
                setScrimAlpha(Math.min(
                        (-offsetin).toFloat() / minimumHeight * maxScrimAlpha, maxScrimAlpha))
                postInvalidateOnAnimation()
            }
            isPinned = offsetin == minOffset
        }

    init {
        scrimColor = ContextCompat.getColor(context, R.color.scrim)
        scrimPaint = Paint()
        scrimPaint.color = ColorUtils.modifyAlpha(scrimColor, scrimAlpha)
    }

    fun setScrimColor(@ColorInt scrimColor: Int) {
        if (this.scrimColor != scrimColor) {
            this.scrimColor = scrimColor
            postInvalidateOnAnimation()
        }
    }

    fun setMaxScrimAlpha() {
        setScrimAlpha(maxScrimAlpha)
    }

    fun clearScrimAlpha() {
        setScrimAlpha(0f)
    }

    fun setScrimAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (scrimAlpha != alpha) {
            scrimAlpha = alpha
            scrimPaint.color = ColorUtils.modifyAlpha(scrimColor, scrimAlpha)
            postInvalidateOnAnimation()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h > minimumHeight) {
            minOffset = minimumHeight - h
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (imageOffset != 0) {
            val saveCount = canvas.save()
            canvas.translate(0f, imageOffset.toFloat())
            super.onDraw(canvas)
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
            canvas.restoreToCount(saveCount)
        } else {
            super.onDraw(canvas)
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (this.isPinned) {
            View.mergeDrawableStates(drawableState, STATE_PINNED)
        }
        return drawableState
    }

    companion object {

        private val STATE_PINNED = intArrayOf(R.attr.state_pinned)

        val OFFSET: Property<ParallaxImageView, Int> = object : AnimUtils.IntProperty<ParallaxImageView>("offset") {

            override fun setValue(propertyObject: ParallaxImageView, value: Int) {
                propertyObject.offset = value
            }

            override fun get(parallaxImageView: ParallaxImageView): Int {
                return parallaxImageView.offset
            }
        }
    }

}
