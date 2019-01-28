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

package com.quxianggif.util.glide

import android.content.Context

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule

import java.io.InputStream

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 自定义Glide模块，用于修改Glide默认的配置。
 *
 * @author guolin
 * @since 2017/10/23
 */

class GifFunModule : GlideModule {

    override fun applyOptions(context: Context, builder: GlideBuilder) {}

    override fun registerComponents(context: Context, glide: Glide) {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(ProgressInterceptor())
        builder.connectTimeout(3000, TimeUnit.MILLISECONDS)
        builder.readTimeout(6000, TimeUnit.MILLISECONDS)
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpGlideUrlLoader.Factory(builder.build()))
    }
}