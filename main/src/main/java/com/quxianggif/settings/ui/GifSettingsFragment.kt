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

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.quxianggif.R

/**
 * GIF设置界面的Fragment。
 *
 * @author guolin
 * @since 2018/5/14
 */
class GifSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.gif_preferences)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity
        if (activity != null) {
            activity.title = getString(R.string.title_gif_play_control)
            val currentGifPlaySpeed = PreferenceManager.getDefaultSharedPreferences(activity)
                    .getString(getString(R.string.key_gif_play_speed), "3") ?: "3"

            val gifPlaySpeed = findPreference<Preference>(getString(R.string.key_gif_play_speed))
            gifPlaySpeed?.summary = getGifPlaySpeedForDisplay(currentGifPlaySpeed)
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val gifPlaySpeed = findPreference<Preference>(getString(R.string.key_gif_play_speed))
        if (gifPlaySpeed is ListPreference) {
            gifPlaySpeed.setSummary(gifPlaySpeed.entry)
        }
    }

    private fun getGifPlaySpeedForDisplay(gifPlaySpeed: String) = when (gifPlaySpeed) {
        "1" -> "1/3 速度"
        "2" -> "1/2 速度"
        "3" -> "正常速度"
        "4" -> "1.5 倍速度"
        "5" -> "2 倍速度"
        else -> "正常速度"
    }

    companion object {

        private val TAG = "GifSettingsFragment"
    }
}
