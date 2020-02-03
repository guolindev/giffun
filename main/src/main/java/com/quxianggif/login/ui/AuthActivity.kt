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

import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.core.Const
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.core.util.SharedUtil
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.GetBaseinfo
import com.quxianggif.network.model.Response
import com.quxianggif.util.ResponseHandler
import com.quxianggif.util.UserUtil

/**
 * 登录和注册Activity的基类，用于封装登录和注册时通用的逻辑功能。
 *
 * @author guolin
 * @since 17/3/10
 */
abstract class AuthActivity : BaseActivity() {

    /**
     * 根据参数中传入的登录类型数值获取登录类型的名称。
     *
     * @param loginType
     * 登录类型的数值。
     * @return 登录类型的名称。
     */
    protected fun getLoginTypeName(loginType: Int) = when (loginType) {
        TYPE_QQ_LOGIN -> "QQ"
        TYPE_WECHAT_LOGIN -> "微信"
        TYPE_WEIBO_LOGIN -> "微博"
        TYPE_GUEST_LOGIN -> "游客"
        else -> ""
    }

    /**
     * 存储用户身份的信息。
     *
     * @param userId
     * 用户id
     * @param token
     * 用户token
     * @param loginType
     * 登录类型
     */
    protected fun saveAuthData(userId: Long, token: String, loginType: Int) {
        SharedUtil.save(Const.Auth.USER_ID, userId)
        SharedUtil.save(Const.Auth.TOKEN, token)
        SharedUtil.save(Const.Auth.LOGIN_TYPE, loginType)
        GifFun.refreshLoginState()
    }

    /**
     * 获取当前登录用户的基本信息，包括昵称、头像等。
     */
    protected fun getUserBaseinfo() {
        GetBaseinfo.getResponse(object : Callback {
            override fun onResponse(response: Response) {
                if (activity == null) {
                    return
                }
                if (!ResponseHandler.handleResponse(response)) {
                    val baseinfo = response as GetBaseinfo
                    val status = baseinfo.status
                    when (status) {
                        0 -> {
                            UserUtil.saveNickname(baseinfo.nickname)
                            UserUtil.saveAvatar(baseinfo.avatar)
                            UserUtil.saveDescription(baseinfo.description)
                            UserUtil.saveBgImage(baseinfo.bgImage)
                            forwardToMainActivity()
                        }
                        10202 -> {
                            showToast(GlobalUtil.getString(R.string.get_baseinfo_failed_user_not_exist))
                            GifFun.logout()
                            finish()
                        }
                        else -> {
                            logWarn(TAG, "Get user baseinfo failed. " + GlobalUtil.getResponseClue(status, baseinfo.msg))
                            showToast(GlobalUtil.getString(R.string.get_baseinfo_failed))
                            GifFun.logout()
                            finish()
                        }
                    }
                } else {
                    activity?.let {
                        if (it.javaClass.name == "club.giffun.app.LoginDialogActivity") {
                            finish()
                        }
                    }
                }
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                showToast(GlobalUtil.getString(R.string.get_baseinfo_failed))
                GifFun.logout()
                finish()
            }
        })
    }

    protected abstract fun forwardToMainActivity()

    companion object {

        private const val TAG = "AuthActivity"

        /**
         * QQ第三方登录的类型。
         */
        const val TYPE_QQ_LOGIN = 1

        /**
         * 微信第三方登录的类型。
         */
        const val TYPE_WECHAT_LOGIN = 2

        /**
         * 微博第三方登录的类型。
         */
        const val TYPE_WEIBO_LOGIN = 3

        /**
         * 手机号登录的类型。
         */
        const val TYPE_PHONE_LOGIN = 4

        /**
         * 游客登录的类型，此登录只在测试环境下有效，线上环境没有此项功能。
         */
        const val TYPE_GUEST_LOGIN = 0
    }

}
