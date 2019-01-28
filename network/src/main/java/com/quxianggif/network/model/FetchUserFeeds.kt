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
import com.quxianggif.core.model.UserFeed
import com.quxianggif.network.request.FetchUserFeedsRequest

/**
 * 获取用户所发Feeds请求的实体类封装。
 *
 * @author guolin
 * @since 17/7/23
 */
class FetchUserFeeds : Response() {

    @SerializedName("user_id")
    var userId: Long = 0

    var nickname: String = ""

    var avatar: String = ""

    @SerializedName("bg_image")
    var bgImage: String = ""

    @SerializedName("feeds_count")
    var feedsCount: Int = 0

    @SerializedName("followings_count")
    var followingsCount: Int = 0

    @SerializedName("followers_count")
    var followersCount: Int = 0

    @SerializedName("is_following")
    var isFollowing: Boolean = false

    var description: String = ""

    @SerializedName("data")
    var feeds: MutableList<UserFeed> = ArrayList()

    companion object {

        fun getResponse(userId: Long, callback: Callback) {
            FetchUserFeedsRequest()
                    .userId(userId)
                    .listen(callback)
        }

        fun getResponse(userId: Long, lastFeed: Long, callback: Callback) {
            FetchUserFeedsRequest()
                    .userId(userId)
                    .lastFeed(lastFeed)
                    .listen(callback)
        }
    }

}