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

import android.annotation.TargetApi
import android.app.SharedElementCallback
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import androidx.annotation.TransitionRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import android.text.InputType
import android.text.TextUtils
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.DisplayMetrics
import android.util.SparseArray
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import com.quxianggif.R
import com.quxianggif.common.callback.InfiniteScrollListener
import com.quxianggif.common.callback.LoadDataListener
import com.quxianggif.common.transitions.CircularReveal
import com.quxianggif.common.transitions.TransitionUtils
import com.quxianggif.common.ui.BaseActivity
import com.quxianggif.common.view.SearchItemDividerDecoration
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.dp2px
import com.quxianggif.core.extension.logWarn
import com.quxianggif.core.extension.postDelayed
import com.quxianggif.core.extension.showToast
import com.quxianggif.core.model.BaseFeed
import com.quxianggif.core.model.SearchItem
import com.quxianggif.core.util.AndroidVersion
import com.quxianggif.core.util.GlobalUtil
import com.quxianggif.feeds.adapter.SearchAdapter
import com.quxianggif.network.model.Callback
import com.quxianggif.network.model.Response
import com.quxianggif.network.model.SearchFeeds
import com.quxianggif.network.model.SearchMixed
import com.quxianggif.util.ResponseHandler
import kotlinx.android.synthetic.main.activity_search.*

/**
 * 执行搜索请求和展示搜索结果的界面。
 *
 * @author guolin
 * @since 18/7/22
 */
class SearchActivity : BaseActivity(), LoadDataListener {

    private lateinit var adapter: SearchAdapter

    private lateinit var layoutManager: LinearLayoutManager

    val searchItemList = ArrayList<SearchItem>()

    private val transitions = SparseArray<Transition>()

    var isLoadFailed = false

    /**
     * 判断是否正在加载更多数据。
     */
    private var isLoading = false

    /**
     * 判断是否还有更多数据。
     *
     * @return 当服务器端没有更多数据时，此值为true，否则此值为false。
     */
    var isNoMoreData = false

    /**
     * 搜索的关键字。
     */
    var keyword = ""

    /**
     * 通过获取屏幕宽度来计算出每张图片最大的宽度。
     *
     * @return 计算后得出的每张图片最大的宽度。
     */
    private val maxImageWidth: Int
        get() {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay?.getMetrics(metrics)
            val columnWidth = metrics.widthPixels
            return columnWidth - dp2px((24 + 20).toFloat())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun setupViews() {
        super.setupViews()
        setupSearchView()
        setupRecyclerView()
        setupTransitions()
        loading?.visibility = View.GONE
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) { // 当手机系统低于6.0时，使用代码的方式隐藏SearchView hint图标
            try {
                val magId = resources.getIdentifier("android:id/search_mag_icon", null, null)
                val magImage: ImageView = searchView.findViewById(magId)
                magImage.layoutParams = LinearLayout.LayoutParams(0, 0)
            } catch (e: Exception) {
                logWarn(TAG, e.message, e)
            }
        }
    }

