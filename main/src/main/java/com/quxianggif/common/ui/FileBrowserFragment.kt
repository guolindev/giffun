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

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.HorizontalScrollView

import com.filebrowser.Interfaces.NavigationInterface
import com.filebrowser.View.FileBrowserView
import com.quxianggif.R
import com.quxianggif.core.GifFun
import com.quxianggif.feeds.ui.SelectGifActivity
import kotlinx.android.synthetic.main.activity_select_gif.*

import java.io.File

class FileBrowserFragment : BaseFragment(), AdapterView.OnItemClickListener {

    private lateinit var activity: SelectGifActivity
    lateinit var fileBrowserView: FileBrowserView

    /**
     * Navigation interface for the view. Used to capture events such as a new
     * directory being loaded, files being opened, etc. For our purposes here,
     * we'll be using the onNewDirLoaded() method to update the ActionBar's title
     * with the current directory's path.
     */
    private val navInterface = object : NavigationInterface {

        override fun onNewDirLoaded(dirFile: File) {
            //Update the action bar title.
            // getActionBar().setTitle(dirFile.getAbsolutePath());
            setCurrentPathToTitle(dirFile.absolutePath)
        }

        override fun onFileOpened(file: File) {

        }

        override fun onParentDirLoaded(dirFile: File) {

        }

        override fun onFileFolderOpenFailed(file: File) {

        }

        override fun getImagePath(imagePath: String) {
            activity.setImagePath(imagePath)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_file_browser, container, false)
        //Grab a reference handle on the view, just like you'd do with any other view.
        fileBrowserView = view.findViewById(R.id.fileBrowserView)
        activity = getActivity() as SelectGifActivity
        //File path = Environment.getExternalStorageDirectory();
        //  getActionBar().setTitle(path.toString());
        //Customize the view.
        fileBrowserView.setFileBrowserLayoutType(FileBrowserView.FILE_BROWSER_LIST_LAYOUT) //Set the type of view to use.
                .setDefaultDirectory(File(Environment.getExternalStorageDirectory().path)) //Set the default directory to show.
                .setShowHiddenFiles(true) //Set whether or not you want to show hidden files.
                .showItemSizes(true) //Shows the sizes of each item in the list.
                .showOverflowMenus(true) //Shows the overflow menus for each item in the list.
                .showItemIcons(true) //Shows the icons next to each item name in the list.
                .setNavigationInterface(navInterface) //Sets the nav interface instance for this view.
                .init() //Loads the view. You MUST call this method, or the view will not be displayed.

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (getActivity() is SelectGifActivity) {
            activity = getActivity() as SelectGifActivity
            activity.currentFragment = this
            activity.rootPath = Environment.getExternalStorageDirectory().path
            activity.currentPath = activity.rootPath
            activity.toolbarTitleText.text = "sdcard"
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    private fun setCurrentPathToTitle(path: String) {
        var sdcardPath = path
        sdcardPath = sdcardPath.replace(activity.rootPath, "sdcard")
        activity.toolbarTitleText.text = sdcardPath
        GifFun.getHandler().post { activity.toolbarScrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT) }
    }

    companion object {

        private const val TAG = "FileBrowserFragment"
    }

}