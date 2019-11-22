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

package com.quxianggif.user.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.util.TypedValue
import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.user.adapter.FollowshipPagerAdapter
import kotlinx.android.synthetic.main.activity_followship.*

/**
 * 用户个人主页关注和粉丝列表的Activity。
 *
 * @author guolin
 * @since 17/7/29
 */
class FollowshipActivity : BaseActivity() {

    var mUserId: Long = 0

    private var mNickname: String = ""

    /**
     * 标识第一次进入当前Activity时是查看的关注列表还是粉丝列表。
     */
    private var isGetFollowings: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserId = intent.getLongExtra(USER_ID, 0)
        mNickname = intent.getStringExtra(NICKNAME)
        isGetFollowings = intent.getBooleanExtra(IS_GET_FOLLOWINGS, true)
        setContentView(R.layout.activity_followship)
    }

    override fun setupViews() {
        setupToolbar()
        pager.adapter = FollowshipPagerAdapter(supportFragmentManager)
        tabs.setViewPager(pager)
        setTabsValue()
        title = mNickname
        if (isGetFollowings) {
            pager.currentItem = 0
        } else {
            pager.currentItem = 1
        }
    }

    /**
     * 对PagerSlidingTabStrip的各项属性进行赋值。
     */
    private fun setTabsValue() {
        val dm = resources.displayMetrics
        // 设置Tab是自动填充满屏幕的
        tabs.shouldExpand = true
        // 设置Tab的分割线是透明的
        tabs.dividerColor = Color.TRANSPARENT
        // 设置Tab底部线的高度
        tabs.underlineHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1f, dm).toInt()
        // 设置Tab Indicator的高度
        tabs.indicatorHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, dm).toInt()
        // 设置Tab标题文字的大小
        tabs.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16f, dm).toInt()
        // 设置Tab Indicator的颜色
        tabs.indicatorColor = ContextCompat.getColor(this, R.color.colorAccent)
        // 设置选中Tab文字的颜色
        tabs.selectedTextColor = ContextCompat.getColor(this, R.color.colorAccent)
        // 取消点击Tab时的背景色
        tabs.tabBackground = 0
    }

    companion object {

        const val NICKNAME = "NICKNAME"

        const val USER_ID = "USER_ID"

        const val IS_GET_FOLLOWINGS = "IS_GET_FOLLOWINGS"

        fun actionFollowings(activity: Activity, userId: Long, nickname: String) {
            val intent = Intent(activity, FollowshipActivity::class.java)
            intent.putExtra(USER_ID, userId)
            intent.putExtra(NICKNAME, nickname)
            intent.putExtra(IS_GET_FOLLOWINGS, true)
            activity.startActivity(intent)
        }

        fun actionFollowers(activity: Activity, userId: Long, nickname: String) {
            val intent = Intent(activity, FollowshipActivity::class.java)
            intent.putExtra(USER_ID, userId)
            intent.putExtra(NICKNAME, nickname)
            intent.putExtra(IS_GET_FOLLOWINGS, false)
            activity.startActivity(intent)
        }
    }

}
