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

import android.graphics.Bitmap
import android.text.TextUtils

import com.qiniu.android.http.ResponseInfo
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.core.util.ImageUtil
import com.quxianggif.network.exception.PostFeedException
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.PostFeed
import com.quxianggif.network.model.ProgressCallback
import com.quxianggif.network.model.Response
import com.quxianggif.network.util.MD5
import com.quxianggif.network.util.NetworkConst
import com.quxianggif.network.util.QiniuManager
import com.quxianggif.network.util.ResHandler

import java.io.File
import java.util.HashMap

import okhttp3.Headers
import kotlin.concurrent.thread

/**
 * 发布Feed请求。对应服务器接口：/feeds/post
 *
 * @author guolin
 * @since 17/4/20
 */
class PostFeedRequest : Request() {

    private lateinit var mCallback: ProgressCallback

    /**
     * GIF图片的本地完整路径。
     */
    private var gifPath: String = ""

    /**
     * 用户对Feed的描述。
     */
    private var feedContent: String = ""

    /**
     * GIF图第一帧的Bitmap。
     */
    private lateinit var firstFrame: Bitmap

    /**
     * GIF图第一帧缓存图片的路径。
     */
    private var firstFramePath: String = ""

    /**
     * 标识封面图在GifFun服务器上的唯一地址。
     */
    private var coverUri: String = ""

    /**
     * 标识GIF资源在GifFun服务器上的唯一地址。
     */
    private var gifUri: String = ""

    /**
     * GIF图片的md5值，用于标识图片的唯一性。
     */
    private var gifMD5: String = ""

    /**
     * 图片的宽度。
     */
    private var imgWidth: Int = 0

    /**
     * 图片的高度。
     */
    private var imgHeight: Int = 0

    fun gifPath(gifPath: String): PostFeedRequest {
        this.gifPath = gifPath
        return this
    }

    fun feedContent(feedContent: String): PostFeedRequest {
        this.feedContent = feedContent
        return this
    }

    fun firstFrame(firstFrame: Bitmap): PostFeedRequest {
        this.firstFrame = firstFrame
        return this
    }

    override fun url(): String {
        return URL
    }

    override fun method(): Int {
        return Request.POST
    }

    override fun listen(callback: Callback?) {
        if (callback is ProgressCallback) {
            mCallback = callback
            thread {
                if (validateParams()) {
                    preparePost()
                }
            }
        }
    }

    override fun params(): Map<String, String>? {
        val params = HashMap<String, String>()
        if (buildAuthParams(params)) {
            params[NetworkConst.COVER] = coverUri
            params[NetworkConst.GIF] = gifUri
            params[NetworkConst.GIF_MD5] = gifMD5
            params[NetworkConst.CONTENT] = feedContent
            params[NetworkConst.IMG_WIDTH] = imgWidth.toString()
            params[NetworkConst.IMG_HEIGHT] = imgHeight.toString()
            params[NetworkConst.DEVICE_NAME] = deviceName
            return params
        }
        return super.params()
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
        buildAuthHeaders(builder, NetworkConst.GIF, NetworkConst.TOKEN, NetworkConst.IMG_WIDTH, NetworkConst.UID, NetworkConst.IMG_HEIGHT)
        return super.headers(builder)
    }

    /**
     * 检验各个参数是否合法。
     * @return 全部合法返回true，有任意参数不合法返回false。
     */
    private fun validateParams(): Boolean {
        if (TextUtils.isEmpty(gifPath) || TextUtils.isEmpty(feedContent)) {
            mCallback.onFailure(PostFeedException(PostFeedException.GIF_PATH_OR_FEED_CONTENT_IS_NULL))
            return false
        }
        if (!ImageUtil.isGifValid(gifPath)) {
            mCallback.onFailure(PostFeedException(PostFeedException.GIF_FORMAT_IS_INCORRECT))
            return false
        }
        if (ImageUtil.getImageSize(gifPath) > GifFun.GIF_MAX_SIZE) {
            mCallback.onFailure(PostFeedException(PostFeedException.GIF_IS_LARGER_THAN_20_MB))
            return false
        }
        firstFramePath = ImageUtil.saveBitmapAsFile(firstFrame) ?: ""
        if (firstFramePath.isBlank()) {
            mCallback.onFailure(PostFeedException(PostFeedException.GIF_COVER_IS_UNREACHABLE))
            return false
        }
        imgWidth = firstFrame.width
        imgHeight = firstFrame.height
        if (imgWidth <= 0 || imgHeight <= 0) {
            mCallback.onFailure(PostFeedException(PostFeedException.GIF_WIDTH_OR_HEIGHT_IS_INVALID))
            return false
        }
        if (imgWidth / (imgHeight * 1.0) > 2.5 || imgHeight / (imgWidth * 1.0) > 2.5) {
            mCallback.onFailure(PostFeedException(PostFeedException.GIF_IS_TOO_WIDE_OR_TOO_NARROW))
            return false
        }
        gifMD5 = MD5.getFileMD5(gifPath)
        if (gifMD5.isBlank()) {
            mCallback.onFailure(PostFeedException(PostFeedException.GIF_MD5_EXCEPTION))
            return false
        }
        return true
    }

