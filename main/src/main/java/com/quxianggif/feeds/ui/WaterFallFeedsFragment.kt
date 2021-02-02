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

package com.quxianggif.feeds.ui

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.quxianggif.R
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.core.extension.dp2px

import org.greenrobot.eventbus.EventBus

/**
 * 展示瀑布流的Feeds内容。
 *
 * @author guolin
 * @since 17/7/24
 */
abstract class WaterFallFeedsFragment : BaseFeedsFragment(), LoadDataListener {

    /**
     * 通过获取屏幕宽度来计算出每张图片的宽度。
     *
     * @return 计算后得出的每张图片的宽度。
     */
    internal val imageWidth: Int
        get() {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay?.getMetrics(metrics)
            val columnWidth = metrics.widthPixels / COLUMN_COUNT
            return columnWidth - dp2px(18f)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_water_fall_feeds, container, false)
        initViews(view)
        EventBus.getDefault().register(this)
        return super.onCreateView(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    override fun setupRecyclerView() {
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        (layoutManager as StaggeredGridLayoutManager).gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    companion object {

        private const val TAG = "WaterFallFeedsFragment"

        private const val COLUMN_COUNT = 2
    }
}