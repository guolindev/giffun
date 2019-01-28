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
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.PostComment
import com.quxianggif.network.util.NetworkConst

import java.util.HashMap

import okhttp3.Headers

/**
 * 发布评论请求。对应服务器接口：/comments/post
 *
 * @author davy, guolin
 * @since 17/7/11
 */
class PostCommentRequest : Request() {

    private var feed: Long = 0

    private var content: String = ""

    fun feed(feed: Long): PostCommentRequest {
        this.feed = feed
        return this
    }

    fun content(content: String): PostCommentRequest {
        this.content = content
        return this
    }

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.POST
    }

    override fun listen(callback: Callback?) {
        setListener(callback)
        inFlight(PostComment::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            params[NetworkConst.FEED] = feed.toString()
            params[NetworkConst.CONTENT] = content
            params[NetworkConst.DEVICE_NAME] = deviceName
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.FEED, NetworkConst.TOKEN, NetworkConst.DEVICE_NAME)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/comments/post"
    }
}
