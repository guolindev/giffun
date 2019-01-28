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

/**
 * 网络下载的回调接口
 *
 * @author guolin
 * @since 17/3/1
 */
interface DownloadListener {

    /**
     * 当下载进度变化时回调此方法
     * @param percent 已下载的百分值
     */
    fun onProgress(percent: Int)

    /**
     * 当下载完成时会回调此方法
     */
    fun onCompleted(filePath: String)

    /**
     * 当下载失败时会回调此方法。
     *
     * @param errorMsg 错误信息
     * @param tr       异常对象
     */
    fun onFailure(errorMsg: String, tr: Throwable)

}
