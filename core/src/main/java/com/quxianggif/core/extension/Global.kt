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

package com.quxianggif.core.extension

import android.annotation.SuppressLint
import android.os.Looper
import android.widget.Toast
import com.quxianggif.core.GifFun
import com.quxianggif.core.util.GlobalUtil

/**
 * 定义全局的扩展工具方法。
 * @author guolin
 * @since 2018/8/31
 */


private var toast: Toast? = null

/**
 * 弹出Toast信息。如果不是在主线程中调用此方法，Toast信息将会不显示。
 *
 * @param content
 * Toast中显示的内容
 */
@SuppressLint("ShowToast")
@JvmOverloads
fun showToast(content: String, duration: Int = Toast.LENGTH_SHORT) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        if (toast == null) {
            toast = Toast.makeText(GifFun.getContext(), content, duration)
        } else {
            toast?.setText(content)
        }
        toast?.show()
    }
}

/**
 * 切换到主线程后弹出Toast信息。此方法不管是在子线程还是主线程中，都可以成功弹出Toast信息。
 *
 * @param content
 * Toast中显示的内容
 * @param duration
 * Toast显示的时长
 */
@SuppressLint("ShowToast")
@JvmOverloads
fun showToastOnUiThread(content: String, duration: Int = Toast.LENGTH_SHORT) {
    GifFun.getHandler().post {
        if (toast == null) {
            toast = Toast.makeText(GifFun.getContext(), content, duration)
        } else {
            toast?.setText(content)
        }
        toast?.show()
    }
}