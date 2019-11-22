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

package com.quxianggif.user.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.quxianggif.R
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.user.ui.FollowersFragment
import com.quxianggif.user.ui.FollowingsFragment

/**
 * 用户个人主页关注和粉丝列表的ViewPagerAdapter。
 *
 * @author guolin
 * @since 17/7/30
 */
class FollowshipPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var followingsFragment: FollowingsFragment? = null

    private var followersFragment: FollowersFragment? = null

    private val titles = arrayOf(GlobalUtil.getString(R.string.followings), GlobalUtil.getString(R.string.followers))

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                if (followingsFragment == null) {
                    followingsFragment = FollowingsFragment()
                }
                followingsFragment!!
            }
            1 -> {
                if (followersFragment == null) {
                    followersFragment = FollowersFragment()
                }
                followersFragment!!
            }
            else -> followersFragment!!
        }
    }

}