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
import com.quxianggif.network.exception.SearchException
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.SearchMixed
import com.quxianggif.network.util.NetworkConst
import okhttp3.Headers
import java.util.*

/**
 * 执行聚合搜索的请求。对应服务器接口：/search/mixed
 *
 * @author guolin
 * @since 18/7/25
 */
class SearchMixedRequest : Request() {

    private var keyword = ""

    fun keyword(keyword: String): SearchMixedRequest {
        this.keyword = keyword
        return this
    }

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.POST
    }

    override fun listen(callback: Callback?) {
        if (keyword.isBlank()) {
            callback?.onFailure(SearchException(SearchException.KEYWORD_IS_BLANK))
        } else {
            setListener(callback)
            inFlight(SearchMixed::class.java)
        }
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        return if (buildAuthParams(params)) {
            params[NetworkConst.KEYWORD] = keyword
            params
        } else super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.UID, NetworkConst.KEYWORD, NetworkConst.TOKEN)
        return super.headers(builder)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/search/mixed"
    }

}
