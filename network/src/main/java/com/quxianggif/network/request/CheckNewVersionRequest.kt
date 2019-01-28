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
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.CheckNewVersion
import com.quxianggif.network.util.NetworkConst
import okhttp3.Headers
import java.util.*

/**
 * 检查是否有新版本。对应服务器接口：/check_new_version
 *
 * @author guolin
 * @since 18/7/10
 */
class CheckNewVersionRequest : Request() {

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.POST
    }

    override fun listen(callback: Callback?) {
        setListener(callback)
        inFlight(CheckNewVersion::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            params[NetworkConst.CLIENT_VERSION] = GlobalUtil.appVersionCode.toString()
            val appChannel =  GlobalUtil.getApplicationMetaData("APP_CHANNEL")
            if (appChannel != null) {
                params[NetworkConst.CLIENT_CHANNEL] = appChannel
            }
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.CLIENT_VERSION, NetworkConst.UID)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/check_new_version"
    }
}
