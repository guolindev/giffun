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

import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.network.model.Response

/**
 * 对服务器的返回进行相应的逻辑处理。注意此类只处理公众的返回逻辑，涉及具体的业务逻辑，仍然交由接口调用处自行处理。
 *
 * @author guolin
 * @since 17/2/21
 */
object ResHandler {

    /**
     * 当网络请求正常响应的时候，根据状态码处理通用部分的逻辑。
     * @param response
     * 响应实体类
     * @return 如果已经将该响应处理掉了，返回true，否则返回false。
     */
    fun handleResponse(response: Response?): Boolean {
        if (response == null) {
            showToast(GlobalUtil.getString(GlobalUtil.getResourceId("unknown_error", "string")))
            return true
        }
        val status = response.status
        return when (status) {
            10001, 10002, 10003 -> {
                GifFun.logout()
                showToast(GlobalUtil.getString(GlobalUtil.getResourceId("login_status_expired", "string")))
                true
            }
            19000 -> {
                showToast(GlobalUtil.getString(GlobalUtil.getResourceId("unknown_error", "string")))
                true
            }
            else -> false
        }
    }

}