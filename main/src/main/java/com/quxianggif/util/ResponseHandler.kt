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

package com.quxianggif.util

import com.quxianggif.R
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToastOnUiThread
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.ForceToLoginEvent
import com.quxianggif.network.exception.ResponseCodeException
import com.quxianggif.network.model.Response
import org.greenrobot.eventbus.EventBus
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException

/**
 * 对服务器的返回进行相应的逻辑处理。注意此类只处理公众的返回逻辑，涉及具体的业务逻辑，仍然交由接口调用处自行处理。
 *
 * @author guolin
 * @since 17/2/21
 */
object ResponseHandler {

    private val TAG = "ResponseHandler"

    /**
     * 当网络请求正常响应的时候，根据状态码处理通用部分的逻辑。
     * @param response
     * 响应实体类
     * @return 如果已经将该响应处理掉了，返回true，否则返回false。
     */
    fun handleResponse(response: Response?): Boolean {
        if (response == null) {
            logWarn(TAG, "handleResponse: response is null")
            showToastOnUiThread(GlobalUtil.getString(R.string.unknown_error))
            return true
        }
        val status = response.status
        when (status) {
            10001, 10002, 10003 -> {
                logWarn(TAG, "handleResponse: status code is $status")
                GifFun.logout()
                showToastOnUiThread(GlobalUtil.getString(R.string.login_status_expired))
                val event = ForceToLoginEvent()
                EventBus.getDefault().post(event)
                return true
            }
            19000 -> {
                logWarn(TAG, "handleResponse: status code is 19000")
                showToastOnUiThread(GlobalUtil.getString(R.string.unknown_error))
                return true
            }
            else -> return false
        }
    }

    /**
     * 当网络请求没有正常响应的时候，根据异常类型进行相应的处理。
     * @param e
     * 异常实体类
     */
    fun handleFailure(e: Exception) {
        when (e) {
            is ConnectException -> showToastOnUiThread(GlobalUtil.getString(R.string.network_connect_error))
            is SocketTimeoutException -> showToastOnUiThread(GlobalUtil.getString(R.string.network_connect_timeout))
            is ResponseCodeException -> showToastOnUiThread(GlobalUtil.getString(R.string.network_response_code_error) + e.responseCode)
            is NoRouteToHostException -> showToastOnUiThread(GlobalUtil.getString(R.string.no_route_to_host))
            else -> {
                logWarn(TAG, "handleFailure exception is $e")
                showToastOnUiThread(GlobalUtil.getString(R.string.unknown_error))
            }
        }
    }

}