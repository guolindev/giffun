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
import com.quxianggif.network.model.PhoneRegister
import com.quxianggif.network.util.NetworkConst
import java.util.*

/**
 * 注册使用手机号登录账号请求。对应服务器接口：/register/phone
 *
 * @author guolin
 * @since 18/1/10
 */
class PhoneRegisterRequest : Request() {

    private var number: String = ""

    private var code: String = ""

    private var nickname: String = ""

    fun number(number: String): PhoneRegisterRequest {
        this.number = number
        return this
    }

    fun code(code: String): PhoneRegisterRequest {
        this.code = code
        return this
    }

    fun nickname(nickname: String): PhoneRegisterRequest {
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
        inFlight(PhoneRegister::class.java)
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        params[NetworkConst.NUMBER] = number
        params[NetworkConst.CODE] = code
        params[NetworkConst.NICKNAME] = nickname
        params[NetworkConst.DEVICE_NAME] = deviceName
        params[NetworkConst.DEVICE_SERIAL] = deviceSerial
        return params
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/register/phone"
    }
}
