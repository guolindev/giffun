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

package com.quxianggif.common.callback

/**
 * 用于延迟执行的特定Runnable，允许在执行的时候传入index参数。专为解决主界面ViewPager之间页签切换时点赞状态同步不准确的问题。
 *
 * @author guolin
 * @since 2018/4/29
 */
interface PendingRunnable {

    fun run(index: Int)

}
