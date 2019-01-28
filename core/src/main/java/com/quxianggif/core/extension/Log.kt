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

import android.util.Log
import com.quxianggif.core.GifFun

/**
 * 日志操作的扩展工具类。
 *
 * @author guolin
 * @since 2018/10/1
 */

private const val VERBOSE = 1
private const val DEBUG = 2
private const val INFO = 3
private const val WARN = 4
private const val ERROR = 5
private const val NOTHING = 6

//    private val level = VERBOSE;
private val level = if (GifFun.isDebug) VERBOSE else WARN

fun Any.logVerbose(msg: String?) {
    if (level <= VERBOSE) {
        Log.v(javaClass.simpleName, msg.toString())
    }
}

fun Any.logDebug(msg: String?) {
    if (level <= DEBUG) {
        Log.d(javaClass.simpleName, msg.toString())
    }
}

fun Any.logInfo(msg: String?) {
    if (level <= INFO) {
        Log.i(javaClass.simpleName, msg.toString())
    }
}

fun Any.logWarn(msg: String?, tr: Throwable? = null) {
    if (level <= WARN) {
        if (tr == null) {
            Log.w(javaClass.simpleName, msg.toString())
        } else {
            Log.w(javaClass.simpleName, msg.toString(), tr)
        }
    }
}

fun Any.logError(msg: String?, tr: Throwable) {
    if (level <= ERROR) {
        Log.e(javaClass.simpleName, msg.toString(), tr)
    }
}

fun logVerbose(tag: String, msg: String?) {
    if (level <= VERBOSE) {
        Log.v(tag, msg.toString())
    }
}

fun logDebug(tag: String, msg: String?) {
    if (level <= DEBUG) {
        Log.d(tag, msg.toString())
    }
}

fun logInfo(tag: String, msg: String?) {
    if (level <= INFO) {
        Log.i(tag, msg.toString())
    }
}

fun logWarn(tag: String, msg: String?, tr: Throwable? = null) {
    if (level <= WARN) {
        if (tr == null) {
            Log.w(tag, msg.toString())
        } else {
            Log.w(tag, msg.toString(), tr)
        }
    }
}

fun logError(tag: String, msg: String?, tr: Throwable) {
    if (level <= ERROR) {
        Log.e(tag, msg.toString(), tr)
    }
}