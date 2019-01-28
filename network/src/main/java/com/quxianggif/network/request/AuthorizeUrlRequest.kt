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

import com.quxianggif.core.GifFun
import com.quxianggif.network.model.AuthorizeUrl
import com.quxianggif.network.model.Callback
import com.quxianggif.network.util.NetworkConst

import java.util.HashMap

import okhttp3.Headers

/**
 * 获取私有空间图片下载地址请求。对应服务器接口：/authorize_url
 *
 * @author guolin
 * @since 17/6/24
 */
class AuthorizeUrlRequest : Request() {

    /**
     * GIF图片的url地址。
     */
    private var url: String = ""

    /**
     * GIF图片对应feed的serverId。
     */
    private var feedId: Long = 0

    fun url(url: String): AuthorizeUrlRequest {
        this.url = url
        return this
    }

    fun feedId(feedId: Long): AuthorizeUrlRequest {
        this.feedId = feedId
        return this
    }

    override fun listen(callback: Callback?) {
        setListener(callback)
        inFlight(AuthorizeUrl::class.java)
    }

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.POST
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            params[NetworkConst.URL] = url
            params[NetworkConst.FEED] = feedId.toString()
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.FEED, NetworkConst.UID, NetworkConst.TOKEN)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/authorize_url"
    }
}
