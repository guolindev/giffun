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

package com.quxianggif.core.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.litepal.annotation.Column

/**
 * Feed实体类，用于存储服务器返回的Feed数据。
 *
 * @author guolin
 * @since 17/5/24
 */
open class BaseFeed : Model(), Parcelable, SearchItem {

    override val modelId: Long
        get() = feedId

    /**
     * 本地数据库中使用的id。
     */
    var id = 0
        internal set

    /**
     * Feed封面的地址。
     */
    var cover = ""

    /**
     * Feed图片的地址。
     */

    var gif = ""

    /**
     * Feed的具体内容。
     */
    var content = ""

    /**
     * Feed图片的宽度。
     */
    @SerializedName("img_width")
    var imgWidth = 0

    /**
     * Feed图片的高度。
     */
    @SerializedName("img_height")
    var imgHeight = 0

    /**
     * Feed所属用户的昵称。
     */
    var nickname = ""

    /**
     * Feed所属用户头像的地址。
     */
    var avatar = ""

    /**
     * Feed所属用户个人主页的背景图。
     */
    @SerializedName("bg_image")
    var bgImage = ""

    /**
     * 该条Feed对应的的User id。
     */
    @SerializedName("user_id")
    var userId = 0L

    /**
     * Feed发布的时间。
     */
    @SerializedName("post_date")
    var postDate  = 0L

    /**
     * 服务器端返回的Feed id。
     */
    @SerializedName("feed_id")
    var feedId = 0L

    /**
     * Feed图片的大小，单位是字节。
     */
    var fsize = 0L

    /**
     * 该条feed的点赞数。
     */
    @SerializedName("likes_count")
    var likesCount = 0

    /**
     * 是否已对该feed点赞。
     */
    @SerializedName("liked_already")
    var isLikedAlready = false

    /**
     * 封面图是否已加载成功。（未成功加载封面图的feed不可以进入Feed详情界面）
     */
    @Column(ignore = true)
    var coverLoaded = false

    /**
     * 封面图片是否加载失败。
     */
    @Column(ignore = true)
    var coverLoadFailed = false

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(cover)
        dest?.writeString(gif)
        dest?.writeString(content)
        dest?.writeInt(imgWidth)
        dest?.writeInt(imgHeight)
        dest?.writeString(nickname)
        dest?.writeString(avatar)
        dest?.writeString(bgImage)
        dest?.writeLong(postDate)
        dest?.writeLong(feedId)
        dest?.writeLong(userId)
        dest?.writeLong(fsize)
        dest?.writeInt(likesCount)
        dest?.writeInt(if (isLikedAlready) 1 else 0)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<BaseFeed> = object : Parcelable.Creator<BaseFeed> {

            override fun createFromParcel(source: Parcel): BaseFeed {
                val feed = WorldFeed()
                feed.cover = source.readString() ?: ""
                feed.gif = source.readString() ?: ""
                feed.content = source.readString() ?: ""
                feed.imgWidth = source.readInt()
                feed.imgHeight = source.readInt()
                feed.nickname = source.readString() ?: ""
                feed.avatar = source.readString() ?: ""
                feed.bgImage = source.readString() ?: ""
                feed.postDate = source.readLong()
                feed.feedId = source.readLong()
                feed.userId = source.readLong()
                feed.fsize = source.readLong()
                feed.likesCount = source.readInt()
                feed.isLikedAlready = source.readInt() == 1
                return feed
            }

            override fun newArray(size: Int): Array<BaseFeed?> {
                return arrayOfNulls(size)
            }
        }
    }

}
