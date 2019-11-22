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

package com.quxianggif.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity

/**
 * 用户对Feed、评论、用户进行举报的Activity。
 * @author guolin
 * @since 2018/8/30
 */
class ReportActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
    }

    override fun setupViews() {
        setupToolbar()
        val reportType = intent.getIntExtra(INTENT_REPORT_TYPE, 0)
        val fragment = when (reportType) {
            REPORT_FEED -> {
                val fragment = ReportFeedFragment()
                fragment.feedId = intent.getLongExtra(INTENT_FEED_ID, 0L)
                fragment
            }
            REPORT_COMMENT -> {
                val fragment = ReportCommentFragment()
                fragment.commentId = intent.getLongExtra(INTENT_COMMENT_ID, 0L)
                fragment
            }
            REPORT_USER -> {
                val fragment = ReportUserFragment()
                fragment.userId = intent.getLongExtra(INTENT_USER_ID, 0L)
                fragment
            }
            else -> null
        }
        if (fragment != null) {
            replaceFragment(fragment)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commitAllowingStateLoss()
    }

    companion object {

        const val TAG = "ReportActivity"

        private const val INTENT_REPORT_TYPE = "intent_report_type"
        private const val INTENT_FEED_ID = "intent_feed_id"
        private const val INTENT_COMMENT_ID = "intent_comment_id"
        private const val INTENT_USER_ID = "intent_user_id"

        private const val REPORT_FEED = 0

        private const val REPORT_COMMENT = 1

        private const val REPORT_USER = 2

        fun actionReportFeed(context: Context, feedId: Long) {
            val intent = Intent(context, ReportActivity::class.java)
            intent.putExtra(INTENT_REPORT_TYPE, REPORT_FEED)
            intent.putExtra(INTENT_FEED_ID, feedId)
            context.startActivity(intent)
        }

        fun actionReportComment(context: Context, commentId: Long) {
            val intent = Intent(context, ReportActivity::class.java)
            intent.putExtra(INTENT_REPORT_TYPE, REPORT_COMMENT)
            intent.putExtra(INTENT_COMMENT_ID, commentId)
            context.startActivity(intent)
        }

        fun actionReportUser(context: Context, userId: Long) {
            val intent = Intent(context, ReportActivity::class.java)
            intent.putExtra(INTENT_REPORT_TYPE, REPORT_USER)
            intent.putExtra(INTENT_USER_ID, userId)
            context.startActivity(intent)
        }

    }

}