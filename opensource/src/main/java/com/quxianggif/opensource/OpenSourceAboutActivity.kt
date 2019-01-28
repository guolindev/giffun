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

package com.quxianggif.opensource

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.settings.ui.AboutActivity

class OpenSourceAboutActivity : AboutActivity() {

    override fun setupViews() {
        super.setupViews()
        button.text = GlobalUtil.getString(R.string.download_official)
        button.setOnClickListener {
            val uri = Uri.parse("market://details?id=club.giffun.app")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            val packageManager = GifFun.getContext().packageManager
            val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                startActivity(intent)
            } else {
                showToast(GlobalUtil.getString(R.string.unable_goto_app_store), Toast.LENGTH_LONG)
            }
        }
    }

    companion object {

        const val TAG = "OpenSourceAboutActivity"

    }
}
