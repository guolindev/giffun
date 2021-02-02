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

package com.quxianggif.settings.adapter

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.quxianggif.R
import com.quxianggif.common.ui.WebViewActivity
import com.quxianggif.settings.model.OpenSourceProject

/**
 * 开源项目列表的RecyclerView适配器。
 * @author guolin
 * @since 2018/6/29
 */
class OpenSourceProjectsAdapter(val activity: Activity, private val projectList: List<OpenSourceProject>) : RecyclerView.Adapter<OpenSourceProjectsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.open_source_project_item, parent, false)
        val holder = ViewHolder(view)
        holder.rootLayout.setOnClickListener {
            val position = holder.adapterPosition
            val project = projectList[position]
            WebViewActivity.actionStart(activity, project.name, project.url)
        }
        return holder
    }

    override fun getItemCount(): Int = projectList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = projectList[position]
        holder.name.text = item.name
        holder.url.text = item.url
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var rootLayout: FrameLayout = view.findViewById(R.id.rootLayout)

        var name: TextView = view.findViewById(R.id.name)

        var url: TextView = view.findViewById(R.id.url)

    }

}