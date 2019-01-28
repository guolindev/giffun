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

package com.quxianggif.opensource.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.quxianggif.core.GifFun
import kotlinx.android.synthetic.main.activity_login.view.*

/**
 * 自定义登录界面Layout，监听布局高度的变化，如果高宽比小于4:3说明此时键盘弹出，应改变布局的比例结果以保证所有元素
 * 都不会被键盘遮挡。
 *
 * @author guolin
 * @since 2019/1/12
 */
class LoginLayout(context: Context, attributes: AttributeSet) : LinearLayout(context, attributes) {

    var keyboardShowed = false

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            val width = right - left
            val height = bottom - top
            if (height.toFloat() / width.toFloat() < 4f / 3f) { // 如果高宽比小于4:3说明此时键盘弹出
                post {
                    loginBgWallLayout.visibility = View.INVISIBLE
                    val params = loginLayoutTop.layoutParams as LayoutParams
                    params.weight = 1.5f
                    keyboardShowed = true
                    loginLayoutTop.requestLayout()
                }
            } else {
                if (keyboardShowed) {
                    post {
                        loginBgWallLayout.visibility = View.VISIBLE
                        val params = loginLayoutTop.layoutParams as LayoutParams
                        params.weight = 6f
                        loginLayoutTop.requestLayout()
                    }
                }
            }
        }
    }

}