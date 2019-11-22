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
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.common.view.SimpleDividerDecoration
import com.quxianggif.settings.adapter.OpenSourceProjectsAdapter
import com.quxianggif.settings.model.OpenSourceProject
import kotlinx.android.synthetic.main.activity_open_source_projects.*

class OpenSourceProjectsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source_projects)
    }

    override fun setupViews() {
        setupToolbar()
        val layoutManager = LinearLayoutManager(this)
        val adapter = OpenSourceProjectsAdapter(this, getProjectList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(SimpleDividerDecoration(this))
    }

    private fun getProjectList() = ArrayList<OpenSourceProject>().apply {
        add(OpenSourceProject("Glide", "https://github.com/bumptech/glide"))
        add(OpenSourceProject("Plaid", "https://github.com/nickbutcher/plaid"))
        add(OpenSourceProject("OkHttp", "https://github.com/square/okhttp"))
        add(OpenSourceProject("Gson", "https://github.com/google/gson"))
        add(OpenSourceProject("EventBus", "https://github.com/greenrobot/EventBus"))
        add(OpenSourceProject("LitePal", "https://github.com/LitePalFramework/LitePal"))
        add(OpenSourceProject("CircleImageView", "https://github.com/hdodenhof/CircleImageView"))
        add(OpenSourceProject("Android Image Cropper", "https://github.com/ArthurHub/Android-Image-Cropper"))
        add(OpenSourceProject("Glide Transformations", "https://github.com/wasabeef/glide-transformations"))
        add(OpenSourceProject("PhotoView", "https://github.com/chrisbanes/PhotoView"))
        add(OpenSourceProject("PagerSlidingTabStrip", "https://github.com/astuetz/PagerSlidingTabStrip"))
        add(OpenSourceProject("FileBrowserView", "https://github.com/psaravan/FileBrowserView"))
    }

    companion object {

        const val TAG = "OpenSourceProjectsActivity"

        fun actionStart(context: Context) {
            val intent = Intent(context, OpenSourceProjectsActivity::class.java)
            context.startActivity(intent)
        }

    }

}