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

package com.quxianggif.core.model

import com.google.gson.annotations.SerializedName

/**
 * User实体类，用于存储服务器返回的用户基本信息数据。
 *
 * @author guolin
 * @since 17/7/30
 */
class User : Model(), SearchItem {
    override val modelId: Long
        get() = userId

    @SerializedName("user_id")
    var userId: Long = 0

    var nickname: String = ""

    var avatar: String = ""

    @SerializedName("bg_image")
    var bgImage: String = ""

    var description: String = ""

    @SerializedName("followers_count")
    var followersCount: Int = 0

    @SerializedName("followings_count")
    var followingsCount: Int = 0

    @SerializedName("feeds_count")
    var feedsCount: Int = 0

    @SerializedName("is_following")
    var isFollowing: Boolean = false

}
