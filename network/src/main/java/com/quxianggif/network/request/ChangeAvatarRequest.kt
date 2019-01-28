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
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.ChangeAvatar
import com.quxianggif.network.model.Response
import com.quxianggif.network.util.NetworkConst
import com.quxianggif.network.util.QiniuManager
import com.quxianggif.network.util.ResHandler

import java.util.HashMap

import okhttp3.Headers

/**
 * 修改头像请求。
 * 修改头像功能需要分为三步来实现：1.获取用于上传到七牛云的uptoken。2.将头像上传到七牛云。3.上传成功之后通知到GifFun服务器。
 * 三步全部完成则视为修改头像成功，其中有任何一步失败都视为修改头像失败。
 *
 * 修改头像对应服务器接口：/user/avatar
 *
 * @author guolin
 * @since 17/3/2
 */
class ChangeAvatarRequest : Request() {

    private var mCallback: Callback? = null

    /**
     * 头像资源的本地完整路径。
     */
    private var filePath: String = ""

    /**
     * 要存储到七牛云的key值，由客户端生成一个唯一的UUID
     */
    private var key: String = ""

    /**
     * 同key，用于作为参数通知到GifFun服务器头像的唯一地址。
     */
    private var uri: String = ""

    fun filePath(filePath: String): ChangeAvatarRequest {
        this.filePath = filePath
        return this
    }

    fun key(key: String): ChangeAvatarRequest {
        this.key = key
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
        if (!TextUtils.isEmpty(filePath) && !TextUtils.isEmpty(key)) {
            getUptoken()
        } else {
            mCallback?.onFailure(ChangeAvatarException("file path or key is null."))
        }
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            params[NetworkConst.URI] = uri
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.UID, NetworkConst.URI, NetworkConst.TOKEN)
        return super.headers(builder)
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
                        uploadToQiniu(uptoken.uptoken)
                    } else {
                        logWarn("getUptoken", GlobalUtil.getResponseClue(status, uptoken.msg))
                        mCallback?.onFailure(ChangeAvatarException(GlobalUtil.getResponseClue(status, uptoken.msg)))
                    }
                } else {
                    mCallback?.onFailure(ChangeAvatarException(ChangeAvatarException.LOGIN_STATUS_EXPIRED))
                }
            }

            override fun onFailure(e: Exception) {
                mCallback?.onFailure(e)
            }
        })
    }

    /**
     * 将头像上传到七牛云。
     * @param uptoken
     * 从服务器获取到的uptoken
     */
    private fun uploadToQiniu(uptoken: String) {
        QiniuManager.upload(filePath, key, uptoken, object : QiniuManager.UploadListener {
            override fun onSuccess(key: String) {
                uri = key
                changeAvatar()
            }

            override fun onFailure(info: ResponseInfo?) {
                if (info != null) {
                    mCallback?.onFailure(ChangeAvatarException(info.error))
                } else {
                    mCallback?.onFailure(ChangeAvatarException("change avatar unknown error"))
                }
            }

            override fun onProgress(percent: Double) {}
        })
    }

    /**
     * 上传成功之后通知到GifFun服务器。
     */
    private fun changeAvatar() {
        setListener(mCallback)
        inFlight(ChangeAvatar::class.java)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/user/avatar"
    }

}