    /**
     * 进行预备Post Feed请求，判断GIF图是否已存在，如果不存在则要获取uptoken。
     */
    private fun preparePost() {
        PreparePostRequest().gifMD5(gifMD5).feedContent(feedContent).listen(object : Callback {
            override fun onResponse(response: Response) {
                if (!ResHandler.handleResponse(response)) {
                    val preparePost = response as PreparePost
                    val status = preparePost.status
                    when (status) {
                        0 -> uploadGifCover(preparePost)
                        10303 -> {
                            coverUri = preparePost.coverUri
                            gifUri = preparePost.gifUri
                            postFeed()
                        }
                        10301 -> {
                            val postFeed = PostFeed()
                            postFeed.status = response.status
                            postFeed.msg = response.msg
                            mCallback.onResponse(postFeed)
                        }
                        else -> {
                            logWarn("preparePost", GlobalUtil.getResponseClue(status, preparePost.msg))
                            mCallback.onFailure(PostFeedException(GlobalUtil.getResponseClue(status, preparePost.msg)))
                        }
                    }
                } else {
                    mCallback.onFailure(PostFeedException(PostFeedException.LOGIN_STATUS_EXPIRED))
                }
            }

            override fun onFailure(e: Exception) {
                mCallback.onFailure(e)
            }
        })
    }

    /**
     * 将GIF封面图上传到七牛云。
     * @param preparePost
     * PreparePost响应，包含uptoken
     */
    private fun uploadGifCover(preparePost: PreparePost) {
        val key = GlobalUtil.generateKey(firstFramePath, "cover")
        QiniuManager.upload(firstFramePath, key, preparePost.openUptoken, object : QiniuManager.UploadListener {
            override fun onSuccess(key: String) {
                coverUri = key
                // 在封面图上传成功之后，删除本地的缓存封面图
                val cover = File(firstFramePath)
                if (cover.exists()) {
                    cover.delete()
                }
                uploadGif(preparePost)
            }

            override fun onFailure(info: ResponseInfo?) {
                if (info != null) {
                    mCallback.onFailure(PostFeedException(info.error))
                } else {
                    mCallback.onFailure(PostFeedException("unknown error"))
                }
            }

            override fun onProgress(percent: Double) {}
        })
    }

    /**
     * 将GIF图上传到七牛云。
     * @param preparePost
     * PreparePost响应，包含uptoken
     */
    private fun uploadGif(preparePost: PreparePost) {
        val key = GlobalUtil.generateKey(gifPath, "gif")
        QiniuManager.upload(gifPath, key, preparePost.privateUptoken, object : QiniuManager.UploadListener {
            override fun onSuccess(key: String) {
                gifUri = key
                postFeed()
            }

            override fun onFailure(info: ResponseInfo?) {
                if (info != null) {
                    mCallback.onFailure(PostFeedException(info.error))
                } else {
                    mCallback.onFailure(PostFeedException("unknown error"))
                }
            }

            override fun onProgress(percent: Double) {
                mCallback.onProgress(percent)
            }
        })
    }

    /**
     * 上传成功之后通知到GifFun服务器。
     */
    private fun postFeed() {
        setListener(mCallback)
        inFlight(PostFeed::class.java)
    }

    companion object {

        private val URL = GifFun.BASE_URL + "/feeds/post"
    }

}
