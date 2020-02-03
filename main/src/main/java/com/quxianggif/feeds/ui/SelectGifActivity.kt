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

package com.quxianggif.feeds.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.quxianggif.R
import com.quxianggif.common.callback.PermissionListener
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.common.ui.NeedPermissionFragment

/**
 * 选择GIF图的Activity。
 *
 * @author guolin
 * @since 17/3/21
 */
class SelectGifActivity : BaseActivity() {

    lateinit var currentFragment: Fragment

    var selectedFragment: Int = 0

    var rootPath: String = ""

    var currentPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_gif)
        refreshPermissionStatus()
    }

    override fun setupViews() {
        setupToolbar()
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        selectedFragment = ALBUM_FRAGMENT
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
        var fragment: Fragment? = null
        when (selectedFragment) {
            ALBUM_FRAGMENT -> fragment = GifAlbumFragment()
//            SDCARD_FRAGMENT -> fragment = FileBrowserFragment()
        }
        if (fragment != null) {
            replaceFragment(fragment)
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commitAllowingStateLoss()
    }

// Android 10 之后不允许直接访问SD卡，不再提供从SD卡中选择GIF图片的方式
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_select_gif, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.album_mode -> {
//                selectedFragment = ALBUM_FRAGMENT
//                refreshPermissionStatus()
//                return true
//            }
//            R.id.sdcard_mode -> {
//                selectedFragment = SDCARD_FRAGMENT
//                refreshPermissionStatus()
//                return true
//            }
//            else -> {
//            }
//        }
//        if (::currentFragment.isInitialized && currentFragment is FileBrowserFragment) {
//            handleFileExplorerBackStack()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PERMISSION_SETTING -> refreshPermissionStatus()
            else -> {
            }
        }
    }

    companion object {

        private const val TAG = "SelectGifActivity"

        const val REQUEST_PERMISSION_SETTING = 1

        private const val ALBUM_FRAGMENT = 0
        private const val SDCARD_FRAGMENT = 1

        fun actionStartForResult(activity: Activity, requestCode: Int) {
            val intent = Intent(activity, SelectGifActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }
    }

}
