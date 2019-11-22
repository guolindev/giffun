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

package com.quxianggif.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quxianggif.R
import com.quxianggif.core.extension.dp2px

/**
 * 实现RecyclerView item之间分割线的效果。
 *
 * @author guolin
 * @since 18/3/27
 */
class SimpleDividerDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val dividerHeight: Int = dp2px(0.5f)

    /**
     * 分割线条左侧的空余距离。
     */
    private var leftSpace = 0

    /**
     * 控制是否在最后一项item的下面绘制分割线。
     */
    private var showDividerUnderLastItem = false

    private val dividerPaint: Paint = Paint()

    init {
        dividerPaint.color = ContextCompat.getColor(context, R.color.split)
    }

    constructor(context: Context, showDividerUnderLastItem: Boolean) : this(context) {
        this.showDividerUnderLastItem = showDividerUnderLastItem
    }

    constructor(context: Context, leftSpace: Int) : this(context) {
        this.leftSpace = leftSpace
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val left = parent.paddingLeft + leftSpace
        val right = parent.width - parent.paddingRight

        val loopLength = if (showDividerUnderLastItem) {
            childCount // 循环绘制item底部分割线，最后一项也需要绘制
        } else {
            childCount - 1 // 循环绘制item底部分割线，最后一项是不用绘制的
        }
        for (i in 0 until loopLength) {
            val view = parent.getChildAt(i)
            if (i == loopLength - 1) {
                val lastView = parent.getChildAt(loopLength)
                if (lastView?.id == R.id.loadingFooterLayout) { // 如果最后一项是loading_footer布局，那么倒数第二项item不用绘制分割线
                    return
                }
            }
            val top = view.bottom.toFloat()
            val bottom = (view.bottom + dividerHeight).toFloat()
            c.drawRect(left.toFloat(), top, right.toFloat(), bottom, dividerPaint)
        }
    }

    companion object {

        private val TAG = "SimpleDividerDecoration"
    }

}
