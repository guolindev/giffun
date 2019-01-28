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

import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.util.ContentLengthInputStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.IOException
import java.io.InputStream

/**
 * 自定义的OkHttp DataFetcher，用于使用OkHttp的方向来进行Glide网络请求。
 * @author guolin
 * @since 2017/10/23
 */
internal class OkHttpFetcher(private val client: OkHttpClient, private val url: GlideUrl) : DataFetcher<InputStream> {
    private var stream: InputStream? = null
    private var responseBody: ResponseBody? = null
    @Volatile
    private var isCancelled: Boolean = false

    @Throws(Exception::class)
    override fun loadData(priority: Priority): InputStream? {
        val requestBuilder = Request.Builder()
                .url(url.toStringUrl())
        for ((key, value) in url.headers) {
            requestBuilder.addHeader(key, value)
        }
        val request = requestBuilder.build()
        if (isCancelled) {
            return null
        }
        val response = client.newCall(request).execute()
        responseBody = response.body()
        if (!response.isSuccessful || responseBody == null) {
            throw IOException("Request failed with code: " + response.code())
        }
        stream = ContentLengthInputStream.obtain(responseBody!!.byteStream(),
                responseBody!!.contentLength())
        return stream
    }

    override fun cleanup() {
        try {
            if (stream != null) {
                stream!!.close()
            }
            if (responseBody != null) {
                responseBody!!.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun getId(): String {
        return url.cacheKey
    }

    override fun cancel() {
        isCancelled = true
    }
}