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

package com.quxianggif.user.ui

import com.quxianggif.core.extension.logWarn
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.GetFollowings
import com.quxianggif.network.model.Response
import com.quxianggif.util.ResponseHandler

/**
 * 用户个人主页关注列表Fragment。
 *
 * @author guolin
 * @since 17/7/30
 */
class FollowingsFragment : BaseFollowshipFragment() {

    override fun loadFollowships(page: Int) {
        GetFollowings.getResponse(activity.mUserId, page, object : Callback {
            override fun onResponse(response: Response) {
                handleFetchedFollowships(response)
                isLoadingMore = false
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                isLoadingMore = false
                if (page == 0) {
                    ResponseHandler.handleFailure(e)
                }
                loadFailed(null)
            }
        })
    }

    companion object {

        private const val TAG = "FollowingsFragment"
    }
}
