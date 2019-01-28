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
package com.filebrowser.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import com.filebrowser.ListLayout.ListLayoutView;
import com.quxianggif.core.R;

import java.io.File;

/**
 * Container view for tabbed browsing. Includes the TabHost,
 * TabWidget, and the actual tab content. If tabbed browsing
 * is enabled, this view becomes the direct child of the
 * parent FileBrowserView instance. Each tab will then host
 *
 * @author Saravan Pantham
 */
public class TabsContainer extends View {

    private Context mContext;

    private FileBrowserView mFileBrowserView;

    protected TabHost mTabHost;

    protected TabWidget mTabWidget;

    protected ImageView mNewTabButton;

    protected FrameLayout mTabContentLayout;

    public TabsContainer(Context context, FileBrowserView fileBrowserView) {
        super(context);
        mContext = context;
        mFileBrowserView = fileBrowserView;
    }

    public TabsContainer(Context context, AttributeSet attrs, FileBrowserView fileBrowserView) {
        super(context, attrs);
        mContext = context;
        mFileBrowserView = fileBrowserView;
    }

    /**
     * Initializes this view instance and opens the default tab/directory structure. Also
     * attaches a "New Tab" button to the TabWidget.
     *
     * @param viewGroup The ViewGroup to inflate the layout into.
     */
    public BaseLayoutView init(ViewGroup viewGroup) {
        //Initialize the tabbed container.
        View view = View.inflate(mContext, R.layout.tabbed_browser_container, viewGroup);
        mTabHost = view.findViewById(R.id.tabHost);
        mTabWidget = view.findViewById(android.R.id.tabs);
        mNewTabButton = view.findViewById(R.id.new_tab_button);
        mTabContentLayout = view.findViewById(android.R.id.tabcontent);
        mNewTabButton.setOnClickListener(newTabClickListener);
        //Initialize the TabHost.
        mTabHost.setup();
        //Open the default tab.
        return openNewBrowserTab(mFileBrowserView.getDefaultDirectory());
    }

    /**
     * Opens a brand new browser tab.
     *
     * @param directory The directory to open when this tab is initialized.
     */
    protected BaseLayoutView openNewBrowserTab(File directory) {
        //Inflate the view's layout based on the selected layout.
        BaseLayoutView contentView = null;
        if (mFileBrowserView.getFileBrowserLayoutType() == FileBrowserView.FILE_BROWSER_LIST_LAYOUT)
            contentView = new ListLayoutView(mContext, mFileBrowserView.getAttributeSet(), mFileBrowserView).init(mTabContentLayout);
        contentView.setId(mTabHost.getTabWidget().getTabCount() + 1);
        mTabContentLayout.addView(contentView);
        //Add the new tab to the TabHost.
        String directoryName = directory.getAbsoluteFile().getName();
        TabHost.TabSpec newTabSpec = mTabHost.newTabSpec(directoryName);
        newTabSpec.setIndicator(directoryName);
        newTabSpec.setContent(mTabHost.getTabWidget().getTabCount() + 1);
        mTabHost.addTab(newTabSpec);
        return contentView;
    }

    /**
     * Click listener for the "New Tab" button.
     */
    private OnClickListener newTabClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            openNewBrowserTab(mFileBrowserView.getDefaultDirectory());
        }

    };
}
