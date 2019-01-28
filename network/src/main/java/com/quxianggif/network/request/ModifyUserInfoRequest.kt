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

package com.quxianggif.network.request

import android.text.TextUtils
import com.qiniu.android.http.ResponseInfo
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.network.exception.ChangeAvatarException
import com.quxianggif.network.exception.ModifyUserInfoException
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.ModifyUserInfo
import com.quxianggif.network.model.Response
import com.quxianggif.network.util.NetworkConst
import com.quxianggif.network.util.QiniuManager
import com.quxianggif.network.util.ResHandler
import okhttp3.Headers
import java.util.*

/**
 * 修改用户信息请求。对应服务器接口：/user/userinfo
 *
 * @author guolin
 * @since 17/10/29
 */
class ModifyUserInfoRequest : Request() {

    private var mCallback: Callback? = null

    /**
     * 用户的昵称。
     */
    private var nickname: String = ""

    /**
     * 用户的个人简介。
     */
    private var description: String = ""

    /**
     * 头像资源的本地完整路径。
     */
    private var avatarFilePath: String = ""

    /**
     * 用户背景图片的本地完整路径。
     */
    private var bgImageFilePath: String = ""

    /**
     * 用于作为参数通知到GifFun服务器头像的唯一地址。
     */
    private var avatarUri: String = ""

    /**
     * 用于作为参数通知到GifFun服务器用户背景图的唯一地址。
     */
    private var bgImageUri: String = ""

    fun nickname(nickname: String): ModifyUserInfoRequest {
        this.nickname = nickname
        return this
    }

    fun description(description: String): ModifyUserInfoRequest {
        this.description = description
        return this
    }

    fun avatarFilePath(avatarFilePath: String): ModifyUserInfoRequest {
        this.avatarFilePath = avatarFilePath
        return this
    }

    fun bgImageFilePath(bgImageFilePath: String): ModifyUserInfoRequest {
        this.bgImageFilePath = bgImageFilePath
        return this
    }

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.POST
    }

    override fun listen(callback: Callback?) {
        mCallback = callback
        if (TextUtils.isEmpty(avatarFilePath) && TextUtils.isEmpty(bgImageFilePath)) {
            modifySimpleUserInfo()
        } else {
            modifyUserInfo()
        }
    }

    /**
     * 修改简易用户信息，只包含用户的昵称和个人简介，不修改用户头像和背景图。
     */
    private fun modifySimpleUserInfo() {
        setListener(mCallback)
        inFlight(ModifyUserInfo::class.java)
    }

    /**
     * 修改完整的用户信息，可能包括用户的昵称、个人简介、头像和背景图。
     */
    private fun modifyUserInfo() {
        setListener(object : Callback {
            override fun onResponse(response: Response) {
                if (!ResHandler.handleResponse(response)) {
                    val modifyUserInfo = response as ModifyUserInfo
                    val status = modifyUserInfo.status
                    if (status == 0) {
                        getUptoken()
                    } else {
                        logWarn("modifyUserInfo", GlobalUtil.getResponseClue(status, modifyUserInfo.msg))
                        mCallback?.onFailure(ModifyUserInfoException(GlobalUtil.getResponseClue(status, modifyUserInfo.msg)))
                    }
                } else {
                    mCallback?.onFailure(ModifyUserInfoException(ModifyUserInfoException.LOGIN_STATUS_EXPIRED))
                }
            }

            override fun onFailure(e: Exception) {
                mCallback?.onFailure(e)
            }
        })
        inFlight(ModifyUserInfo::class.java)
    }

    /**
     * 获取用于上传到七牛云的uptoken。
     */
    private fun getUptoken() {
        GetUptokenRequest().openSpace().listen(object : Callback {
            override fun onResponse(response: Response) {
                if (!ResHandler.handleResponse(response)) {
                    val uptoken = response as Uptoken
                    val status = uptoken.status
                    if (status == 0) {
                        val token = uptoken.uptoken
                        uploadAvatarToQiniu(token)
                    } else {
                        logWarn("getUptoken", GlobalUtil.getResponseClue(status, uptoken.msg))
                        mCallback?.onFailure(ModifyUserInfoException(GlobalUtil.getResponseClue(status, uptoken.msg)))
                    }
                } else {
                    mCallback?.onFailure(ModifyUserInfoException(ModifyUserInfoException.LOGIN_STATUS_EXPIRED))
                }
            }

            override fun onFailure(e: Exception) {
                mCallback?.onFailure(e)
            }
        })
    }

    /**
     * 将头像上传到七牛云。
     * @param token
     * 从服务器获取到的uptoken
     */
    private fun uploadAvatarToQiniu(token: String) {
        if (TextUtils.isEmpty(avatarFilePath)) {
            uploadBgImageToQiniu(token)
        } else {
            val key = GlobalUtil.generateKey(avatarFilePath, "avatar")
            QiniuManager.upload(avatarFilePath, key, token, object : QiniuManager.UploadListener {
                override fun onSuccess(key: String) {
                    avatarUri = key
                    uploadBgImageToQiniu(token)
                }

                override fun onFailure(info: ResponseInfo?) {
                    if (info != null) {
                        mCallback?.onFailure(ChangeAvatarException(info.error))
                    } else {
                        mCallback?.onFailure(ChangeAvatarException("unknown error"))
                    }
                }

                override fun onProgress(percent: Double) {}
            })
        }
    }

    /**
     * 将用户背景图上传到七牛云。
     * @param token
     * 从服务器获取到的uptoken
     */
    private fun uploadBgImageToQiniu(token: String) {
        if (TextUtils.isEmpty(bgImageFilePath)) {
            setListener(mCallback)
            getParamsAlready = false
            inFlight(ModifyUserInfo::class.java)
        } else {
            val key = GlobalUtil.generateKey(bgImageFilePath, "bg")
            QiniuManager.upload(bgImageFilePath, key, token, object : QiniuManager.UploadListener {
                override fun onSuccess(key: String) {
                    bgImageUri = key
                    setListener(mCallback)
                    getParamsAlready = false
                    inFlight(ModifyUserInfo::class.java)
                }

                override fun onFailure(info: ResponseInfo?) {
                    if (info != null) {
                        mCallback?.onFailure(ChangeAvatarException(info.error))
                    } else {
                        mCallback?.onFailure(ChangeAvatarException("unknown error"))
                    }
                }

                override fun onProgress(percent: Double) {}
            })
        }
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            if (nickname.isNotBlank()) {
                params[NetworkConst.NICKNAME] = nickname
            }
            if (description.isNotBlank()) {
                params[NetworkConst.DESCRIPTION] = description
            }
            if (avatarUri.isNotBlank()) {
                params[NetworkConst.AVATAR] = avatarUri
            }
            if (bgImageUri.isNotBlank()) {
                params[NetworkConst.BG_IMAGE] = bgImageUri
            }
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.NICKNAME, NetworkConst.UID, NetworkConst.TOKEN)
        return super.headers(builder)
    }

    companion object {

        private val TAG = "ModifyUserInfoRequest"

        private val URL = GifFun.BASE_URL + "/user/userinfo"
    }

}