    private fun setupSearchView() {
        searchView.queryHint = GlobalUtil.getString(R.string.search_hint)
        searchView.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
        searchView.imeOptions = searchView.imeOptions or EditorInfo.IME_ACTION_SEARCH or
                EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchFor(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (TextUtils.isEmpty(query)) {
                    clearResults()
                }
                return true
            }
        })

        searchBack.setOnClickListener {
            dismiss()
        }
        scrim.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(activity)
        adapter = SearchAdapter(this@SearchActivity, searchItemList, maxImageWidth)
        searchResults.layoutManager = layoutManager
        searchResults.adapter = adapter
        searchResults.addItemDecoration(SearchItemDividerDecoration(this@SearchActivity, dp2px(65f)))
        (searchResults.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        searchResults.addOnScrollListener(object : InfiniteScrollListener(layoutManager) {

            override fun onLoadMore() {
                onLoad()
            }

            override fun isDataLoading() = isLoading

            override fun isNoMoreData() = isNoMoreData

        })
    }

    @TargetApi(22)
    private fun setupTransitions() {
        if (AndroidVersion.hasLollipopMR1()) {
            setEnterSharedElementCallback(object : SharedElementCallback() {
                override fun onSharedElementStart(sharedElementNames: List<String>, sharedElements: List<View>?, sharedElementSnapshots: List<View>) {
                    if (sharedElements != null && sharedElements.isNotEmpty()) {
                        val searchIcon = sharedElements[0]
                        if (searchIcon.id != R.id.searchBack) return
                        val centerX = (searchIcon.left + searchIcon.right) / 2
                        val hideResults = TransitionUtils.findTransition(window.returnTransition as TransitionSet, CircularReveal::class.java, R.id.resultsContainer) as CircularReveal?
                        hideResults?.setCenter(Point(centerX, 0))
                    }
                }
            })

            window.enterTransition.addListener(object : TransitionUtils.TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    searchView.requestFocus()
                    showKeyboard()
                }
            })
        } else {
            searchView.requestFocus()
            GifFun.getHandler().postDelayed(100) {
                showKeyboard()
            }
        }
    }

    private fun searchFor(query: String) {
        keyword = query
        if (keyword.isBlank()) {
            showToast(GlobalUtil.getString(R.string.search_keyword_can_not_be_blank))
        } else {
            clearResults()
            loading?.visibility = View.VISIBLE
            hideKeyboard()

            isLoading = true
            isNoMoreData = false
            GifFun.getHandler().postDelayed(600) {
                SearchMixed.getResponse(keyword, object : Callback {
                    override fun onResponse(response: Response) {
                        if (activity == null) {
                            return
                        }
                        isLoading = false
                        if (!ResponseHandler.handleResponse(response)) {
                            val searchMixed = response as SearchMixed
                            val status = searchMixed.status
                            when (status) {
                                0 -> {
                                    searchItemList.addAll(searchMixed.users)
                                    searchItemList.addAll(searchMixed.feeds)
                                    if (searchMixed.feeds.size < 10) {
                                        isNoMoreData = true
                                    }
                                    adapter.notifyDataSetChanged()
                                    loadFinished()
                                }
                                10004 -> {
                                    loadFinished()
                                }
                                10501 -> {
                                    loadFailed(GlobalUtil.getString(R.string.search_keyword_can_not_be_blank))
                                }
                                10502 -> {
                                    loadFailed(GlobalUtil.getString(R.string.search_keyword_to_long))
                                }
                                else -> {
                                    logWarn(TAG, "Search failed. " + GlobalUtil.getResponseClue(status, searchMixed.msg))
                                    loadFailed(GlobalUtil.getString(R.string.search_failed) + " " + status)
                                }
                            }
                        } else {
                            loadFailed(GlobalUtil.getString(R.string.unknown_error) + ": " + response.status)
                        }
                    }

                    override fun onFailure(e: Exception) {
                        logWarn(TAG, e.message, e)
                        isLoading = false
                        loadFailed(GlobalUtil.getString(R.string.search_failed_bad_network))
                    }
                })
            }

        }
    }

    private fun searchMoreFeeds(keyword: String, lastFeed: Long) {
        isLoading = true
        SearchFeeds.getResponse(keyword, lastFeed, object : Callback {
            override fun onResponse(response: Response) {
                if (activity == null) {
                    return
                }
                isLoading = false
                if (!ResponseHandler.handleResponse(response)) {
                    val searchFeeds = response as SearchFeeds
                    val status = searchFeeds.status
                    when (status) {
                        0 -> {
                            searchItemList.addAll(searchFeeds.data)
                            adapter.notifyDataSetChanged()
                            searchResults.stopScroll()
                        }
                        10004 -> {
                            isNoMoreData = true
                            adapter.notifyItemChanged(adapter.itemCount - 1)
                        }
                        else -> {
                            loadFailed(GlobalUtil.getString(R.string.search_failed) + ": " + response.status)
                        }
                    }
                } else {
                    loadFailed(GlobalUtil.getString(R.string.unknown_error) + ": " + response.status)
                }
            }

            override fun onFailure(e: Exception) {
                logWarn(TAG, e.message, e)
                isLoading = false
                loadFailed(null)
            }
        })
    }

    private fun clearResults() {
        runAutoTransition()
        adapter.clear()
        searchResults.visibility = View.INVISIBLE
        loading?.visibility = View.GONE
        hideNoContentView()
        hideLoadErrorView()
    }

    /**
     * 加载完成，将数据显示出来，将加载等待控件隐藏。
     */
    override fun loadFinished() {
        super.loadFinished()
        isLoadFailed = false
        runAutoTransition()
        loading?.visibility = View.GONE
        if (searchItemList.isEmpty()) {
            showNoContentView(GlobalUtil.getString(R.string.search_no_result))
        } else {
            searchResults.visibility = View.VISIBLE
        }
    }


    /**
     * 加载评论失败，将加载等待控件隐藏。
     */
    override fun loadFailed(msg: String?) {
        super.loadFailed(msg)
        isLoadFailed = true
        if (searchItemList.isEmpty()) {
            runAutoTransition()
            loading?.visibility = View.GONE
            if (msg != null) {
                showLoadErrorView(msg)
            }
        } else {
            adapter.notifyItemChanged(adapter.itemCount - 1)
        }
    }

    override fun onLoad() {
        if (searchItemList.isNotEmpty()) {
            val searchItem = searchItemList.last()
            if (searchItem is BaseFeed) {
                searchMoreFeeds(keyword, searchItem.feedId)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getTransition(@TransitionRes transitionId: Int): Transition {
        var transition: Transition? = transitions.get(transitionId)
        return if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId)
            transitions.put(transitionId, transition)
            transition
        } else {
            transition
        }
    }

    private fun dismiss() {
        // clear the background else the touch ripple moves with the translation which looks bad
        searchBack.background = null
        if (AndroidVersion.hasLollipop()) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    private fun runAutoTransition() {
        if (AndroidVersion.hasLollipop()) {
            TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto))
        }
    }

    private fun showKeyboard() {
        try {
            val id = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
            val editText = searchView.findViewById<EditText>(id)
            showSoftKeyboard(editText)
        } catch (e: Exception) {
            logWarn(TAG, e.message, e)
        }
    }

    private fun hideKeyboard() {
        searchView.clearFocus()
        resultsContainer.requestFocus()
        hideSoftKeyboard()
    }

    companion object {

        private const val TAG = "SearchActivity"

    }

}
