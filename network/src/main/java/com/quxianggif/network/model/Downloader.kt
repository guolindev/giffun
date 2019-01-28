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

import android.os.AsyncTask
import android.text.TextUtils
import com.quxianggif.core.extension.logDebug
import com.quxianggif.core.extension.logError
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 网络下载器，可以从网络上下载任意指定内容。
 *
 * @author guolin
 * @since 17/3/1
 */
object Downloader {

    private const val TAG = "Downloader"

    /**
     * 开始下载。
     *
     * @param url
     * 下载目标的url地址
     * @param filePath
     * 文件存储到本地的路径
     * @param l
     * 下载的监听器
     * @return 下载任务对象，可以使用该对象来操纵下载任务，比如取消下载。
     */
    fun startDownload(url: String, filePath: String, l: DownloadListener): DownloadTask? {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filePath)) {
            logDebug(TAG, "param invalid: url=$url filePath=$filePath DownloadListener=$l")
            return null
        }
        val task = DownloadTask(l)
        task.execute(url, filePath)
        return task
    }

    /**
     * 在这里开始真正的下载任务，并实时回调下载进度。
     */
    open class DownloadTask internal constructor(private var l: DownloadListener) : AsyncTask<String, Int, String>() {

        private var e: Exception? = null

        private var bufferSize = 2048

        private var interrupt = false

        /**
         * 获取OkHttpClient的实例，用于操作OkHttp的所有功能。
         *
         * @return OkHttpClient的实例
         */
        private val okHttpClient: OkHttpClient
            get() = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build()

        override fun doInBackground(vararg params: String): String? {
            var sink: BufferedSink? = null
            var source: BufferedSource? = null
            try {
                val address = params[0]
                val filePath = params[1]
                val request = Request.Builder().url(address).build()
                val response = okHttpClient.newCall(request).execute()
                if (response != null && response.isSuccessful) {
                    val fileDir = getFileDir(filePath)
                    if (fileDir != null) {
                        val dir = File(fileDir)
                        if (!dir.exists()) {
                            dir.mkdirs()
                        }
                    }
                    val file = File(filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                    sink = Okio.buffer(Okio.sink(file))
                    val responseBody = response.body()
                    if (responseBody != null) {
                        source = responseBody.source()
                        val contentLength = responseBody.contentLength()
                        val buffer = ByteArray(bufferSize)
                        var total = 0
                        val downloadedLength: Long = 0
                        var bytes = source.read(buffer)
                        while (bytes >= 0) {
                            if (!interrupt) {
                                sink.write(buffer, 0, bytes)
                                sink.flush()
                                total += bytes
                                val downloadPercent = ((total + downloadedLength) * 1.0 / ((contentLength + downloadedLength) * 1.0) * 100).toInt()
                                publishProgress(downloadPercent)
                                logDebug(TAG, "download percent is $downloadPercent")
                                bytes = source.read(buffer)
                            } else {
                                file.delete()
                                response.close()
                                return null
                            }
                        }
                    } else {
                        file.delete()
                        response.close()
                        e = RuntimeException("OkHttp Response body is null")
                        return null
                    }
                    response.close()
                    return file.path
                }
            } catch (ex: Exception) {
                e = ex
                logError(TAG, ex.message, ex)
            } finally {
                try {
                    sink?.close()
                    source?.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }

            }
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            val percent = values[0]
            if (percent != null) {
                l.onProgress(percent)
            }
        }

        override fun onPostExecute(filePath: String?) {
            if (filePath != null) {
                l.onCompleted(filePath)
            } else {
                if (e != null) {
                    e?.let {
                        l.onFailure(it.message.toString(), it)
                    }
                } else {
                    l.onFailure("Canceled by user or server response failed.", RuntimeException("Canceled by user or server response failed."))
                }
            }
        }

        /**
         * 获取当前文件路径所处的目录。
         * @param filePath
         * 完整的文件路径
         * @return 文件路径所处的目录。
         */
        private fun getFileDir(filePath: String): String? {
            if (!TextUtils.isEmpty(filePath)) {
                val endIndex = filePath.lastIndexOf("/")
                if (endIndex != -1) {
                    return filePath.substring(0, endIndex)
                }
            }
            return null
        }

        /**
         * 中断下载，会以失败为结果进行回调
         */
        fun cancel() {
            interrupt = true
        }

    }

}
