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

package com.quxianggif.user.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity

/**
 * 展示系统推荐关注的用户列表界面。
 *
 * @author guolin
 * @since 18/3/19
 */
class RecommendFollowingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend_following)
    }

    override fun setupViews() {
        setupToolbar()
    }

    companion object {

        fun actionStart(activity: Activity) {
            val intent = Intent(activity, RecommendFollowingActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
