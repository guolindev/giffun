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

package com.quxianggif.network.util

import android.text.TextUtils
import com.quxianggif.core.Const
import com.quxianggif.core.util.SharedUtil

/**
 * 服务器身份验证相关的工具类。
 *
 * @author guolin
 * @since 17/2/14
 */
object AuthUtil {

    /**
     * 判断用户是否已登录。
     *
     * @return 已登录返回true，未登录返回false。
     */
    val isLogin: Boolean
        get() {
            val u = SharedUtil.read(Const.Auth.USER_ID, 0L)
            val t = SharedUtil.read(Const.Auth.TOKEN, "")
            val lt = SharedUtil.read(Const.Auth.LOGIN_TYPE, -1)
            return u > 0 && !TextUtils.isEmpty(t) && lt >= 0
        }

    /**
     * 获取当前登录用户的id。
     * @return 当前登录用户的id。
     */
    val userId: Long
        get() = SharedUtil.read(Const.Auth.USER_ID, 0L)

    /**
     * 获取当前登录用户的token。
     * @return 当前登录用户的token。
     */
    val token: String
        get() = SharedUtil.read(Const.Auth.TOKEN, "")

    /**
     * 获取服务器校验码。使用和服务器端相同的算法生成服务器校验码，对接口的安全性进行保护，防止对服务器进行恶意攻击。
     * @param params
     * 参与生成服务器校验码的参数。
     * @return 服务器校验码。
     */
    fun getServerVerifyCode(vararg params: String): String {
        if (params.isNotEmpty()) {
            val builder = StringBuilder()
            var needSeparator = false
            for (param in params) {
                if (needSeparator) {
                    builder.append(",")
                }
                builder.append(param)
                needSeparator = true
            }
            return MD5.encrypt(builder.toString())
        }
        return ""
    }

}