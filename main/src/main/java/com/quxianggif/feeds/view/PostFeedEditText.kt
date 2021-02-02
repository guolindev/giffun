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
import androidx.appcompat.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * 自定义EditText控件，解决多行显示EditText和imeOptions冲突的问题。
 *
 * @author guolin
 * @since 17/4/1
 */
class PostFeedEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val connection = super.onCreateInputConnection(outAttrs)
        val imeActions = outAttrs.imeOptions and EditorInfo.IME_MASK_ACTION
        if (imeActions and EditorInfo.IME_ACTION_SEND != 0) {
            // clear the existing action
            outAttrs.imeOptions = outAttrs.imeOptions xor imeActions
            // set the SEND action
            outAttrs.imeOptions = outAttrs.imeOptions or EditorInfo.IME_ACTION_SEND
        }
        if (outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) {
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
        }
        return connection
    }
}
