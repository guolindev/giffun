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

package com.quxianggif.network.request

import com.quxianggif.core.extension.logVerbose
import java.io.IOException

import okhttp3.*

/**
 * OkHttp网络请求日志拦截器，通过日志记录OkHttp所有请求以及响应的细节。
 *
 * @author guolin
 * @since 17/2/25
 */
internal class LoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        logVerbose(TAG, "Sending request: " + request.url() + "\n" + request.headers())

        val response = chain.proceed(request)

        val t2 = System.nanoTime()
        logVerbose(TAG, "Received response for " + response.request().url() + " in "
                + (t2 - t1) / 1e6 + "ms\n" + response.headers())
        return response
    }

    companion object {

        val TAG = "LoggingInterceptor"

    }

}
