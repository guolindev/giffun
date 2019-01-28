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

import com.quxianggif.core.model.BaseFeed
import com.quxianggif.network.request.SearchFeedsRequest

/**
 * Feed搜索请求的实体类封装。
 *
 * @author guolin
 * @since 18/7/25
 */
class SearchFeeds : Response() {

    var data: List<BaseFeed> = ArrayList()

    companion object {

        fun getResponse(keyword: String, callback: Callback) {
            SearchFeedsRequest()
                    .keyword(keyword)
                    .listen(callback)
        }

        fun getResponse(keyword: String, lastFeed: Long, callback: Callback) {
            SearchFeedsRequest()
                    .keyword(keyword)
                    .lastFeed(lastFeed)
                    .listen(callback)
        }
    }

}
