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

package com.quxianggif

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.quxianggif.core.GifFun
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import org.litepal.LitePal

/**
 * GifFun自定义Application，在这里进行全局的初始化操作。
 *
 * @author guolin
 * @since 17/2/15
 */
open class GifFunApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        GifFun.initialize(this)
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null)
        MobclickAgent.setCatchUncaughtExceptions(false) // 关闭友盟的崩溃采集功能，使用腾讯Bugly
        LitePal.initialize(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}