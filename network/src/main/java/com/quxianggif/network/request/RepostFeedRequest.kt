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
import com.quxianggif.network.model.RepostFeed
import com.quxianggif.network.util.NetworkConst

import java.util.HashMap

import okhttp3.Headers

/**
 * 转发Feed请求。对应服务器接口：/feeds/repost
 *
 * @author guolin
 * @since 17/10/21
 */
class RepostFeedRequest : Request() {

    /**
     * 要存储到七牛云的key值，由客户端生成一个唯一的UUID
     */
    private var feedContent: String = ""

    private var refFeed: Long = 0

    fun refFeed(refFeed: Long): RepostFeedRequest {
        this.refFeed = refFeed
        return this
    }

    fun feedContent(feedContent: String): RepostFeedRequest {
        this.feedContent = feedContent
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
        inFlight(RepostFeed::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            params[NetworkConst.REF_FEED] = refFeed.toString()
            params[NetworkConst.CONTENT] = feedContent
            params[NetworkConst.DEVICE_NAME] = deviceName
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.TOKEN, NetworkConst.REF_FEED)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/feeds/repost"
    }

}
