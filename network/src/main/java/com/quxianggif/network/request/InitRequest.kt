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
import com.quxianggif.network.model.Init
import com.quxianggif.network.util.NetworkConst

import java.util.HashMap

import okhttp3.Headers

/**
 * 初始化请求。对应服务器接口：/init
 *
 * @author guolin
 * @since 17/2/12
 */
class InitRequest : Request() {
    init {
        connectTimeout(5)
        readTimeout(5)
        writeTimeout(5)
    }

    override fun listen(callback: Callback?) {
        setListener(callback)
        inFlight(Init::class.java)
    }

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.GET
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        params[NetworkConst.CLIENT_VERSION] = GlobalUtil.appVersionCode.toString()
        val appChannel =  GlobalUtil.getApplicationMetaData("APP_CHANNEL")
        if (appChannel != null) {
            params[NetworkConst.CLIENT_CHANNEL] = appChannel
        }
        if (buildAuthParams(params)) {
            params[NetworkConst.DEVICE_NAME] = deviceName
        }
        return params
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.UID, NetworkConst.TOKEN)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/init"
    }
}
