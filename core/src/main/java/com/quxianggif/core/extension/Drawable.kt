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

package com.quxianggif.core.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * Drawable扩展工具类。
 *
 * @author guolin
 * @since 2018/10/19
 */

fun Drawable.toBitmap(): Bitmap {
    // 取 drawable 的长宽
    val w = intrinsicWidth
    val h = intrinsicHeight

    // 取 drawable 的颜色格式
    val config = if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    // 建立对应 bitmap
    val bitmap = Bitmap.createBitmap(w, h, config);
    // 建立对应 bitmap 的画布
    val canvas = Canvas(bitmap);
    setBounds(0, 0, w, h);
    // 把 drawable 内容画到画布中
    draw(canvas);
    return bitmap;
}