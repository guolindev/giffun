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
import android.text.StaticLayout
import android.util.AttributeSet
import android.widget.TextView

/**
 * 防止TextView不正常自动换行的自定义TextView。
 *
 * @author guolin
 * @since 17/10/29
 */
class AntiWrapTextView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    private var mLineY: Int = 0
    private var mViewWidth: Int = 0

    override fun onDraw(Canvas: Canvas) {
        val paint = paint
        paint.color = currentTextColor
        paint.drawableState = drawableState
        mViewWidth = measuredWidth
        val text = text.toString()
        mLineY = 0
        mLineY += textSize.toInt()
        val layout = layout ?: return

        // layout.getLayout()在4.4.3出现NullPointerException

        val fm = paint.fontMetrics

        var textHeight = Math.ceil((fm.descent - fm.ascent).toDouble()).toInt()
        textHeight = (textHeight * layout.spacingMultiplier + layout
                .spacingAdd).toInt()
        //解决了最后一行文字间距过大的问题
        for (i in 0 until layout.lineCount) {
            val lineStart = layout.getLineStart(i)
            val lineEnd = layout.getLineEnd(i)
            val width = StaticLayout.getDesiredWidth(text, lineStart,
                    lineEnd, getPaint())
            val line = text.substring(lineStart, lineEnd)

            if (i < layout.lineCount - 1) {
                if (needScale(line)) {
                    drawScaledText(Canvas, line, width)
                } else {
                    Canvas.drawText(line, 0f, mLineY.toFloat(), paint)
                }
            } else {
                Canvas.drawText(line, 0f, mLineY.toFloat(), paint)
            }
            mLineY += textHeight
        }
    }

    private fun drawScaledText(Canvas: Canvas, line: String, lineWidth: Float) {
        var linein = line
        var x = 0f
        if (isFirstLineOfParagraph(linein)) {
            val blanks = "  "
            Canvas.drawText(blanks, x, mLineY.toFloat(), paint)
            val bw = StaticLayout.getDesiredWidth(blanks, paint)
            x += bw

            linein = linein.substring(3)
        }

        val gapCount = linein.length - 1
        var i = 0
        if (linein.length > 2 && linein[0].toInt() == 12288
                && linein[1].toInt() == 12288) {
            val substring = linein.substring(0, 2)
            val cw = StaticLayout.getDesiredWidth(substring, paint)
            Canvas.drawText(substring, x, mLineY.toFloat(), paint)
            x += cw
            i += 2
        }

        val d = (mViewWidth - lineWidth) / gapCount
        while (i < linein.length) {
            val c = linein[i].toString()
            val cw = StaticLayout.getDesiredWidth(c, paint)
            Canvas.drawText(c, x, mLineY.toFloat(), paint)
            x += cw + d
            i++
        }
    }

    private fun isFirstLineOfParagraph(line: String): Boolean {
        return (line.length > 3 && line[0] == ' '
                && line[1] == ' ')
    }

    private fun needScale(line: String?): Boolean {
        return if (line == null || line.isEmpty()) {
            false
        } else {
            line[line.length - 1] != '\n'
        }
    }

}