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

package com.quxianggif.network.exception

/**
 * 分享GIF图时出现的异常，使用此异常类进行抛出。
 *
 * @author guolin
 * @since 17/3/6
 */
class PostFeedException(message: String) : RuntimeException(message) {

    companion object {
        const val GIF_PATH_OR_FEED_CONTENT_IS_NULL = "gifPath or feedContent is null."

        const val GIF_FORMAT_IS_INCORRECT = "GIF format is incorrect."

        const val GIF_IS_LARGER_THAN_20_MB = "GIF is larger than 20 MB."

        const val GIF_COVER_IS_UNREACHABLE = "GIF cover is unreachable."

        const val GIF_WIDTH_OR_HEIGHT_IS_INVALID = "GIF width or height is invalid."

        const val GIF_MD5_EXCEPTION = "GIF md5 exception."

        const val GIF_IS_TOO_WIDE_OR_TOO_NARROW = "GIF is too wide or too narrow"

        const val LOGIN_STATUS_EXPIRED = "Login status expired."
    }

}
