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

package com.quxianggif.network.model

import com.google.gson.annotations.SerializedName

/**
 * 第三方登录的实体类基类。
 *
 * @author guolin
 * @since 17/6/22
 */
open class ThirdPartyLogin : Response() {

    /**
     * 用户的账号id。
     */
    @SerializedName("user_id")
    var userId: Long = 0

    /**
     * 记录用户的登录身份，token有效期30天。
     */
    var token = ""

    /**
     * 用户在第三方平台上所使用的昵称，如果账号未注册时会返回此参数，客户端可以使用此参数来引导用户注册，在注册时给出建议的昵称。
     */
    var nickname = ""

    var openid = ""

    @SerializedName("access_token")
    var accessToken = ""

}