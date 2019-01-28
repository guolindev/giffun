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

package com.quxianggif.core.util

import android.content.Context
import android.net.ConnectivityManager
import com.quxianggif.core.GifFun

/**
 * 判断设备当前网络状态的工具类。
 *
 * @author guolin
 * @since 2018/5/19
 */
object NetworkUtil {

    const val NO_NETWORK = 0

    const val WIFI = 1

    const val MOBILE = 2

    const val UNKNOWN = -1

    fun checkNetwork(): Int {
        val manager = GifFun.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return when {
            wifi.isConnected -> WIFI
            mobile.isConnected -> MOBILE
            else -> NO_NETWORK
        }
    }

}
