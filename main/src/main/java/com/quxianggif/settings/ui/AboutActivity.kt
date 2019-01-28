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

package com.quxianggif.settings.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.Button
import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.core.GifFun
import com.quxianggif.core.util.GlobalUtil
import kotlinx.android.synthetic.main.activity_about.*

open class AboutActivity : BaseActivity() {

    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    override fun setupViews() {
        setupToolbar()
        button = actionButton
        val version = "${GlobalUtil.getString(R.string.version)} ${GlobalUtil.appVersionName}"
        aboutVersion.text = version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            openSourceListTextView.text = Html.fromHtml("<u>"+ GlobalUtil.getString(R.string.open_source_project_list) +"</u>", 0)
        } else {
            openSourceListTextView.text = Html.fromHtml("<u>"+ GlobalUtil.getString(R.string.open_source_project_list) +"</u>")
        }
        logo.setImageDrawable(GlobalUtil.getAppIcon())
        openSourceListTextView.setOnClickListener {
            OpenSourceProjectsActivity.actionStart(this)
        }
    }

    companion object {

        const val TAG = "AboutActivity"

        private val ACTION_VIEW_ABOUT = "${GifFun.getPackageName()}.ACTION_VIEW_ABOUT"

        fun actionStart(context: Context) {
            context.startActivity(Intent(ACTION_VIEW_ABOUT))
        }
    }
}
