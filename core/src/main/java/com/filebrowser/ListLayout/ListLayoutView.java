/*
 * Copyright (C) 2014 Saravan Pantham
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
package com.filebrowser.ListLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.filebrowser.FileBrowserEngine.AdapterData;
import com.filebrowser.View.BaseLayoutView;
import com.filebrowser.View.FileBrowserView;
import com.quxianggif.core.R;
import com.quxianggif.core.extension.GlobalKt;
import com.quxianggif.core.util.GlobalUtil;

import java.io.File;


/**
 * List layout view implementation for the file browser.
 *
 * @author Saravan Pantham
 */
public class ListLayoutView extends BaseLayoutView {

    //Context.
    private Context mContext;

    //Parent FileBrowserView and its children.
    private FileBrowserView mFileBrowserView;

    private static File oldFile = null;

    public ListLayoutView(Context context, AttributeSet attributeSet, FileBrowserView fileBrowserView) {
        super(context, attributeSet);
        mContext = context;
        mFileBrowserView = fileBrowserView;

    }

    /**
     * Inflates the layout and sets the list's adapter.
     *
     * @param viewGroup The ViewGroup to inflate the layout into.
     * @return A reference to this view's instance.
     */
    public ListLayoutView init(ViewGroup viewGroup) {
        //Inflate the view from the XML resource.
        View.inflate(mContext, R.layout.simple_list_file_browser, viewGroup);
        mAbsListView = viewGroup.findViewById(R.id.file_browser_list_view);
        oldFile = mFileBrowserView.getDefaultDirectory();
        //Display the default dir.

        showDir(mFileBrowserView.getDefaultDirectory());

        return this;
    }

    /**
     * Loads the directory structure of the specified dir and sets the ListView's adapter.
     *
     * @param directory The File object that points to the directory to load.
     */
    @Override
    public void showDir(File directory) {
        if (directory != null) {
            //Grab the directory's data to feed to the list adapter.
            AdapterData adapterData = mFileBrowserView.getFileBrowserEngine().loadDir(directory);
            if (adapterData != null) {
                oldFile = directory;
                //Call the interface callback method.
                if (mNavigationInterface != null)
                    mNavigationInterface.onNewDirLoaded(directory);
                //Check if the user wants to use a custom adapter.
                if (mFileBrowserView.getFileBrowserAdapter() != null) {
                    //The user called setFileBrowserAdapter() and is using a custom adapter.
                    mFileBrowserView.getFileBrowserAdapter().setAdapterData(adapterData);
                } else {
                    //Nope, no custom adapter, so fall back to the default adapter.
                    ListLayoutAdapter adapter = new ListLayoutAdapter(mContext, mFileBrowserView, adapterData);
                    mFileBrowserView.setCustomAdapter(adapter);
                }
                //Apply the adapter to the ListView.
                mAbsListView.setAdapter(mFileBrowserView.getFileBrowserAdapter());
                //Apply the click listener to the ListView.
                mAbsListView.setOnItemClickListener(onItemClickListener);
            } else {
                //Call the interface callback method.
                if (mNavigationInterface != null)
                    mNavigationInterface.onNewDirLoaded(oldFile);
                if (namePath.contains(".gif") || namePath.contains(".GIF")) {
                    mNavigationInterface.getImagePath(namePath);
                } else {
                    GlobalKt.showToast(GlobalUtil.INSTANCE.getString(R.string.only_support_gif_file));

                }
            }
        } else {
            GlobalKt.showToast(GlobalUtil.INSTANCE.getString(R.string.unable_to_load_dir));
        }
    }

    public static String namePath = "null";

    /**
     * Click listener for the ListView.
     */
    private ListView.OnItemClickListener onItemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File file = null;
            try {
                String newPath = mFileBrowserView.getFileBrowserAdapter().getPathsList().get(position);
                namePath = newPath;
                file = new File(newPath);
                showDir(file);
            } catch (Exception e) {
                e.printStackTrace();
                if (mNavigationInterface != null)
                    mNavigationInterface.onFileFolderOpenFailed(file);
                //Display an error toast.
                if (file != null && file.isDirectory()) {
                    Toast.makeText(mContext, R.string.unable_to_load_dir, Toast.LENGTH_SHORT).show();
                } else if (file != null && !file.isDirectory()) {
                    Toast.makeText(mContext, R.string.unable_to_open_file, Toast.LENGTH_SHORT).show();
                }
            }
        }

    };
}