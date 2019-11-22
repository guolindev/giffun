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

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import android.widget.Toast
import com.quxianggif.R
import com.quxianggif.common.ui.WebViewActivity
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.postDelayed
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.FollowingFeed
import com.quxianggif.core.model.RefFeed
import com.quxianggif.core.model.WorldFeed
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.event.CleanCacheEvent
import com.quxianggif.login.ui.LoginActivity
import com.quxianggif.util.ActivityCollector
import com.quxianggif.util.glide.GlideUtil
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.extension.deleteAllAsync
import java.util.*

/**
 * App设置主界面的Fragment。
 *
 * @author guolin
 * @since 2018/5/14
 */
class MainSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var settingsActivity: SettingsActivity

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity == null) return
        settingsActivity = activity as SettingsActivity
        settingsActivity.title = getString(R.string.settings)

        val gifPlay = findPreference<Preference>(getString(R.string.key_gif_play_control))
        gifPlay?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            GifFun.getHandler().postDelayed(300) {
                settingsActivity.gotoSettings(SettingsActivity.GIF_SETTINGS)
            }
            true
        }

        val cleanCache = findPreference<Preference>(getString(R.string.key_clean_cache))
        cleanCache?.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
            val cacheSize = GlideUtil.cacheSize
            if (cacheSize > ONE_MEGA_BYTE) {
                val cacheSizeInMegaBytes = String.format(Locale.ENGLISH, "%.1f", cacheSize.toDouble() / 1024.0 / 1024.0) + "M"
                val message = String.format(GlobalUtil.getString(R.string.are_your_sure_to_clean_all_caches), cacheSizeInMegaBytes)
                val dialog = AlertDialog.Builder(settingsActivity, R.style.GifFunAlertDialogStyle)
                        .setMessage(message)
                        .setPositiveButton(GlobalUtil.getString(R.string.ok)) { _, _ ->
                            val event = CleanCacheEvent()
                            EventBus.getDefault().post(event)
                            GlideUtil.clearCache()
                            showToast(GlobalUtil.getString(R.string.clean_cache_success))
                        }
                        .setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
                        .create()
                dialog.show()
            } else {
                showToast(GlobalUtil.getString(R.string.no_cache_to_clean))
            }
            true
        }

        val appInfo = findPreference<Preference>(getString(R.string.key_app_info))
        appInfo?.setOnPreferenceClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.parse("package:" + GlobalUtil.appPackage)
            intent.data = uri
            startActivity(intent)
            true
        }

        val about = findPreference<Preference>(getString(R.string.key_about))
        about?.setOnPreferenceClickListener {
            AboutActivity.actionStart(settingsActivity)
            true
        }

        val userTerms = findPreference<Preference>(getString(R.string.key_user_terms))
        userTerms?.setOnPreferenceClickListener {
            WebViewActivity.actionStart(settingsActivity, getString(R.string.title_user_terms), USER_TERMS_URL)
            true
        }

        val logout = findPreference<Preference>(getString(R.string.key_logout))
        logout?.setOnPreferenceClickListener {
            logout()
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.main_preferences)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.key_check_update)) {
            val checkUpdate = findPreference<Preference>(key)
            if (checkUpdate is SwitchPreferenceCompat && !checkUpdate.isChecked) {
                showToast(GlobalUtil.getString(R.string.check_update_in_about_if_you_need), Toast.LENGTH_LONG)
            }
        }
    }

    /**
     * 注销当前登录账号。
     */
    private fun logout() {
        val dialog = AlertDialog.Builder(settingsActivity, R.style.GifFunAlertDialogStyle)
                .setMessage(GlobalUtil.getString(R.string.confirm_to_logout))
                .setPositiveButton(GlobalUtil.getString(R.string.ok)) { _, _ ->
                    GifFun.logout()
                    LitePal.deleteAllAsync<WorldFeed>().listen(null)
                    LitePal.deleteAllAsync<FollowingFeed>().listen(null)
                    LitePal.deleteAllAsync<RefFeed>().listen(null)
                    ActivityCollector.finishAll()
                    LoginActivity.actionStart(settingsActivity, false, null)
                }
                .setNegativeButton(GlobalUtil.getString(R.string.cancel), null)
                .create()
        dialog.show()
    }

    companion object {

        var ONE_MEGA_BYTE = (1024 * 1024).toLong()

        var USER_TERMS_URL = "http://quxianggif.com/terms.html"
    }

}
