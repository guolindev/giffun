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

package com.quxianggif.feeds.view

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.view.View

import com.quxianggif.common.adapter.WaterFallFeedAdapter
import com.quxianggif.core.extension.dp2px

/**
 * 实现主界面Feed的左右两边间距相同的功能。
 *
 * @author guolin
 * @since 17/7/31
 */
class SpaceItemDecoration(private val adapter: RecyclerView.Adapter<*>) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val spanIndex = (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex
        val type = adapter.getItemViewType(position)
        when (type) {
            WaterFallFeedAdapter.TYPE_FEEDS -> if (spanIndex == 0) {
                outRect.left = dp2px(12f)
                outRect.right = dp2px(6f)
            } else { //if you just have 2 span . Or you can use (staggeredGridLayoutManager.getSpanCount()-1) as last span
                outRect.left = dp2px(6f)
                outRect.right = dp2px(12f)
            }
        }
    }

}
