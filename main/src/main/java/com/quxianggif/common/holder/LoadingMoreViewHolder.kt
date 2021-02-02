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

package com.quxianggif.common.holder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.quxianggif.R

/**
 * 用于在RecyclerView当中显示更加更多的进度条。
 *
 * @author guolin
 * @since 17/7/23
 */
class LoadingMoreViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

    val progress: ProgressBar = view.findViewById(R.id.loadProgress)

    val end: ImageView = view.findViewById(R.id.loadingEnd)

    val failed: TextView = view.findViewById(R.id.loadFailed)

    companion object {

        fun createLoadingMoreViewHolder(context: Context, parent: ViewGroup): LoadingMoreViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.loading_footer, parent, false)
            return LoadingMoreViewHolder(view)
        }
    }

}