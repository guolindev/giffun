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

package com.quxianggif.login.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import com.quxianggif.R
import com.quxianggif.core.GifFun
import com.quxianggif.core.model.Version
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.event.FinishActivityEvent
import com.quxianggif.event.MessageEvent
import com.quxianggif.feeds.ui.MainActivity
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 应用程序登录界面的基类。
 *
 * @author guolin
 * @since 17/2/18
 */
abstract class LoginActivity : AuthActivity() {

    /**
     * 是否正在进行transition动画。
     */
    protected var isTransitioning = false

    override fun onBackPressed() {
        if (!isTransitioning) {
            finish()
        }
    }

    override fun forwardToMainActivity() {
        // 登录成功，跳转到应用主界面
        MainActivity.actionStart(this)
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is FinishActivityEvent && LoginActivity::class.java == messageEvent.activityClass) {
            finish()
        }
    }

    companion object {

        private const val TAG = "LoginActivity"

        @JvmStatic val START_WITH_TRANSITION = "start_with_transition"

        @JvmStatic val INTENT_HAS_NEW_VERSION = "intent_has_new_version"

        @JvmStatic val INTENT_VERSION = "intent_version"

        private val ACTION_LOGIN = "${GifFun.getPackageName()}.ACTION_LOGIN"

        private val ACTION_LOGIN_WITH_TRANSITION = "${GifFun.getPackageName()}.ACTION_LOGIN_WITH_TRANSITION"

        /**
         * 启动LoginActivity。
         *
         * @param activity
         *          原Activity的实例
         * @param hasNewVersion
         *          是否存在版本更新。
         *
         */
        fun actionStart(activity: Activity, hasNewVersion: Boolean, version: Version?) {
            val intent = Intent(ACTION_LOGIN).apply {
                putExtra(INTENT_HAS_NEW_VERSION, hasNewVersion)
                putExtra(INTENT_VERSION, version)
            }
            activity.startActivity(intent)
        }

        /**
         * 启动LoginActivity，并附带Transition动画。
         *
         * @param activity
         * 原Activity的实例
         * @param logo
         * 要执行transition动画的控件
         */
        fun actionStartWithTransition(activity: Activity, logo: View, hasNewVersion: Boolean, version: Version?) {
            val intent = Intent(ACTION_LOGIN_WITH_TRANSITION).apply {
                putExtra(INTENT_HAS_NEW_VERSION, hasNewVersion)
                putExtra(INTENT_VERSION, version)
            }
            if (AndroidVersion.hasLollipop()) {
                intent.putExtra(START_WITH_TRANSITION, true)
                val options = ActivityOptions.makeSceneTransitionAnimation(activity, logo,
                        activity.getString(R.string.transition_logo_splash))
                activity.startActivity(intent, options.toBundle())
            } else {
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }
}
