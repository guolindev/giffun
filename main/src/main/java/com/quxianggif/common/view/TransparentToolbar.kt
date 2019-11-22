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
import androidx.appcompat.widget.Toolbar
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * 用于需要透明化显示的Toolbar。使用这种Toolbar后，即使被Toolbar盖住的区域依然可以点击，解决了被Toolbar盖住的部分明明
 * 看得到，却无法点击的问题。
 *
 * @author guolin
 * @since 17/8/28
 */
class TransparentToolbar : Toolbar {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

}
