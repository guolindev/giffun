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

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import androidx.palette.graphics.Palette
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quxianggif.R
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.core.Const
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.*
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.core.util.SharedUtil
import com.quxianggif.event.MessageEvent
import com.quxianggif.event.ModifyUserInfoEvent
import com.quxianggif.settings.ui.SettingsActivity
import com.quxianggif.user.ui.ModifyUserInfoActivity
import com.quxianggif.user.ui.RecommendFollowingActivity
import com.quxianggif.user.ui.UserHomePageActivity
import com.quxianggif.util.AnimUtils
import com.quxianggif.util.ColorUtils
import com.quxianggif.util.UserUtil
import com.quxianggif.util.glide.CustomUrl
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.LitePalDB
import java.util.*

/**
 * 趣享GIF的主界面。
 *
 * @author guolin
 * @since 17/2/15
 */
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var pagerAdapter: Adapter

    private lateinit var nicknameMe: TextView

    private lateinit var descriptionMe: TextView

    private lateinit var avatarMe: ImageView

    private lateinit var editImage: ImageView

    private var backPressTime = 0L

    private var currentPagerPosition = 0

    internal var isNeedToRefresh = false

    private var navHeaderBgLoadListener: RequestListener<Any, GlideDrawable> = object : RequestListener<Any, GlideDrawable> {

        override fun onException(e: Exception?, model: Any, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onResourceReady(glideDrawable: GlideDrawable?, model: Any, target: Target<GlideDrawable>,
                                     isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            if (glideDrawable == null) {
                return false
            }
            val bitmap = glideDrawable.toBitmap()
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            if (bitmapWidth <= 0 || bitmapHeight <= 0) {
                return false
            }
            val left = (bitmapWidth * 0.2).toInt()
            val right = bitmapWidth - left
            val top = bitmapHeight / 2
            val bottom = bitmapHeight - 1
            logDebug(TAG, "text area top $top , bottom $bottom , left $left , right $right")
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    .setRegion(left, top, right, bottom) // 测量图片下半部分的颜色，以确定用户信息的颜色
                    .generate { palette ->
                        val isDark = ColorUtils.isBitmapDark(palette, bitmap)
                        val color: Int
                        color = if (isDark) {
                            ContextCompat.getColor(this@MainActivity, R.color.white_text)
                        } else {
                            ContextCompat.getColor(this@MainActivity, R.color.primary_text)
                        }
                        nicknameMe.setTextColor(color)
                        descriptionMe.setTextColor(color)
                        editImage.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY)
                    }
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkIsNeedToRefresh()
        initDatabase()
        setContentView(R.layout.activity_main)
    }

    override fun setupViews() {
        setupToolbar()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        setupViewPager(viewpager)
        tabs.setupWithViewPager(viewpager)
        tabs.addOnTabSelectedListener(tabSelectedListener)
        composeFab.setOnClickListener {
            PostFeedActivity.actionStart(this)
        }
        navView.setNavigationItemSelectedListener(this)
        popFab()
        animateToolbar()
        navView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                navView.viewTreeObserver.removeOnPreDrawListener(this)
                loadUserInfo()
                return false
            }
        })
    }

    private fun checkIsNeedToRefresh() {
        val autoRefresh = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(GlobalUtil.getString(R.string.key_auto_refresh), true)
        if (autoRefresh) {
            val lastUseTime = SharedUtil.read(Const.Feed.MAIN_LAST_USE_TIME, 0L)
            val timeNotUsed = System.currentTimeMillis() - lastUseTime
            logDebug(TAG, "not used for " + timeNotUsed / 1000 + " seconds")
            if (timeNotUsed > 10 * 60 * 1000) { // 超过10分钟未使用
                isNeedToRefresh = true
            }
        }
    }

    private fun initDatabase() {
        val litepalDB = LitePalDB.fromDefault("giffun_" + GifFun.getUserId().toString())
        LitePal.use(litepalDB)
    }

    /**
     * 加载登录用户的信息，头像和昵称等。
     */
    private fun loadUserInfo() {
        val count = navView.headerCount
        if (count == 1) {
            val nickname = UserUtil.nickname
            val avatar = UserUtil.avatar
            val description = UserUtil.description
            val bgImage = UserUtil.bgImage
            val headerView = navView.getHeaderView(0)
            val userLayout = headerView.findViewById<LinearLayout>(R.id.userLayout)
            val descriptionLayout = headerView.findViewById<LinearLayout>(R.id.descriptionLayout)
            val navHeaderBg = headerView.findViewById<ImageView>(R.id.navHeaderBgImage)
            avatarMe = headerView.findViewById(R.id.avatarMe)
            nicknameMe = headerView.findViewById(R.id.nicknameMe)
            descriptionMe = headerView.findViewById(R.id.descriptionMe)
            editImage = headerView.findViewById(R.id.editImage)

            nicknameMe.text = nickname
            if (TextUtils.isEmpty(description)) {
                descriptionMe.text = GlobalUtil.getString(R.string.edit_description)
            } else {
                descriptionMe.text = String.format(GlobalUtil.getString(R.string.description_content), description)
            }
            Glide.with(this)
                    .load(CustomUrl(avatar))
                    .bitmapTransform(CropCircleTransformation(activity))
                    .placeholder(R.drawable.loading_bg_circle)
                    .error(R.drawable.avatar_default)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(avatarMe)

            if (TextUtils.isEmpty(bgImage)) {
                if (!TextUtils.isEmpty(avatar)) {
                    Glide.with(this)
                            .load(CustomUrl(avatar))
                            .bitmapTransform(BlurTransformation(this, 15))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .listener(navHeaderBgLoadListener)
                            .into(navHeaderBg)
                }
            } else {
                val bgImageWidth = navView.width
                val bgImageHeight = dp2px((250 + 25).toFloat() /* 25为补偿系统状态栏高度，不加这个高度值图片顶部会出现状态栏的底色 */)
                Glide.with(this)
                        .load(bgImage)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .override(bgImageWidth, bgImageHeight)
                        .listener(navHeaderBgLoadListener)
                        .into(navHeaderBg)
            }
            userLayout.setOnClickListener { UserHomePageActivity.actionStart(this@MainActivity, avatarMe, GifFun.getUserId(), nickname, avatar, bgImage) }
            descriptionLayout.setOnClickListener { ModifyUserInfoActivity.actionEditDescription(this@MainActivity) }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        pagerAdapter = Adapter(supportFragmentManager)
        pagerAdapter.addFragment(WorldFeedsFragment(), GlobalUtil.getString(R.string.world))
        pagerAdapter.addFragment(FollowingFeedsFragment(), GlobalUtil.getString(R.string.follow))
        pagerAdapter.addFragment(HotFeedsFragment(), GlobalUtil.getString(R.string.hot))
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2
        currentPagerPosition = SharedUtil.read(Const.Feed.MAIN_PAGER_POSITION, 0)
        if (currentPagerPosition < 0 || currentPagerPosition >= pagerAdapter.count) {
            currentPagerPosition = 0
        }
        viewPager.currentItem = currentPagerPosition
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                currentPagerPosition = position
                executePendingRunnable()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * 使用pop动画的方式将fab按钮显示出来。
     */
    private fun popFab() {
        composeFab.show()
        composeFab.alpha = 0f
        composeFab.scaleX = 0f
        composeFab.scaleY = 0f
        val animator = ObjectAnimator.ofPropertyValuesHolder(
                composeFab,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
        animator.startDelay = 200
        animator.start()
    }

    /**
     * 使用缩放动画的方式将Toolbar标题显示出来。
     */
    private fun animateToolbar() {
        val t = toolbar?.getChildAt(0)
        if (t != null && t is TextView) {
            t.alpha = 0f
            t.scaleX = 0.8f
            t.animate()
             .alpha(1f)
             .scaleX(1f)
             .setStartDelay(300)
             .setDuration(900).interpolator = AnimUtils.getFastOutSlowInInterpolator(this)
        }
    }

    /**
     * 执行Pending任务，用于同步ViewPager各面页签之间的状态。
     */
    private fun executePendingRunnable() {
        val fragment = pagerAdapter.getItem(currentPagerPosition)
        if (fragment is BaseFeedsFragment) {
            fragment.executePendingRunnableList()
        }
    }

    override fun onResume() {
        super.onResume()
        executePendingRunnable()
    }

    override fun onPause() {
        super.onPause()
        SharedUtil.save(Const.Feed.MAIN_PAGER_POSITION, currentPagerPosition)
        SharedUtil.save(Const.Feed.MAIN_LAST_USE_TIME, System.currentTimeMillis())
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val now = System.currentTimeMillis()
            if (now - backPressTime > 2000) {
                showToast(String.format(GlobalUtil.getString(R.string.press_again_to_exit), GlobalUtil.appName))
                backPressTime = now
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
            R.id.menu_search -> {
                if (AndroidVersion.hasLollipopMR1()) { // Android 5.0版本启用transition动画会存在一些效果上的异常，因此这里只在Android 5.1以上启用此动画
                    val searchMenuView: View? = toolbar?.findViewById(R.id.menu_search)
                    val options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                            getString(R.string.transition_search_back)).toBundle()
                    startActivityForResult(Intent(this, SearchActivity::class.java), REQUEST_SEARCH, options)
                } else {
                    startActivityForResult(Intent(this, SearchActivity::class.java), REQUEST_SEARCH)
                }
                composeFab.visibility = View.GONE // 当进入搜索界面键盘弹出时，composeFab会随着键盘往上偏移。暂时没查到原因，使用隐藏的方式先进行规避
            }
        }
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is ModifyUserInfoEvent) {
            if (messageEvent.modifyAvatar || messageEvent.modifyBgImage || messageEvent.modifyDescription || messageEvent.modifyNickname) {
                loadUserInfo()
            }
        } else {
            super.onMessageEvent(messageEvent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SEARCH -> popFab() // 进行搜索界面时会将composeFab隐藏，返回MainActivity时需要将composeFab重新显示。
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tabs.removeOnTabSelectedListener(tabSelectedListener)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.compose -> GifFun.getHandler().postDelayed(300){ PostFeedActivity.actionStart(this) }
            R.id.user_home -> GifFun.getHandler().postDelayed(300) {
                UserHomePageActivity.actionStart(this, avatarMe, GifFun.getUserId(),
                        UserUtil.nickname, UserUtil.avatar, UserUtil.bgImage)
            }
            R.id.draft -> GifFun.getHandler().postDelayed(300) { DraftActivity.actionStart(this) }
            R.id.recommend_following -> GifFun.getHandler().postDelayed(300) { RecommendFollowingActivity.actionStart(this) }
            R.id.settings -> GifFun.getHandler().postDelayed(300) { SettingsActivity.actionStart(this) }
        }
        GifFun.getHandler().post {
            uncheckNavigationItems()
            drawerLayout.closeDrawers()
        }
        return true
    }

    private fun uncheckNavigationItems() {
        navView.setCheckedItem(R.id.none)
    }

    internal class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val mFragments = ArrayList<Fragment>()
        private val mFragmentTitles = ArrayList<String>()

        fun addFragment(fragment: Fragment, title: String) {
            mFragments.add(fragment)
            mFragmentTitles.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitles[position]
        }
    }

    private val tabSelectedListener by lazy {
        object : TabLayout.ViewPagerOnTabSelectedListener(viewpager) {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                super.onTabReselected(tab)
                if (tab != null) {
                    val fragment = pagerAdapter.getItem(tab.position)
                    if (fragment is BaseFeedsFragment) {
                        fragment.scrollToTop()
                    }
                }
                println("on tab onTabReselected ${tab?.position}")
            }
        }
    }

    companion object {

        private const val TAG = "MainActivity"

        private const val REQUEST_SEARCH = 10000

        fun actionStart(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
