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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.quxianggif.R
import com.quxianggif.common.callback.PermissionListener
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.extension.showToastOnUiThread
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.core.util.ImageUtil
import com.quxianggif.feeds.ui.SelectGifActivity
import com.quxianggif.util.FileUtil
import kotlinx.android.synthetic.main.fragment_share_dialog.*
import java.io.File
import java.util.*


/**
 * 分享对话框的弹出界面。
 *
 * @author guolin
 * @since 2018/10/21
 */
open class ShareDialogFragment : BottomSheetDialogFragment() {

    private lateinit var imagePath: String

    private lateinit var attachedActivity: Activity

    private var mListener: PermissionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_share_dialog, container, false)
    }

    fun showDialog(activity: AppCompatActivity, imageUri: String) {
        show(activity.supportFragmentManager, "share_dialog")
        imagePath = imageUri
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { act ->
            attachedActivity = act
            shareToQQ.setOnClickListener {
                processShare(SHARE_QQ)
            }
            shareToWechatFriends.setOnClickListener {
                processShare(SHARE_WECHAT)
            }
            shareToWeibo.setOnClickListener {
                processShare(SHARE_WEIBO)
            }
            saveToPhone.setOnClickListener {
                processShare(SAVE_TO_PHONE)
            }
        }
    }

    private fun processShare(shareType: Int) {
        handlePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionListener {
            override fun onGranted() {
                when (shareType) {
                    SHARE_QQ -> {
                        if (!GlobalUtil.isQQInstalled()) {
                            showToast(GlobalUtil.getString(R.string.your_phone_does_not_install_qq))
                            return
                        }
                        share("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity")
                    }
                    SHARE_WECHAT -> {
                        if (!GlobalUtil.isWechatInstalled()) {
                            showToast(GlobalUtil.getString(R.string.your_phone_does_not_install_wechat))
                            return
                        }
                        share("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
                    }
                    SHARE_WEIBO -> {
                        if (!GlobalUtil.isWeiboInstalled()) {
                            showToast(GlobalUtil.getString(R.string.your_phone_does_not_install_weibo))
                            return
                        }
                        share("com.sina.weibo", "com.sina.weibo.composerinde.ComposerDispatchActivity")
                    }
                    SAVE_TO_PHONE -> {
                        saveToSDCard()
                    }
                }
            }

            override fun onDenied(deniedPermissions: List<String>) {
                var allNeverAskAgain = true
                for (deniedPermission in deniedPermissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(attachedActivity, deniedPermission)) {
                        allNeverAskAgain = false
                        break
                    }
                }
                // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                if (allNeverAskAgain) {
                    val dialog = AlertDialog.Builder(attachedActivity, R.style.GifFunAlertDialogStyle)
                            .setMessage(GlobalUtil.getString(R.string.allow_storage_permission_please))
                            .setPositiveButton(GlobalUtil.getString(R.string.settings)) { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", GlobalUtil.appPackage, null)
                                intent.data = uri
                                attachedActivity.startActivityForResult(intent, SelectGifActivity.REQUEST_PERMISSION_SETTING)
                            }
                            .setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
                            .create()
                    dialog.show()
                } else {
                    showToast(GlobalUtil.getString(R.string.must_agree_permission_to_share))
                }
            }
        })
    }

    private fun share(packageName: String, className: String) {
        if (File(imagePath).exists()) {
            val targetFile = copyGifCacheToQuxiangDir()
            if (targetFile != null) {
                val uri = ImageUtil.insertImageToSystem(attachedActivity, targetFile.path)
                if (uri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "image/gif"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    shareIntent.setClassName(packageName, className)
                    startActivity(shareIntent)
                } else {
                    showToast(GlobalUtil.getString(R.string.share_unknown_error))
                }
            } else {
                showToast(GlobalUtil.getString(R.string.share_unknown_error))
            }
        } else {
            showToast(GlobalUtil.getString(R.string.gif_file_not_exist))
        }
        dismiss()
    }

    private fun saveToSDCard() {
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/quxiang/趣享GIF")
        if (!dir.exists() && !dir.mkdirs()) {
            showToastOnUiThread(GlobalUtil.getString(R.string.save_failed))
            dismiss()
            return
        }
        val targetFile = File(dir, GlobalUtil.currentDateString + ".gif")
        if (targetFile.exists() && !targetFile.delete()) {
            showToastOnUiThread(GlobalUtil.getString(R.string.save_failed))
            dismiss()
            return
        }
        val srcFile = File(imagePath)
        if (!srcFile.exists()) {
            showToastOnUiThread(GlobalUtil.getString(R.string.gif_file_not_exist))
            dismiss()
            return
        }
        if (FileUtil.copyFile(srcFile, targetFile)) {
            ImageUtil.insertImageToSystem(attachedActivity, targetFile.path)
            showToastOnUiThread(String.format(GlobalUtil.getString(R.string.save_gif_success), targetFile.name), Toast.LENGTH_LONG)
            attachedActivity.runOnUiThread {
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val uri = Uri.fromFile(targetFile)
                intent.data = uri
                attachedActivity.sendBroadcast(intent)
            }
        } else {
            showToastOnUiThread(GlobalUtil.getString(R.string.save_failed))
        }
        dismiss()
    }

    private fun copyGifCacheToQuxiangDir(): File? {
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/quxiang/share")
        if (!dir.exists() && !dir.mkdirs()) {
            return null
        }
        val srcFile = File(imagePath)
        if (!srcFile.exists()) {
            return null
        }
        val targetFile = File(dir, srcFile.name.substringBeforeLast(".") + ".gif")
        if (targetFile.exists()) {
            return targetFile
        }
        if (FileUtil.copyFile(srcFile, targetFile)) {
            return targetFile
        }
        return null
    }

    /**
     * 检查和处理运行时权限，并将用户授权的结果通过PermissionListener进行回调。
     *
     * @param permissions
     * 要检查和处理的运行时权限数组
     * @param listener
     * 用于接收授权结果的监听器
     */
    private fun handlePermissions(permissions: Array<String>?, listener: PermissionListener) {
        if (permissions == null) {
            return
        }
        mListener = listener
        val requestPermissionList = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(attachedActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionList.add(permission)
            }
        }
        if (!requestPermissionList.isEmpty()) {
            requestPermissions(requestPermissionList.toTypedArray(), 1)
        } else {
            listener.onGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty()) {
                val deniedPermissions = ArrayList<String>()
                for (i in grantResults.indices) {
                    val grantResult = grantResults[i]
                    val permission = permissions[i]
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permission)
                    }
                }
                if (deniedPermissions.isEmpty()) {
                    mListener?.onGranted()
                } else {
                    mListener?.onDenied(deniedPermissions)
                }
            }
            else -> {
            }
        }
    }

    companion object {
        const val TAG = "ShareDialogFragment"
        const val SHARE_QQ = 0
        const val SHARE_WECHAT = 2
        const val SHARE_WEIBO = 3
        const val SAVE_TO_PHONE = 4
    }

}