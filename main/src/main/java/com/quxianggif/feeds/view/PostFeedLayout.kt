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

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ScrollView

import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.dp2px
import com.quxianggif.feeds.ui.PostFeedActivity
import kotlinx.android.synthetic.main.activity_post_feed.*

/**
 * 发送Feed界面的主布局，通过这个界面可以监听到键盘弹出的情况，如果键盘弹出，则将包含GIF图片的ScrollView滚动到底部。
 *
 * @author guolin
 * @since 17/4/1
 */
class PostFeedLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    /**
     * PostFeedActivity的实例。
     */
    internal var activity: PostFeedActivity = context as PostFeedActivity

    /**
     * 获取GIF图片布局的Params。
     *
     * @return GIF图片布局的Params。
     */
    private val selectedGifLayoutParams: LinearLayout.LayoutParams
        get() = activity.selectedGifLayout.layoutParams as LayoutParams

    /**
     * 获取内容输入框的Params。
     *
     * @return 内容输入框的Params。
     */
    private val contentEditParams: LinearLayout.LayoutParams
        get() = activity.contentEdit.layoutParams as LayoutParams

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (isKeyboardShown(height, oldHeight)) {
            if (isGifLayoutNeedToScroll(height)) {
                resetLayoutForKeyboardShown()
            }
        } else {
            resetLayoutForKeyboardHidden()
        }
    }

    /**
     * 根据布局大小的变化来判断键盘是否弹出。
     *
     * @param newHeight
     * 布局大小变化后新的高度
     * @param oldHeight
     * 布局大小变化后原来的高度
     * @return 键盘弹出返回true，否则返回false。
     */
    private fun isKeyboardShown(newHeight: Int, oldHeight: Int): Boolean {
        return newHeight < oldHeight
    }

    /**
     * 判断当前界面的可显示区域的高度是否需要滚动GIF图片。
     *
     * @param height
     * 可显示区域的大小
     * @return 需要滚动GIF图片返回true，否则返回false。
     */
    private fun isGifLayoutNeedToScroll(height: Int): Boolean {
        val toolbarHeight = activity.toolbar!!.height // 工具栏的高度
        val selectedGifLayoutHeight = activity.overrideHeight // GIF布局的高度
        val contentEditHeight = dp2px(55f) // 内容输入框在键盘弹出时的默认高度
        val needHeight = toolbarHeight + selectedGifLayoutHeight + contentEditHeight
        return height < needHeight
    }

    /**
     * 当键盘弹出时，重置GIF布局和内容输入框的布局。
     */
    private fun resetLayoutForKeyboardShown() {
        GifFun.getHandler().post {
            val contentEditParams = contentEditParams
            val selectedGifLayoutParams = selectedGifLayoutParams
            contentEditParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            contentEditParams.weight = 0f
            // 键盘弹出时，限定内容输入框的最大行数是2，否则会出现文字覆盖图片的情况
            activity.contentEdit.maxLines = 2
            selectedGifLayoutParams.height = 0
            selectedGifLayoutParams.weight = 1f
            // 为了产生弹出键盘将图片底上去的效果，需要将GIF布局滚动到底部
            scrollSelectedGifLayout()
        }
    }

    /**
     * 当键盘隐藏时，重置GIF布局和内容输入框的布局。
     */
    private fun resetLayoutForKeyboardHidden() {
        GifFun.getHandler().post {
            val contentEditParams = contentEditParams
            val selectedGifLayoutParams = selectedGifLayoutParams
            contentEditParams.height = 0
            contentEditParams.weight = 1f
            activity.contentEdit.maxLines = Integer.MAX_VALUE
            selectedGifLayoutParams.weight = 0f
            selectedGifLayoutParams.height = activity.overrideHeight
        }
    }

    /**
     * 将GIF布局滚动到底部。
     */
    private fun scrollSelectedGifLayout() {
        GifFun.getHandler().post { activity.selectedGifLayout.fullScroll(ScrollView.FOCUS_DOWN) }
    }

}
