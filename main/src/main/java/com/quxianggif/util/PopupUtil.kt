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

package com.quxianggif.util

import android.annotation.SuppressLint
import android.app.Activity
import androidx.core.content.ContextCompat
import android.util.Pair
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.PopupWindow
import com.quxianggif.R
import com.quxianggif.core.extension.dp2px

/**
 * 用于构建PopupWindow类型的弹出式菜单。
 *
 * @author guolin
 * @since 17/9/8
 */
object PopupUtil {

    fun showUserFeedExpandMenu(activity: Activity, expandMenuItems: List<String>, anchor: View): Pair<PopupWindow, ListView> {
        @SuppressLint("InflateParams")
        val popupView = activity.layoutInflater.inflate(R.layout.expand_menu, null)
        val expandMenuList = popupView.findViewById<ListView>(R.id.expandMenuList)
        expandMenuList.adapter = ArrayAdapter(activity, R.layout.user_feed_more_item, expandMenuItems)
        val windowWidth = dp2px(100f)
        val windowHeight = dp2px(46f) * expandMenuItems.size
        val window = PopupWindow(popupView, windowWidth, windowHeight)
        window.isFocusable = true
        window.isOutsideTouchable = true
        window.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.expand_menu_bg))
        window.update()
        window.showAsDropDown(anchor, -(windowWidth - dp2px(30f)), -dp2px(8f))
        return Pair(window, expandMenuList)
    }

    fun showCommentExpandMenu(activity: Activity, expandMenuItems: List<String>, anchor: View): Pair<PopupWindow, ListView> {
        @SuppressLint("InflateParams")
        val popupView = activity.layoutInflater.inflate(R.layout.expand_menu, null)
        val expandMenuList = popupView.findViewById<ListView>(R.id.expandMenuList)
        expandMenuList.adapter = ArrayAdapter(activity, R.layout.comment_more_item, expandMenuItems)
        val windowWidth = dp2px(90f)
        val windowHeight = dp2px(41f) * expandMenuItems.size
        val window = PopupWindow(popupView, windowWidth, windowHeight)
        window.isFocusable = true
        window.isOutsideTouchable = true
        window.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.expand_menu_bg))
        window.update()
        window.showAsDropDown(anchor, -(windowWidth - dp2px(5f)), dp2px(-25f))
        return Pair(window, expandMenuList)
    }

}
