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

package com.quxianggif.util

import com.quxianggif.core.Const
import com.quxianggif.core.util.SharedUtil

/**
 * 获取当前登录用户信息的工具类。
 *
 * @author guolin
 * @since 17/3/10
 */
object UserUtil {

    val nickname: String
        get() = SharedUtil.read(Const.User.NICKNAME, "")

    val avatar: String
        get() = SharedUtil.read(Const.User.AVATAR, "")

    val bgImage: String
        get() = SharedUtil.read(Const.User.BG_IMAGE, "")

    val description: String
        get() = SharedUtil.read(Const.User.DESCRIPTION, "")

    fun saveNickname(nickname: String?) {
        if (nickname != null && nickname.isNotBlank()) {
            SharedUtil.save(Const.User.NICKNAME, nickname)
        } else {
            SharedUtil.clear(Const.User.NICKNAME)
        }
    }

    fun saveAvatar(avatar: String?) {
        if (avatar != null && avatar.isNotBlank()) {
            SharedUtil.save(Const.User.AVATAR, avatar)
        } else {
            SharedUtil.clear(Const.User.AVATAR)
        }
    }

    fun saveBgImage(bgImage: String?) {
        if (bgImage != null && bgImage.isNotBlank()) {
            SharedUtil.save(Const.User.BG_IMAGE, bgImage)
        } else {
            SharedUtil.clear(Const.User.BG_IMAGE)
        }
    }

    fun saveDescription(description: String?) {
        if (description != null && description.isNotBlank()) {
            SharedUtil.save(Const.User.DESCRIPTION, description)
        } else {
            SharedUtil.clear(Const.User.DESCRIPTION)
        }
    }

}
