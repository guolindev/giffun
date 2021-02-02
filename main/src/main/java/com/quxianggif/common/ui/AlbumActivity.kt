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
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.quxianggif.R
import com.quxianggif.common.callback.PermissionListener
import com.quxianggif.core.extension.logDebug
import com.quxianggif.core.extension.logWarn
import com.theartofdev.edmodo.cropper.CropImage

/**
 * 展示手机相册的Activity。
 *
 * @author guolin
 * @since 17/11/15
 */
class AlbumActivity : BaseActivity() {

    private var cropWidth: Int = 0

    private var cropHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        refreshPermissionStatus()
    }

    override fun setupViews() {
        setupToolbar()
        cropWidth = intent.getIntExtra(CROP_WIDTH, 0)
        cropHeight = intent.getIntExtra(CROP_HEIGHT, 0)
    }

    private fun refreshPermissionStatus() {
        handlePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionListener {
            override fun onGranted() {
                permissionsGranted()
            }

            override fun onDenied(deniedPermissions: List<String>) {
                val fragment = NeedPermissionFragment()
                fragment.setPermissions(deniedPermissions.toTypedArray())
                replaceFragment(fragment)
            }
        })
    }

    override fun permissionsGranted() {
        val fragment = AlbumFragment()
        fragment.setCropSize(cropWidth, cropHeight)
        replaceFragment(fragment)
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commitAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PERMISSION_SETTING -> refreshPermissionStatus()
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == RESULT_CANCELED) {
                    return
                }
                val intent = Intent()
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    logDebug(TAG, "uri is ${result.uri} , uri path is ${result.uri.path}")
                    intent.putExtra(AlbumActivity.IMAGE_URI, result.uri)
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    logWarn(TAG, "Cropping failed: " + result.error.message, result.error)
                }
                activity?.setResult(resultCode, intent)
                activity?.finish()
            }
            else -> {
            }
        }
    }

    companion object {

        private const val TAG = "AlbumActivity"

        const val REQUEST_PERMISSION_SETTING = 1

        const val IMAGE_URI = "image_uri"

        const val CROP_WIDTH = "crop_width"

        const val CROP_HEIGHT = "crop_height"

        fun actionStartForResult(activity: Activity, requestCode: Int, cropWidth: Int, cropHeight: Int) {
            val intent = Intent(activity, AlbumActivity::class.java)
            intent.putExtra(CROP_WIDTH, cropWidth)
            intent.putExtra(CROP_HEIGHT, cropHeight)
            activity.startActivityForResult(intent, requestCode)
        }
    }


}
