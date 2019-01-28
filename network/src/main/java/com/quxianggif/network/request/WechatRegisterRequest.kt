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
import com.quxianggif.network.model.WechatRegister
import com.quxianggif.network.util.NetworkConst
import java.util.*

/**
 * 注册使用微信第三方登录账号请求。对应服务器接口：/register/wechat
 *
 * @author guolin
 * @since 18/11/3
 */
class WechatRegisterRequest : Request() {

    private var openId: String = ""

    private var accessToken: String = ""

    private var nickname: String = ""

    fun openId(openId: String): WechatRegisterRequest {
        this.openId = openId
        return this
    }

    fun accessToken(accessToken: String): WechatRegisterRequest {
        this.accessToken = accessToken
        return this
    }

    fun nickname(nickname: String): WechatRegisterRequest {
        this.nickname = nickname
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
        inFlight(WechatRegister::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        params[NetworkConst.OPEN_ID] = openId
        params[NetworkConst.ACCESS_TOKEN] = accessToken
        params[NetworkConst.NICKNAME] = nickname
        params[NetworkConst.DEVICE_NAME] = deviceName
        params[NetworkConst.DEVICE_SERIAL] = deviceSerial
        return params
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/register/wechat"
    }
}
