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

import com.google.gson.annotations.SerializedName

/**
 * SimpleListFeed的实体类，用于存储单列列表展示的Feed数据。
 *
 * @author guolin
 * @since 17/10/14
 */
abstract class SimpleListFeed : BaseFeed() {

    @SerializedName("feed_type")
    var feedType = 0

    abstract fun refFeed() : BaseFeed?

}
