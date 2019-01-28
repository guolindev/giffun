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
import com.quxianggif.network.model.ReportComment
import com.quxianggif.network.model.Response
import com.quxianggif.util.ResponseHandler
import kotlinx.android.synthetic.main.fragment_report_feed.*

/**
 * 用户对评论进行举报的Fragment。
 *
 * @author guolin
 * @since 2018/9/1
 */
class ReportCommentFragment : BaseFragment() {

    private var selectedReason = 0

    var commentId = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_comment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        reasonGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedReason = when (checkedId) {
                R.id.reasonPorn -> REASON_PORN
                R.id.reasonViolence -> REASON_VIOLENCE
                R.id.reasonReactionary -> REASON_REACTIONARY
                R.id.reasonFraud -> REASON_FRAUD
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
            ReportComment.getResponse(commentId, selectedReason, desp, object: Callback {
                override fun onResponse(response: Response) {
                    if (!ResponseHandler.handleResponse(response)) {
                        if (activity == null) {
                            return
                        }
                        val reportComment = response as ReportComment
                        val status = reportComment.status
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

        const val TAG = "ReportCommentFragment"

        const val REASON_PORN = 1

        const val REASON_VIOLENCE = 2

        const val REASON_REACTIONARY = 3

        const val REASON_FRAUD = 4

        const val REASON_OTHER = 5

    }

}