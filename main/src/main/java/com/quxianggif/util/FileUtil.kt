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

import com.quxianggif.core.extension.logError
import java.io.*

/**
 * 文件操作工具类。
 *
 * @author guolin
 * @since 17/8/3
 */
object FileUtil {

    private const val TAG = "FileUtil"

    /**
     * 复制文件。
     *
     * @param fromFile 源文件
     * @param toFile   目标文件
     */
    fun copyFile(fromFile: File, toFile: File): Boolean {
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            val fis = FileInputStream(fromFile)
            bis = BufferedInputStream(fis)
            val out = FileOutputStream(toFile)
            bos = BufferedOutputStream(out)
            val buffer = ByteArray(1024)
            var bytes = bis.read(buffer)
            while (bytes >= 0) {
                bos.write(buffer, 0, bytes)
                bos.flush()
                bytes = bis.read(buffer)
            }
            return true
        } catch (e: Exception) {
           logError(TAG, e.message, e)
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (bis != null) {
                try {
                    bis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return false
    }

}
