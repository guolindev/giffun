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
import kotlinx.android.synthetic.main.activity_login.view.loginBgWallLayout
import kotlinx.android.synthetic.main.activity_login.view.loginInputElements
import kotlinx.android.synthetic.main.activity_login.view.loginLayoutBottom
import kotlinx.android.synthetic.main.activity_login.view.loginLayoutTop

/**
 * 自定义登录界面Layout，监听布局高度的变化，如果高宽比小于4:3说明此时键盘弹出，应改变布局的比例结果以保证所有元素
 * 都不会被键盘遮挡。
 *
 * @author guolin
 * @since 2019/1/12
 */
class LoginLayout(context: Context, attributes: AttributeSet) : LinearLayout(context, attributes) {

    var keyboardShowed = false

    var loginInputElementsPreferHeight = -1

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 键盘弹出后 loginLayoutBottom 的最小高度为
        if (loginInputElementsPreferHeight == -1) loginInputElementsPreferHeight = loginInputElements.height
        if (changed) {
            val width = right - left
            val height = bottom - top
            if (height.toFloat() / width.toFloat() < 4f / 3f) { // 如果高宽比小于4:3说明此时键盘弹出
                // 按照 1.5 / 4 比例计算 loginLayoutBottom 的高度
                val loginInputElementsAvailableHeight = (height * (4 / 5.5)).toInt()
                // 如 loginLayoutBottom 可以承载 loginInputElements 的高度则直接按照比例分配
                // 反之则优先分配 loginLayoutBottom 高度, 剩余高度设置给 loginLayoutTop
                if (loginInputElementsAvailableHeight >= loginInputElementsPreferHeight){
                    post {
                        loginBgWallLayout.visibility = View.INVISIBLE
                        processLayoutParams(loginLayoutTop, 0, 1.5f)
                        processLayoutParams(loginLayoutBottom, 0, 4f)
                        keyboardShowed = true
                        requestLayout()
                    }
                } else {
                    post {
                        loginBgWallLayout.visibility = View.INVISIBLE
                        processLayoutParams(loginLayoutTop, height - loginInputElementsPreferHeight, 0f)
                        processLayoutParams(loginLayoutBottom, loginInputElementsPreferHeight, 0f)
                        keyboardShowed = true
                        requestLayout()
                    }
                }
            } else {
                if (keyboardShowed) {
                    post {
                        loginBgWallLayout.visibility = View.VISIBLE
                        processLayoutParams(loginLayoutTop, 0, 6f)
                        processLayoutParams(loginLayoutBottom, 0, 4f)
                        loginLayoutTop.requestLayout()
                    }
                }
            }
        }
    }

    private fun processLayoutParams(view: View, height: Int, weight: Float){
        val params = view.layoutParams as LayoutParams
        params.height = height
        params.weight = weight
    }

}