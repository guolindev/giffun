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

package com.quxianggif.common.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import com.quxianggif.R
import com.quxianggif.common.callback.PermissionListener
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.feeds.ui.SelectGifActivity

/**
 * 当运行时权限被拒绝时显示的界面，在此界面中提供让用户再次申请权限的按钮。
 *
 * @author guolin
 * @since 17/3/21
 */
class NeedPermissionFragment : BaseFragment(), OnClickListener {

    private var mPermissions: Array<String>? = null

    private var activity: BaseActivity? = null

    fun setPermissions(permissions: Array<String>) {
        mPermissions = permissions
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_need_permission, container, false)
        val agree = view.findViewById<Button>(R.id.agree)
        agree.setOnClickListener(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as BaseActivity?
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.agree -> handlePermissions(mPermissions, object : PermissionListener {
                override fun onGranted() {
                    activity!!.permissionsGranted()
                }

                override fun onDenied(deniedPermissions: List<String>) {
                    var allNeverAskAgain = true
                    for (deniedPermission in deniedPermissions) {
                        if (shouldShowRequestPermissionRationale(deniedPermission)) {
                            allNeverAskAgain = false
                            break
                        }
                    }
                    // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                    if (allNeverAskAgain) {
                        val dialog = AlertDialog.Builder(activity!!, R.style.GifFunAlertDialogStyle)
                                .setMessage(GlobalUtil.getString(R.string.allow_storage_permission_please))
                                .setPositiveButton(GlobalUtil.getString(R.string.settings)) { _, _ ->
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", GlobalUtil.appPackage, null)
                                    intent.data = uri
                                    activity!!.startActivityForResult(intent, SelectGifActivity.REQUEST_PERMISSION_SETTING)
                                }
                                .setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
                                .create()
                        dialog.show()
                    }
                }
            })
            else -> {
            }
        }
    }
}
