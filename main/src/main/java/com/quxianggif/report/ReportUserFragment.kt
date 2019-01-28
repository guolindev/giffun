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

package com.quxianggif.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quxianggif.R
import com.quxianggif.common.ui.BaseFragment
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.showToast
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.ReportUser
import com.quxianggif.network.model.Response
import com.quxianggif.util.ResponseHandler
import kotlinx.android.synthetic.main.fragment_report_feed.*

/**
 * 对用户进行举报的Fragment。
 *
 * @author guolin
 * @since 2018/8/30
 */
class ReportUserFragment : BaseFragment() {

    private var selectedReason = 0

    var userId = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_user, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        reasonGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedReason = when (checkedId) {
                R.id.reasonIcon -> REASON_ICON
                R.id.reasonBackground -> REASON_BACKGROUND
                R.id.reasonNickname -> REASON_NICKNAME
                R.id.reasonDescription -> REASON_DESCRIPTION
                R.id.reasonOther -> REASON_OTHER
                else -> 0
            }
        }

        submit.setOnClickListener {
            if (selectedReason == 0) {
                showToast(getString(R.string.please_choose_a_reason))
                return@setOnClickListener
            }
            val desp = descriptionEdit.text.toString().trim()
            if (selectedReason == REASON_OTHER && desp.isEmpty()) {
                showToast(getString(R.string.add_detail_information_when_choose_reason_other))
                return@setOnClickListener
            }

            submit.isEnabled = false
            descriptionEdit.isEnabled = false
            ReportUser.getResponse(userId, selectedReason, desp, object: Callback {
                override fun onResponse(response: Response) {
                    if (!ResponseHandler.handleResponse(response)) {
                        if (activity == null) {
                            return
                        }
                        val reportUser = response as ReportUser
                        val status = reportUser.status
                        when (status) {
                            0 -> {
                                showToast(getString(R.string.report_success))
                                activity?.finish()
                            }
                            else -> {
                                showToast(getString(R.string.submit_failed) + ": " + response.status)
                            }
                        }
                    } else {
                        showToast(getString(R.string.unknown_error) + ": " + response.status)
                    }
                    submit.isEnabled = true
                    descriptionEdit.isEnabled = true
                }

                override fun onFailure(e: Exception) {
                    logWarn(TAG, e.message, e)
                    showToast(getString(R.string.submit_failed_network_bad))
                    submit.isEnabled = true
                    descriptionEdit.isEnabled = true
                }

            })
        }
    }

    companion object {

        const val TAG = "ReportUserFragment"

        const val REASON_ICON = 1

        const val REASON_BACKGROUND = 2

        const val REASON_NICKNAME = 3

        const val REASON_DESCRIPTION = 4

        const val REASON_OTHER = 5

    }

}