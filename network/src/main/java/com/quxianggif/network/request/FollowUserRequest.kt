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
import com.quxianggif.network.model.FollowUser
import com.quxianggif.network.util.NetworkConst

import java.util.HashMap

import okhttp3.Headers

/**
 * 关注用户请求。对应服务器接口：/user/follow
 *
 * @author guolin
 * @since 17/7/26
 */
class FollowUserRequest : Request() {

    private var followingIds: LongArray? = null

    fun followingIds(vararg followingIds: Long): FollowUserRequest {
        this.followingIds = followingIds
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
        inFlight(FollowUser::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            val followingsBuilder = StringBuilder()
            var needComma = false
            if (this.followingIds != null && this.followingIds!!.size > 0) {
                for (followingId in this.followingIds!!) {
                    if (needComma) {
                        followingsBuilder.append(",")
                    }
                    followingsBuilder.append(followingId)
                    needComma = true
                }
            }
            params[NetworkConst.FOLLOWING_IDS] = followingsBuilder.toString()
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.FOLLOWING_IDS, NetworkConst.TOKEN)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/user/follow"
    }

}
