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

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import com.quxianggif.core.extension.logDebug
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.login.ui.AuthActivity
import com.quxianggif.login.ui.RegisterActivity
import com.quxianggif.network.model.BaseRegister
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.PhoneRegister
import com.quxianggif.network.model.Response
import com.quxianggif.util.ResponseHandler

/**
 * 当登录账号不存在时，跳转到注册界面来进行账号注册。
 *
 * @author guolin
 * @since 17/2/19
 */
class OpenSourceRegisterActivity : RegisterActivity(), TextView.OnEditorActionListener {

    private var number = ""

    private var code = ""

    /**
     * 获取Intent中传递过来的数据并显示到界面上。
     */
    override fun setupViews() {
        super.setupViews()
        if (intent.getStringExtra(INTENT_PHONE_NUMBER) == null || intent.getStringExtra(INTENT_VERIFY_CODE) == null) {
            showToast(GlobalUtil.getString(R.string.phone_number_verify_code_is_null))
            finish()
            return
        }
        number = intent.getStringExtra(INTENT_PHONE_NUMBER)
        code = intent.getStringExtra(INTENT_VERIFY_CODE)
        nicknameEditText.requestFocus()
    }

    /**
     * 开始执行注册逻辑。
     */
    override fun doRegister() {
        if (isRegistering) return
        when (loginType) {
            AuthActivity.TYPE_PHONE_LOGIN -> processPhoneRegister()
        }
    }

    /**
     * 注册手机号登录账号。
     */
    private fun processPhoneRegister() {
        if (isNicknameValid) {
            hideSoftKeyboard()
            nicknameLayout.isErrorEnabled = false
            registerInProgress(true)
            sendPhoneRegisterRequest()
        }
    }

    private fun sendPhoneRegisterRequest() {
        PhoneRegister.getResponse(number, code, nicknameEditText.text.toString().trim(), object : Callback {
            override fun onResponse(response: Response) {
                handleRegisterCallback(response)
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                registerInProgress(false)
                ResponseHandler.handleFailure(e)
            }
        })
    }

    private fun handleRegisterCallback(response: Response) {
        if (activity == null) {
            return
        }
        if (!ResponseHandler.handleResponse(response)) {
            val register = response as BaseRegister
            val status = register.status
            when (status) {
                0 -> {
                    logDebug(TAG, "token is " + register.token + " , getAvatar is " + register.avatar)
                    val userId = register.userId
                    val token = register.token
                    saveAuthData(userId.toLong(), token, loginType)
                    registerSuccess()
                }
                10105 -> {
                    registerInProgress(false)
                    nicknameLayout.isErrorEnabled = true
                    nicknameLayout.error = GlobalUtil.getString(R.string.register_failed_nickname_is_used)
                }
                else -> {
                    logWarn(TAG, "Register failed. " + GlobalUtil.getResponseClue(status, register.msg))
                    showToast(register.msg)
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    /**
     * 处理注册成功时的逻辑，包括数据缓存，界面跳转等。
     */
    private fun registerSuccess() {
        getUserBaseinfo()
    }

    companion object {

        private const val TAG = "OpenSourceRegisterActivity"

        fun registerByPhone(activity: Activity, number: String, code: String) {
            val intent = Intent(activity, OpenSourceRegisterActivity::class.java)
            intent.putExtra(INTENT_PHONE_NUMBER, number)
            intent.putExtra(INTENT_VERIFY_CODE, code)
            intent.putExtra(INTENT_LOGIN_TYPE, TYPE_PHONE_LOGIN)
            activity.startActivity(intent)
        }
    }

}
