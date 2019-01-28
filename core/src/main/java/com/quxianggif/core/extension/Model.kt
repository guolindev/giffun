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

import com.quxianggif.core.GifFun
import com.quxianggif.core.model.Model
import kotlin.concurrent.thread

/**
 * 查询Model并回调集合中第一条符合给定参数条件元素的下标，如未查到则回调-1。
 */
fun <T : Model> findModelIndex(models: List<T>?, modelId: Long, action: (index: Int) -> Unit) {
    thread {
        var index = -1
        if (models != null && !models.isEmpty()) {
            for (i in models.indices) {
                val model = models[i]
                if (model.modelId == modelId) {
                    index = i
                    break
                }
            }
        }
        GifFun.getHandler().post {
            action(index)
        }
    }
}

/**
 * 查询Model并回调集合中第一条符合给定参数条件元素的下标，如未查到则不进行回调。
 */
fun <T : Model> searchModelIndex(models: List<T>?, modelId: Long, action: (index: Int) -> Unit) {
    thread {
        var index = -1
        if (models != null && !models.isEmpty()) {
            for (i in models.indices) {
                val model = models[i]
                if (model.modelId == modelId) {
                    index = i
                    break
                }
            }
        }
        if (index != -1) {
            GifFun.getHandler().post {
                action(index)
            }
        }
    }
}