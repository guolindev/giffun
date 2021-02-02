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

import android.content.pm.PackageManager
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.quxianggif.R
import com.quxianggif.common.callback.PermissionListener
import com.quxianggif.common.callback.RequestLifecycle
import java.util.*

/**
 * 应用程序中所有Fragment的基类。
 *
 * @author guolin
 * @since 17/3/20
 */
open class BaseFragment : Fragment(), RequestLifecycle {

    private var mListener: PermissionListener? = null

    /**
     * Fragment中由于服务器异常导致加载失败显示的布局。
     */
    private var loadErrorView: View? = null

    /**
     * Fragment中由于网络异常导致加载失败显示的布局。
     */
    private var badNetworkView: View? = null

    /**
     * Fragment中当界面上没有任何内容时展示的布局。
     */
    private var noContentView: View? = null

    /**
     * Fragment中inflate出来的布局。
     */
    protected var rootView: View? = null

    /**
     * Fragment中显示加载等待的控件。
     */
    protected var loading: ProgressBar? = null

    /**
     * 当Fragment中的加载内容服务器返回失败，通过此方法显示提示界面给用户。
     *
     * @param tip
     * 界面中的提示信息
     */
    protected fun showLoadErrorView(tip: String) {
        if (loadErrorView != null) {
            loadErrorView?.visibility = View.VISIBLE
            return
        }
        if (rootView != null) {
            val viewStub = rootView?.findViewById<ViewStub>(R.id.loadErrorView)
            if (viewStub != null) {
                loadErrorView = viewStub.inflate()
                val loadErrorText = loadErrorView?.findViewById<TextView>(R.id.loadErrorText)
                loadErrorText?.text = tip
            }
        }
    }

    /**
     * 当Fragment中的内容因为网络原因无法显示的时候，通过此方法显示提示界面给用户。
     *
     * @param listener
     * 重新加载点击事件回调
     */
    protected fun showBadNetworkView(listener: View.OnClickListener) {
        if (badNetworkView != null) {
            badNetworkView?.visibility = View.VISIBLE
            return
        }
        if (rootView != null) {
            val viewStub = rootView?.findViewById<ViewStub>(R.id.badNetworkView)
            if (viewStub != null) {
                badNetworkView = viewStub.inflate()
                val badNetworkRootView = badNetworkView?.findViewById<View>(R.id.badNetworkRootView)
                badNetworkRootView?.setOnClickListener(listener)
            }
        }
    }

    /**
     * 当Fragment中没有任何内容的时候，通过此方法显示提示界面给用户。
     *
     * @param tip
     * 界面中的提示信息
     */
    protected fun showNoContentView(tip: String) {
        if (noContentView != null) {
            noContentView?.visibility = View.VISIBLE
            return
        }
        if (rootView != null) {
            val viewStub = rootView?.findViewById<ViewStub>(R.id.noContentView)
            if (viewStub != null) {
                noContentView = viewStub.inflate()
                val noContentText = noContentView?.findViewById<TextView>(R.id.noContentText)
                noContentText?.text = tip
            }
        }
    }

    /**
     * 当Fragment中没有任何内容的时候，通过此方法显示提示界面给用户。
     * @param tip
     * 界面中的提示信息
     * @param buttonText
     * 界面中的按钮的文字
     * @param listener
     * 按钮的点击事件回调
     */
    protected fun showNoContentViewWithButton(tip: String, buttonText: String, listener: View.OnClickListener) {
        if (noContentView != null) {
            noContentView?.visibility = View.VISIBLE
            return
        }
        if (rootView != null) {
            val viewStub = rootView?.findViewById<ViewStub>(R.id.noContentViewWithButton)
            if (viewStub != null) {
                noContentView = viewStub.inflate()
                val noContentText = noContentView?.findViewById<TextView>(R.id.noContentText)
                val noContentButton = noContentView?.findViewById<Button>(R.id.noContentButton)
                noContentText?.text = tip
                noContentButton?.text = buttonText
                noContentButton?.setOnClickListener(listener)
            }
        }
    }

    /**
     * 将load error view进行隐藏。
     */
    protected fun hideLoadErrorView() {
        loadErrorView?.visibility = View.GONE
    }

    /**
     * 将no content view进行隐藏。
     */
    protected fun hideNoContentView() {
        noContentView?.visibility = View.GONE
    }

    /**
     * 将bad network view进行隐藏。
     */
    protected fun hideBadNetworkView() {
        badNetworkView?.visibility = View.GONE
    }

    /**
     * 检查和处理运行时权限，并将用户授权的结果通过PermissionListener进行回调。
     *
     * @param permissions
     * 要检查和处理的运行时权限数组
     * @param listener
     * 用于接收授权结果的监听器
     */
    protected fun handlePermissions(permissions: Array<String>?, listener: PermissionListener) {
        if (permissions == null || activity == null) {
            return
        }
        mListener = listener
        val requestPermissionList = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionList.add(permission)
            }
        }
        if (!requestPermissionList.isEmpty()) {
            requestPermissions(requestPermissionList.toTypedArray(), 1)
        } else {
            listener.onGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty()) {
                val deniedPermissions = ArrayList<String>()
                for (i in grantResults.indices) {
                    val grantResult = grantResults[i]
                    val permission = permissions[i]
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permission)
                    }
                }
                if (deniedPermissions.isEmpty()) {
                    mListener?.onGranted()
                } else {
                    mListener?.onDenied(deniedPermissions)
                }
            }
            else -> {
            }
        }
    }

    /**
     * 在Fragment基类中获取通用的控件，会将传入的View实例原封不动返回。
     * @param view
     * Fragment中inflate出来的View实例。
     * @return  Fragment中inflate出来的View实例原封不动返回。
     */
    fun onCreateView(view: View): View {
        rootView = view
        loading = view.findViewById(R.id.loading)
        return view
    }

    /**
     * 开始加载，将加载等待控件显示。
     */
    @CallSuper
    override fun startLoading() {
        loading?.visibility = View.VISIBLE
        hideBadNetworkView()
        hideNoContentView()
        hideLoadErrorView()
    }

    /**
     * 加载完成，将加载等待控件隐藏。
     */
    @CallSuper
    override fun loadFinished() {
        loading?.visibility = View.GONE
    }

    /**
     * 加载失败，将加载等待控件隐藏。
     */
    @CallSuper
    override fun loadFailed(msg: String?) {
        loading?.visibility = View.GONE
    }
}
