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

import com.quxianggif.network.model.Response

/**
 * 获取Uptoken请求的实体类封装。
 *
 * @author guolin
 * @since 17/3/5
 */
internal class Uptoken : Response() {

    /**
     * 能够将资源上传到七牛云空间的uptoken，客户端使用此uptoken就可以进行资源上传操作。
     */
    var uptoken: String = ""
}