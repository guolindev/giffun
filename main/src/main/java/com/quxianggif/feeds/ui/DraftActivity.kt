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

package com.quxianggif.feeds.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.view.MenuItem
import android.view.View
import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.common.view.SimpleDividerDecoration
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.DiscardDraftEvent
import com.quxianggif.event.FinishActivityEvent
import com.quxianggif.event.MessageEvent
import com.quxianggif.event.SaveDraftEvent
import com.quxianggif.feeds.adapter.DraftAdapter
import com.quxianggif.feeds.model.Draft
import kotlinx.android.synthetic.main.activity_draft.*
import org.litepal.LitePal
import org.litepal.extension.delete
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import java.util.*
import kotlin.concurrent.thread

class DraftActivity : BaseActivity() {

    internal lateinit var adapter: RecyclerView.Adapter<*>

    internal lateinit var layoutManager: LinearLayoutManager

    /**
     * RecyclerView的数据源。
     */
    private var draftList: MutableList<Draft> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft)
        loadDrafts()
        popFab()
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        layoutManager = LinearLayoutManager(this)
        adapter = DraftAdapter(this, draftList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SimpleDividerDecoration(this, true))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        composeFab.setOnClickListener {
            PostFeedActivity.actionStart(this)
        }
    }

    private fun loadDrafts() {
        thread {
            val drafts = LitePal.order("time desc").find<Draft>()
            if (drafts.isEmpty()) {
                runOnUiThread {
                    showNoContentView()
                }
            } else {
                runOnUiThread {
                    draftList.clear()
                    draftList.addAll(drafts)
                    adapter.notifyDataSetChanged()
                    loadFinished()
                }
            }
        }
    }

    /**
     * 使用pop动画的方式将fab按钮显示出来。
     */
    private fun popFab() {
        composeFab.show()
        composeFab.alpha = 0f
        composeFab.scaleX = 0f
        composeFab.scaleY = 0f
        val animator = ObjectAnimator.ofPropertyValuesHolder(
                composeFab,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
        animator.startDelay = 200
        animator.start()
    }

    private fun showNoContentView() {
        showNoContentView(GlobalUtil.getString(R.string.your_draft_is_empty))
    }

    override fun loadFinished() {
        super.loadFinished()
        recyclerView.visibility = View.VISIBLE
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == DraftAdapter.REMOVE_DRAFT_ITEM) {
            val itemPosition = item.order
            val draft = draftList[itemPosition]
            LitePal.delete<Draft>(draft.id)
            draftList.removeAt(itemPosition)
            adapter.notifyItemRemoved(itemPosition)
            if (draftList.isEmpty()) {
                showNoContentView()
            }
        } else if (item.itemId == DraftAdapter.CLEAN_DRAFT_BOX) {
            val dialog = AlertDialog.Builder(this, R.style.GifFunAlertDialogStyle)
                    .setMessage(GlobalUtil.getString(R.string.are_you_sure_to_clean_draft_box))
                    .setPositiveButton(GlobalUtil.getString(R.string.ok)) { _, _ ->
                        LitePal.deleteAll<Draft>()
                        draftList.clear()
                        adapter.notifyDataSetChanged()
                        if (draftList.isEmpty()) {
                            showNoContentView()
                        }
                    }
                    .setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
                    .create()
            dialog.show()
        }
        return super.onContextItemSelected(item)
    }

    override fun onMessageEvent(messageEvent: MessageEvent) {
        when (messageEvent) {
            is SaveDraftEvent -> {
                startLoading()
                loadDrafts()
            }
            is DiscardDraftEvent -> {
                if (draftList.isEmpty()) {
                    showNoContentView()
                }
            }
            is FinishActivityEvent -> {
                if (javaClass == messageEvent.activityClass) {
                    if (!isFinishing) {
                        finish()
                    }
                }
            }
            else -> {
                super.onMessageEvent(messageEvent)
            }
        }
    }

    companion object {

        fun actionStart(activity: Activity) {
            val intent = Intent(activity, DraftActivity::class.java)
            activity.startActivity(intent)
        }

        const val TAG = "DraftActivity"
    }
}
