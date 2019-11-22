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

package com.quxianggif.common.callback

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


/**
 * A scroll listener for RecyclerView to load more items as you approach the end.
 *
 * Adapted from https://gist.github.com/ssinss/e06f12ef66c51252563e
 */
abstract class InfiniteScrollListener(private val layoutManager: RecyclerView.LayoutManager) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        // bail out if scrolling upward or already loading data
        if (dy < 0 || isDataLoading() || isNoMoreData()) return

        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        var firstVisibleItem: Int? = null
        if (layoutManager is LinearLayoutManager) {
            firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val items = layoutManager.findFirstVisibleItemPositions(null)
            firstVisibleItem = items.min()
        }
        if (firstVisibleItem != null) {
            if (totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD) {
                onLoadMore()
            }
        }
    }

    abstract fun onLoadMore()

    abstract fun isDataLoading(): Boolean

    abstract fun isNoMoreData(): Boolean

    companion object {

        private const val TAG = "InfiniteScrollListener"

        // The minimum number of items remaining before we should loading more.
        private const val VISIBLE_THRESHOLD = 1
    }

}