/*
 * Copyright 2015 Google Inc.
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

package com.quxianggif.util

import android.graphics.Bitmap
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.palette.graphics.Palette
import com.quxianggif.core.extension.logDebug

/**
 * Utility methods for working with colors.
 */
object ColorUtils {

    private const val IS_LIGHT = 0
    private const val IS_DARK = 1
    private const val LIGHTNESS_UNKNOWN = 2

    private const val TAG = "ColorUtils"

    /**
     * Set the alpha component of `color` to be `alpha`.
     */
    @CheckResult
    @ColorInt
    fun modifyAlpha(@ColorInt color: Int,
                    @IntRange(from = 0, to = 255) alpha: Int): Int {
        return color and 0x00ffffff or (alpha shl 24)
    }

    /**
     * Set the alpha component of `color` to be `alpha`.
     */
    @CheckResult
    @ColorInt
    fun modifyAlpha(@ColorInt color: Int,
                    @FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
        return modifyAlpha(color, (255f * alpha).toInt())
    }

    /**
     * 判断传入的图片的颜色属于深色还是浅色。
     * @param bitmap
     * 图片的Bitmap对象。
     * @return 返回true表示图片属于深色，返回false表示图片属于浅色。
     */
    fun isBitmapDark(palette: Palette?, bitmap: Bitmap): Boolean {
        val isDark: Boolean
        val lightness = ColorUtils.isDark(palette)
        if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
            isDark = ColorUtils.isDark(bitmap, bitmap.width / 2, 0)
        } else {
            isDark = lightness == ColorUtils.IS_DARK
        }
        return isDark
    }

    /**
     * Checks if the most populous color in the given palette is dark
     *
     *
     * Annoyingly we have to return this Lightness 'enum' rather than a boolean as palette isn't
     * guaranteed to find the most populous color.
     */
    fun isDark(palette: Palette?): Int {
        val mostPopulous = getMostPopulousSwatch(palette) ?: return LIGHTNESS_UNKNOWN
        return if (isDark(mostPopulous.hsl)) IS_DARK else IS_LIGHT
    }

    /**
     * Determines if a given bitmap is dark. This extracts a palette inline so should not be called
     * with a large image!! If palette fails then check the color of the specified pixel
     */
    fun isDark(bitmap: Bitmap, backupPixelX: Int, backupPixelY: Int): Boolean {
        // first try palette with a small color quant size
        val palette = Palette.from(bitmap).maximumColorCount(3).generate()
        return if (palette.swatches.size > 0) {
            isDark(palette) == IS_DARK
        } else {
            // if palette failed, then check the color of the specified pixel
            isDark(bitmap.getPixel(backupPixelX, backupPixelY))
        }
    }

    /**
     * Convert to HSL & check that the lightness value
     */
    fun isDark(@ColorInt color: Int): Boolean {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(color, hsl)
        return isDark(hsl)
    }

    /**
     * Check that the lightness value (0–1)
     */
    fun isDark(hsl: FloatArray): Boolean { // @Size(3)
        logDebug(TAG, "hsl[2] is " + hsl[2])
        return hsl[2] < 0.8f
    }

    fun getMostPopulousSwatch(palette: Palette?): Palette.Swatch? {
        var mostPopulous: Palette.Swatch? = null
        if (palette != null) {
            for (swatch in palette.swatches) {
                if (mostPopulous == null || swatch.population > mostPopulous.population) {
                    mostPopulous = swatch
                }
            }
        }
        return mostPopulous
    }

}
