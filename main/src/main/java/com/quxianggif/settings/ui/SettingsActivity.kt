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

package com.quxianggif.settings.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.MenuItem

import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.core.extension.logDebug

/**
 * App设置界面的主Activity。
 *
 * @author guolin
 * @since 2018/5/14
 */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_settings)
        val settingsType = intent.getIntExtra(INTENT_SETTINGS_TYPE, MAIN_SETTINGS)
        gotoSettings(settingsType)
    }

    override fun setupViews() {
        setupToolbar()
    }

    fun gotoSettings(settingsType: Int) {
        var settingsFragment: Fragment? = null
        when (settingsType) {
            MAIN_SETTINGS -> settingsFragment = MainSettingsFragment()
            GIF_SETTINGS -> settingsFragment = GifSettingsFragment()
            else -> {
            }
        }
        if (settingsFragment != null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settingsFragmentLayout, settingsFragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onBackPressed() {
        val fragments = supportFragmentManager.backStackEntryCount
        logDebug(TAG, "fragments is $fragments")
        if (fragments == 1) {
            finish()
        } else {
            if (fragments > 1) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val TAG = "SettingsActivity"

        const val MAIN_SETTINGS = 0

        const val GIF_SETTINGS = 1

        private const val INTENT_SETTINGS_TYPE = "intent_settings_type"

        fun actionStart(activity: Activity) {
            val intent = Intent(activity, SettingsActivity::class.java)
            activity.startActivity(intent)
        }

        fun actionStartGIFSettings(activity: Activity) {
            val intent = Intent(activity, SettingsActivity::class.java)
            intent.putExtra(INTENT_SETTINGS_TYPE, GIF_SETTINGS)
            activity.startActivity(intent)
        }
    }
}
