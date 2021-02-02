/*
 * Copyright 2015 Google Inc.
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

import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.Window
import com.quxianggif.R
import com.quxianggif.core.extension.logWarn

/**
 * Utility methods for working with Views.
 */
object ViewUtils {

    private val TAG = "ViewUtils"

    /**
     * 设置Toolbar上的图标颜色。
     * @param isDark
     * true表示设置成深色，false表示设置成浅色。
     */
    fun setToolbarIconColor(activity: AppCompatActivity, toolbar: Toolbar, isDark: Boolean) {
        try {
            // change back button color.
            val color: Int
            if (isDark) {
                color = ContextCompat.getColor(activity, R.color.black_text)
            } else {
                color = ContextCompat.getColor(activity, R.color.white_text)
            }
            val backArrow = ContextCompat.getDrawable(activity, R.drawable.abc_ic_ab_back_material)
            backArrow?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            val actionBar = activity.supportActionBar
            actionBar?.setHomeAsUpIndicator(backArrow)
            // change overflow button color.
            var drawable = toolbar.overflowIcon
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable)
                DrawableCompat.setTint(drawable!!.mutate(), color)
                toolbar.overflowIcon = drawable
            }
            // change title text color.
            toolbar.setTitleTextColor(color)
        } catch (e: Exception) {
            logWarn(TAG, e.message, e)
        }

    }

    fun setLightStatusBar(window: Window, view: View) {
        if (OSUtil.isMiUI8OrLower) {
            setMiUIStatusBarLightMode(window, true)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var flags = view.systemUiVisibility
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                view.systemUiVisibility = flags
            }
        }
    }

    fun clearLightStatusBar(window: Window, view: View) {
        if (OSUtil.isMiUI8OrLower) {
            setMiUIStatusBarLightMode(window, false)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var flags = view.systemUiVisibility
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                view.systemUiVisibility = flags
            }
        }
    }

    fun viewsIntersect(view1: View?, view2: View?): Boolean {
        if (view1 == null || view2 == null) return false

        val view1Loc = IntArray(2)
        view1.getLocationOnScreen(view1Loc)
        val view1Rect = Rect(view1Loc[0],
                view1Loc[1],
                view1Loc[0] + view1.width,
                view1Loc[1] + view1.height)
        val view2Loc = IntArray(2)
        view2.getLocationOnScreen(view2Loc)
        val view2Rect = Rect(view2Loc[0],
                view2Loc[1],
                view2Loc[0] + view2.width,
                view2Loc[1] + view2.height)
        return view1Rect.intersect(view2Rect)
    }

    /**
     * 设置小米手机状态栏字体图标颜色模式，需要MIUI 6以上系统才支持。
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     */
    fun setMiUIStatusBarLightMode(window: Window?, dark: Boolean) {
        if (window != null) {
            val clazz = window.javaClass
            try {
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                val darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                if (dark) { // 将状态栏字体和图标设成黑色
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)
                } else { // 将状态栏字体设成原色
                    extraFlagField.invoke(window, 0, darkModeFlag)
                }
            } catch (e: Exception) {
                logWarn(TAG, e.message, e)
            }

        }
    }

}
