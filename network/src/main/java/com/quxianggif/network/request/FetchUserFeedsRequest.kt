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
import com.quxianggif.network.model.FetchUserFeeds
import com.quxianggif.network.util.NetworkConst

import java.util.HashMap

import okhttp3.Headers

/**
 * 获取指定用户所发Feeds的请求。对应服务器接口：/feeds/user
 *
 * @author guolin
 * @since 17/7/23
 */
class FetchUserFeedsRequest : Request() {

    private var lastFeed: Long = 0

    private var userId: Long = 0

    fun userId(userId: Long): FetchUserFeedsRequest {
        this.userId = userId
        return this
    }

    fun lastFeed(lastFeed: Long): FetchUserFeedsRequest {
        this.lastFeed = lastFeed
        return this
    }

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.GET
    }

    override fun listen(callback: Callback?) {
        setListener(callback)
        inFlight(FetchUserFeeds::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            if (lastFeed > 0) {
                params[NetworkConst.LAST_FEED] = lastFeed.toString()
            }
            if (userId > 0) {
                params[NetworkConst.USER_ID] = userId.toString()
            }
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.TOKEN, NetworkConst.USER_ID)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/feeds/user"
    }
}
