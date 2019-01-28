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

import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GenericLoaderFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory

import java.io.InputStream

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 自定义的OkHttp ModelLoader，用于使用OkHttp的方向来进行Glide网络请求。
 * @author guolin
 * @since 2017/10/23
 */

internal class OkHttpGlideUrlLoader(private val okHttpClient: OkHttpClient) : ModelLoader<GlideUrl, InputStream> {

    class Factory : ModelLoaderFactory<GlideUrl, InputStream> {

        private var client: OkHttpClient? = null

        private val okHttpClient: OkHttpClient
            @Synchronized get() {
                if (client == null) {
                    client = OkHttpClient.Builder().build()
                }
                return client!!
            }

        constructor() {}

        constructor(client: OkHttpClient) {
            this.client = client
        }

        override fun build(context: Context, factories: GenericLoaderFactory): ModelLoader<GlideUrl, InputStream> {
            return OkHttpGlideUrlLoader(okHttpClient)
        }

        override fun teardown() {}
    }

    override fun getResourceFetcher(model: GlideUrl, width: Int, height: Int): DataFetcher<InputStream> {
        return OkHttpFetcher(okHttpClient, model)
    }
}