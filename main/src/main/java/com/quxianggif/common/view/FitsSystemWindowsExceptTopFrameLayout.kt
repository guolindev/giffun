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

package com.quxianggif.common.view

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout

/**
 * 解决FrameLayout中嵌套了EditText，然后Activity使用了透明状态栏后，键盘弹出会将EditText覆盖的问题。使用这种特殊
 * 定制的FrameLayout可以避免此问题的出现。
 *
 * @author guolin
 * @since 18/1/8
 */
class FitsSystemWindowsExceptTopFrameLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPadding(insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom)
            return insets.replaceSystemWindowInsets(0, insets.systemWindowInsetTop, 0, 0)
        } else {
            return super.onApplyWindowInsets(insets)
        }
    }
}