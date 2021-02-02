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

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.quxianggif.R

/**
 * 评论界面Item的Holder。
 *
 * @author guolin
 * @since 17/7/23
 */
class CommentHolder(view: View) : RecyclerView.ViewHolder(view) {

    val avatar: ImageView = view.findViewById(R.id.avatar)

    val nickname: TextView = view.findViewById(R.id.nickname)

    val postDate: TextView = view.findViewById(R.id.postDate)

    val content: TextView = view.findViewById(R.id.content)

    val goodCount: TextView = view.findViewById(R.id.goodCount)

    val goodImage: ImageView = view.findViewById(R.id.goodImage)

    val moreButton: ImageView? = view.findViewById(R.id.moreButton)

}