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
import com.quxianggif.network.model.ReportComment
import com.quxianggif.network.model.ReportUser
import com.quxianggif.network.util.NetworkConst
import okhttp3.Headers
import java.util.*

/**
 * 举报用户请求。对应服务器接口：/report/user
 *
 * @author guolin
 * @since 18/8/29
 */
class ReportUserRequest : Request() {

    private var user: Long = 0

    private var reason = 0

    private var desp = ""

    fun user(user: Long): ReportUserRequest {
        this.user = user
        return this
    }

    fun reason(reason: Int): ReportUserRequest {
        this.reason = reason
        return this
    }

    fun desp(desp: String) : ReportUserRequest {
        this.desp = desp
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
        inFlight(ReportUser::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            params[NetworkConst.USER] = user.toString()
            params[NetworkConst.REASON] = reason.toString()
            if (desp.isNotBlank()) {
                params[NetworkConst.DESCRIPTION] = desp
            }
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.UID, NetworkConst.USER, NetworkConst.REASON)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/report/user"
    }
}
