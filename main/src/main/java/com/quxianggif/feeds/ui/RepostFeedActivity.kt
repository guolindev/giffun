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

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.BaseFeed
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.RefreshFollowingFeedsEvent
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.RepostFeed
import com.quxianggif.network.model.Response
import com.quxianggif.util.DateUtil
import com.quxianggif.util.ResponseHandler
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.activity_repost_feed.*
import org.greenrobot.eventbus.EventBus

class RepostFeedActivity : BaseActivity(), View.OnClickListener, TextView.OnEditorActionListener {

    private lateinit var refFeed: BaseFeed

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repost_feed)
    }

    override fun onResume() {
        super.onResume()
        showSoftKeyboard(contentEdit)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_repost_feed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.repost_feed -> {
                repostFeed()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setupViews() {
        if (loadFeed()) {
            setupToolbar()

            feedUser.text = refFeed.nickname
            feedContent.text = refFeed.content
            Glide.with(this)
                    .load(refFeed.cover)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.album_loading_bg)
                    .into(feedCover)

            rootLayout.setOnClickListener(this)
            contentEdit.setOnEditorActionListener(this)
        } else {
            finish()
        }
    }

    /**
     * 读取传入的Feed实例。
     * @return 读取成功返回true，失败返回false。
     */
    private fun loadFeed(): Boolean {
        val feed = intent.getParcelableExtra<BaseFeed>(FEED)
        if (feed != null) {
            refFeed = feed
            return true
        }
        showToast(GlobalUtil.getString(R.string.load_feed_failed))
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            rootLayout.id -> showSoftKeyboard(contentEdit)
        }
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            repostFeed()
        }
        return false
    }

    /**
     * 转发Feed。
     */
    private fun repostFeed() {
        val feedContent = contentEdit.text.toString().trim()
        if (TextUtils.isEmpty(feedContent)) {
            showToast(GlobalUtil.getString(R.string.say_something))
            return
        }
        hideSoftKeyboard()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("请稍后 ...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        RepostFeed.getResponse(refFeed.feedId, feedContent, object : Callback {
            override fun onResponse(response: Response) {
                progressDialog.dismiss()
                if (activity == null) {
                    return
                }
                if (!ResponseHandler.handleResponse(response)) {
                    val repostFeed = response as RepostFeed
                    val status = repostFeed.status
                    if (status == 0) {
                        val event = RefreshFollowingFeedsEvent()
                        EventBus.getDefault().post(event)
                        MobclickAgent.onEvent(this@RepostFeedActivity, "10002")
                        showToast(GlobalUtil.getString(R.string.repost_success))
                        finish()
                    } else if (status == 10301) {
                        val timeLeft = repostFeed.msg.toLong()
                        if (DateUtil.isBlockedForever(timeLeft)) {
                            showToast(GlobalUtil.getString(R.string.unable_to_repost_feed_forever), Toast.LENGTH_LONG)
                        } else {
                            val tip = DateUtil.getTimeLeftTip(timeLeft)
                            showToast(String.format(GlobalUtil.getString(R.string.unable_to_repost_feed), tip), Toast.LENGTH_LONG)
                        }
                    } else {
                        logWarn(TAG, "Repost feed failed. " + GlobalUtil.getResponseClue(status, repostFeed.msg))
                        showToast(GlobalUtil.getString(R.string.repost_failed) + GlobalUtil.getString(R.string.unknown_error))
                    }
                }
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                ResponseHandler.handleFailure(e)
                progressDialog.dismiss()
            }
        })

    }

    companion object {

        private const val TAG = "RepostFeedActivity"

        const val FEED = "feed"

        fun actionStart(activity: Activity, refFeed: BaseFeed) {
            val intent = Intent(activity, RepostFeedActivity::class.java)
            intent.putExtra(FEED, refFeed)
            activity.startActivity(intent)
        }
    }

}
