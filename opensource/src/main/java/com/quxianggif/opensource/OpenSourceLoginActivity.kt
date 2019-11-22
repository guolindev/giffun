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

package com.quxianggif.opensource

import android.os.Bundle
import android.os.CountDownTimer
import androidx.transition.Fade
import androidx.transition.TransitionManager
import android.transition.Transition
import android.view.View
import com.quxianggif.common.callback.SimpleTransitionListener
import com.quxianggif.core.extension.logDebug
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.FinishActivityEvent
import com.quxianggif.login.ui.LoginActivity
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.FetchVCode
import com.quxianggif.network.model.PhoneLogin
import com.quxianggif.network.model.Response
import com.quxianggif.network.util.AuthUtil
import com.quxianggif.util.ResponseHandler
import kotlinx.android.synthetic.main.activity_login.*
import org.greenrobot.eventbus.EventBus
import java.util.regex.Pattern

/**
 * 开源版登录界面，支持手机号登录。如果登录的账号还没有注册会跳转到注册界面，如果已经注册过了则会直接跳转到应用主界面。
 * @author guolin
 * @since 2018/12/26
 */
class OpenSourceLoginActivity : LoginActivity() {

    private lateinit var timer: CountDownTimer

    /**
     * 是否正在登录中。
     */
    private var isLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun setupViews() {
        super.setupViews()
        val isStartWithTransition = intent.getBooleanExtra(LoginActivity.START_WITH_TRANSITION, false)
        if (AndroidVersion.hasLollipop() && isStartWithTransition) {
            isTransitioning = true
            window.sharedElementEnterTransition.addListener(object : SimpleTransitionListener() {
                override fun onTransitionEnd(transition: Transition) {
                    val event = FinishActivityEvent()
                    event.activityClass = OpenSourceSplashActivity::class.java
                    EventBus.getDefault().post(event)
                    isTransitioning = false
                    fadeElementsIn()
                }
            })
        } else {
            loginLayoutBottom.visibility = View.VISIBLE
            loginBgWallLayout.visibility = View.VISIBLE
        }

        timer = SMSTimer(60 * 1000, 1000)
        getVerifyCode.setOnClickListener {
            val number = phoneNumberEdit.text.toString()
            if (number.isEmpty()) {
                showToast(GlobalUtil.getString(R.string.phone_number_is_empty))
                return@setOnClickListener
            }
            val pattern = "^1\\d{10}\$"
            if (!Pattern.matches(pattern, number)) {
                showToast(GlobalUtil.getString(R.string.phone_number_is_invalid))
                return@setOnClickListener
            }
            getVerifyCode.isClickable = false
            FetchVCode.getResponse(number, object : Callback {
                override fun onResponse(response: Response) {
                    if (response.status == 0) {
                        timer.start()
                        verifyCodeEdit.requestFocus()
                    } else {
                        showToast(response.msg)
                        getVerifyCode.isClickable = true
                    }
                }

                override fun onFailure(e: Exception) {
                    logWarn(TAG, e.message, e)
                    ResponseHandler.handleFailure(e)
                    getVerifyCode.isClickable = true
                }
            })
        }
        loginButton.setOnClickListener {
            if (isLogin) return@setOnClickListener
            val number = phoneNumberEdit.text.toString()
            val code = verifyCodeEdit.text.toString()
            if (number.isEmpty() || code.isEmpty()) {
                showToast(GlobalUtil.getString(R.string.phone_number_or_code_is_empty))
                return@setOnClickListener
            }
            val pattern = "^1\\d{10}\$"
            if (!Pattern.matches(pattern, number)) {
                showToast(GlobalUtil.getString(R.string.phone_number_is_invalid))
                return@setOnClickListener
            }
            processLogin(number, code)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    private fun processLogin(number: String, code: String) {
        hideSoftKeyboard()
        loginInProgress(true)
        PhoneLogin.getResponse(number, code, object : Callback {
            override fun onResponse(response: Response) {
                if (!ResponseHandler.handleResponse(response)) {
                    val thirdPartyLogin = response as PhoneLogin
                    val status = thirdPartyLogin.status
                    val msg = thirdPartyLogin.msg
                    val userId = thirdPartyLogin.userId
                    val token = thirdPartyLogin.token
                    when (status) {
                        0 -> {
                            hideSoftKeyboard()
                            // 处理登录成功时的逻辑，包括数据缓存，界面跳转等
                            saveAuthData(userId, token, TYPE_PHONE_LOGIN)
                            getUserBaseinfo()
                        }
                        10101 -> {
                            hideSoftKeyboard()
                            OpenSourceRegisterActivity.registerByPhone(this@OpenSourceLoginActivity, number, code)
                            loginInProgress(false)
                        }
                        else -> {
                            logWarn(TAG, "Login failed. " + GlobalUtil.getResponseClue(status, msg))
                            showToast(response.msg)
                            loginInProgress(false)
                        }
                    }
                } else {
                    loginInProgress(false)
                }
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                ResponseHandler.handleFailure(e)
                loginInProgress(false)
            }
        })
    }

    /**
     * 根据用户是否正在注册来刷新界面。如果正在处理就显示进度条，否则的话就显示输入框。
     *
     * @param inProgress 是否正在注册
     */
    private fun loginInProgress(inProgress: Boolean) {
        if (AndroidVersion.hasMarshmallow() && !(inProgress && loginRootLayout.keyboardShowed)) {
            TransitionManager.beginDelayedTransition(loginRootLayout, Fade())
        }
        isLogin = inProgress
        if (inProgress) {
            loginInputElements.visibility = View.INVISIBLE
            loginProgressBar.visibility = View.VISIBLE
        } else {
            loginProgressBar.visibility = View.INVISIBLE
            loginInputElements.visibility = View.VISIBLE
        }
    }

    /**
     * 将LoginActivity的界面元素使用淡入的方式显示出来。
     */
    private fun fadeElementsIn() {
        TransitionManager.beginDelayedTransition(loginLayoutBottom, Fade())
        loginLayoutBottom.visibility = View.VISIBLE
        TransitionManager.beginDelayedTransition(loginBgWallLayout, Fade())
        loginBgWallLayout.visibility = View.VISIBLE
    }

    inner class SMSTimer(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {

        override fun onFinish() {
            getVerifyCode.text = GlobalUtil.getString(R.string.fetch_vcode)
            getVerifyCode.isClickable = true
        }

        override fun onTick(millisUntilFinished: Long) {
            getVerifyCode.text = String.format(GlobalUtil.getString(R.string.sms_is_sent), millisUntilFinished / 1000)
        }

    }

    companion object {
        const val TAG = "OpenSourceLoginActivity"
    }

}