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

package com.quxianggif.util.glide

import android.text.TextUtils

import com.bumptech.glide.load.model.GlideUrl
import com.quxianggif.core.extension.logDebug

/**
 * 用于加载带后缀参数的图片。使用CustomUrl可以使用Glide忽略掉URL地址后面的可变部分，
 * 只使用前面固定部分的URL地址来进行缓存。
 *
 * @author guolin
 * @since 17/6/25
 */
class CustomUrl(private val gifUrl: String) : GlideUrl(gifUrl) {

    override fun getCacheKey(): String {
        if (!TextUtils.isEmpty(gifUrl)) {
            val index = gifUrl.indexOf("?")
            val cacheKey = if (index > 0) {
                gifUrl.substring(0, index)
            } else {
                gifUrl
            }
            logDebug(TAG, "gifUrl: $gifUrl , cache key is $cacheKey")
            return cacheKey
        }
        return ""
    }

    companion object {
        private const val TAG = "CustomUrl"
    }

}
