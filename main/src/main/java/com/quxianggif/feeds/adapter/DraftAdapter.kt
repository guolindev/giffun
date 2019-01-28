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

package com.quxianggif.feeds.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.feeds.model.Draft
import com.quxianggif.feeds.ui.PostFeedActivity
import org.litepal.LitePal
import org.litepal.extension.delete
import java.text.SimpleDateFormat
import java.util.*

/**
 * 草稿箱列表的RecyclerView适配器。
 * @author guolin
 * @since 2018/6/20
 */
class DraftAdapter(val activity: Activity, private val draftList: MutableList<Draft>) : RecyclerView.Adapter<DraftAdapter.DraftViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.draft_item, parent, false)
        val holder = DraftViewHolder(view)
        holder.rootLayout.setOnClickListener {
            val position = holder.adapterPosition
            val draft = draftList[position]
            PostFeedActivity.actionStart(activity, draft)
            LitePal.delete<Draft>(draft.id)
            draftList.removeAt(position)
            notifyItemRemoved(position)
        }
        return holder
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        val draft = draftList[position]
        holder.content.text = draft.content
        holder.time.text = getDraftTime(draft.time.time)
        Glide.with(activity)
                .load(draft.gifPath)
                .asBitmap()
                .placeholder(R.drawable.loading_bg_rect)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.cover)
    }

    override fun getItemCount() = draftList.size

    private fun getDraftTime(draftMillis: Long): String {
        val currentMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentMillis
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.timeInMillis = draftMillis
        val draftYear = calendar.get(Calendar.YEAR)
        val draftMonth = calendar.get(Calendar.MONTH)
        val draftDay = calendar.get(Calendar.DAY_OF_MONTH)
        return if (currentYear == draftYear && currentMonth == draftMonth && currentDay == draftDay) {
            // 当天的草稿只显示时间
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(draftMillis))
        } else {
            if (currentYear == draftYear) {
                // 当年的草稿只显示月日
                SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(draftMillis))
            } else {
                // 隔年的草稿显示完整年月日
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(draftMillis))
            }
        }
    }

    class DraftViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu.add(0, REMOVE_DRAFT_ITEM, adapterPosition, GlobalUtil.getString(R.string.delete_draft_item))
            menu.add(0, CLEAN_DRAFT_BOX, adapterPosition, GlobalUtil.getString(R.string.clean_draft_box))
        }

        val rootLayout: LinearLayout = view.findViewById(R.id.rootLayout)

        val cover: ImageView = view.findViewById(R.id.cover)

        val content: TextView = view.findViewById(R.id.content)

        val time: TextView = view.findViewById(R.id.time)

    }

    companion object {
        const val TAG = "DraftAdapter"

        const val REMOVE_DRAFT_ITEM = 0

        const val CLEAN_DRAFT_BOX = 1
    }
}