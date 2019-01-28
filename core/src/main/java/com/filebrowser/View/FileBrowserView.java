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
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.filebrowser.FileBrowserEngine.FileBrowserEngine;
import com.filebrowser.FileBrowserEngine.FileExtensionFilter;
import com.filebrowser.Interfaces.NavigationInterface;
import com.filebrowser.ListLayout.ListLayoutView;

import java.io.File;
import java.util.ArrayList;

/**
 * Base implementation class for FileBrowserView. Each FileBrowserView object is essentially a
 * ViewGroup that consists of other view children (an optional header view and an AbsListView).
 * This class is simply a container for the main view class, which is determined by the type of
 * any settings/preferences that the user requests for the view.
 *
 * @author Saravan Pantham
 */
public class FileBrowserView extends FrameLayout {

    //Context and AttributeSet.
    private Context mContext;

    private AttributeSet mAttributeSet;

    //Current layout type selection/view reference.
    private int mFileBrowserLayoutType = FILE_BROWSER_LIST_LAYOUT;

    private BaseLayoutView mFileBrowserLayout;

    //File browser engine.
    private FileBrowserEngine mFileBrowserEngine;

    //Adapter to use for the list/grid view.
    private AbstractFileBrowserAdapter mAdapter;

    //Default directory to display.
    private File mDefaultDir = null;

    //Flag to show/hide hidden files.
    private boolean mShowHiddenFiles = false;

    //Flags to display individual item attributes in the default adapter view.
    private boolean mShowOverflowMenus = true;

    private boolean mShowItemSizes = true;

    private boolean mShowIcons = true;

    //Layout type constants.
    public static final int FILE_BROWSER_LIST_LAYOUT = 0;

    public static final int FILE_BROWSER_GRID_LAYOUT = 1;

    //Navigation Interface.
    private NavigationInterface mNavigationInterface;

    //Whether or not tabbed browsing is enabled.
    private boolean mTabbedBrowsingEnabled = false;

    //FileExtensionFilter instance and whether or not to show other dirs in the current dir.
    private FileExtensionFilter mFileExtensionFilter;

    private boolean mShouldShowFolders = true;

    public FileBrowserView(Context context) {
        super(context);
        mContext = context;
        mFileExtensionFilter = new FileExtensionFilter();
    }

    public FileBrowserView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        mAttributeSet = attributeSet;
        mFileExtensionFilter = new FileExtensionFilter();
    }

    public FileBrowserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mFileExtensionFilter = new FileExtensionFilter();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(l, t, r, b);
        }

    }

    /**
     * Initializes the file browser view and fires it up for first use. Must be called to
     * properly display the file browser.
     */
    public void init() {
        //Check if the default dir was set.
        if (getDefaultDirectory() == null)
            setDefaultDirectory(Environment.getExternalStorageDirectory());
        //Initialize the file browser engine for this view instance.
        mFileBrowserEngine = new FileBrowserEngine(mContext, this);
        /*
         * If tabbed browsing is enabled, an instance of TabContainer will
         * become the direct child of this view. If not, we can directly
         * inflate the List/Grid layout views.
         */
        if (isTabbedBrowsingEnabled()) {
            mFileBrowserLayout = new TabsContainer(mContext, mAttributeSet, this).init(this);
        } else {
            //Inflate the view's layout based on the selected layout.
            if (getFileBrowserLayoutType() == FILE_BROWSER_LIST_LAYOUT)
                mFileBrowserLayout = new ListLayoutView(mContext, mAttributeSet, this).init(this);
        }
        //Apply the navigation interface.
        mFileBrowserLayout.setNavigationInterface(mNavigationInterface);

    }

    /**
     * Sets the default directory to show when the FileBrowserView is initialized.
     *
     * @param directory The file that points to the default directory to display.
     * @return An instance of this FileBrowserView to allow method chaining.
     * @throws IllegalArgumentException Thrown if the input File argument doesn't
     *                                  point to a valid directory or the directory can't be read.
     */
    public FileBrowserView setDefaultDirectory(File directory) throws IllegalArgumentException {
        if (directory == null || !directory.isDirectory())
            throw new IllegalArgumentException("You must use a File object that points to a valid, " +
                    "accessible directory.");
        if (!directory.canRead())
            throw new IllegalArgumentException("Could not read the specified default directory. Make " +
                    "sure you have permission to read the directory.");
        mDefaultDir = directory;
        return this;
    }

    /**
     * Sets the layout type (list or grid) of this FileBrowserView instance.
     *
     * @param layoutType Use one of the following two options: {@link #FILE_BROWSER_LIST_LAYOUT} or
     *                   {@link #FILE_BROWSER_GRID_LAYOUT}.
     * @return An instance of this FileBrowserView to allow method chaining.
     */
    public FileBrowserView setFileBrowserLayoutType(int layoutType) {
        mFileBrowserLayoutType = layoutType;
        return this;
    }

    /**
     * Call this method to use your own adapter for the list/grid view. The adapter must be a
     * for an example of a GridView adapter.
     *
     * @param adapter An adapter that is extended from {@link AbstractFileBrowserAdapter}.
     * @return An instance of this FileBrowserView to allow method chaining.
     * @throws IllegalArgumentException Thrown if the adapter passed in is not an instance
     */
    public FileBrowserView setCustomAdapter(AbstractFileBrowserAdapter adapter)
            throws IllegalArgumentException {
        if (!(adapter instanceof AbstractFileBrowserAdapter))
            throw new IllegalArgumentException("The adapter you pass into setCustomAdapter() " +
                    "must extend AbstractFileBrowserAdapter.");
        mAdapter = adapter;
        return this;
    }

    /**
     * Sets whether hidden files should be shown or not.
     *
     * @param show Specifies whether hidden files should be shown or not.
     * @return An instance of this FileBrowserView to allow method chaining.
     */
    public FileBrowserView setShowHiddenFiles(boolean show) {
        mShowHiddenFiles = show;
        return this;
    }

    /**
     * Sets whether or not the overflow menu should be shown or not (defaults to true). Note that
     * this method will have no effect if you use your own adapter via
     * {@link #setCustomAdapter(AbstractFileBrowserAdapter)}.
     *
     * @param show Whether or not the overflow menu should be shown.
     * @return An instance of this FileBrowserView to allow method chaining.
     */
    public FileBrowserView showOverflowMenus(boolean show) {
        mShowOverflowMenus = show;
        return this;
    }

    /**
     * Sets whether or not each file/folder's size should be displayed underneath the name (defaults
     * to true). Note that this method will have no effect if you use your own adapter via
     * {@link #setCustomAdapter(AbstractFileBrowserAdapter)}.
     * <p>
     * If the item is a folder, the number of subfiles/subfolders will be displayed in the
     * following format: xxx items.
     * <p>
     * If the item is a file, the size of the file will be displayed in the most appropriate
     * units: xxx KB, xxx bytes, xxx MB, etc.
     *
     * @param show Whether or not the item sizes should be shown.
     * @return An instance of this FileBrowserView to allow method chaining.
     */
    public FileBrowserView showItemSizes(boolean show) {
        mShowItemSizes = show;
        return this;
    }

    /**
     * Sets whether or not each file/folder's icon should be displayed next to the name (defaults
     * to true). Note that this method will have no effect if you use your own adapter via
     * {@link #setCustomAdapter(AbstractFileBrowserAdapter)}.
     *
     * @param show Whether or not the icon should be shown.
     * @return An instance of this FileBrowserView to allow method chaining.
     */
    public FileBrowserView showItemIcons(boolean show) {
        mShowIcons = show;
        return this;
    }

    /**
     * @param navInterface The navigation interface to assign to this view.
     * @return An instance of this FileBrowserView to allow method chaining
     */
    public FileBrowserView setNavigationInterface(NavigationInterface navInterface) {
        mNavigationInterface = navInterface;
        return this;
    }

    /**
     * Sets whether or not tabbed browsing should be enabled. If you pass true,
     * a TabHost will be placed above the FileBrowserView and will allow the user
     * to open new tabs to browse the filesystem (à la Google Chrome tab browsing).
     *
     * @param enable Whether or not tabbed browsing should be enabled.
     * @return An instance of this FileBrowserView to allow method chaining.
     */
    public FileBrowserView enableTabbedBrowsing(boolean enable) {
        mTabbedBrowsingEnabled = enable;
        return this;
    }

    /**
     * Prevents the browser from displaying files that match any of the file extensions that are
     * passed in via the param fileExtensions. Calling this method is optional; not calling it or
     * passing in an empty list will cause all files to be displayed regardless of their extensions.
     *
     * @param fileExtensions An ArrayList that contains string representations of the
     *                       extensions of the files to hide in the browser. The file
     *                       extension must be formatted like this: ".xxx", where xxx is
     *                       the file extension.
     * @param showFolders    Sets whether folders should be displayed in the view or not. Useful for
     *                       displaying a static view of the current folder with no way to navigate
     *                       away from it.
     * @return An instance of this FileBrowserView to allow method chaining.
     */
    public FileBrowserView excludeFileTypes(ArrayList<String> fileExtensions, boolean showFolders) {
        if (fileExtensions == null)
            return this;
        mFileExtensionFilter = new FileExtensionFilter();
        mShouldShowFolders = showFolders;
        for (int i = 0; i < fileExtensions.size(); i++) {
            mFileExtensionFilter.addExtension(fileExtensions.get(i));
        }
        return this;
    }

    public void setImagePath(String imagePath){

    }
    /**
     * @return BaseLayoutView.
     */
    public BaseLayoutView getBaseLayoutView() {
        return mFileBrowserLayout;
    }

    /**
     * @return The AttributeSet object associated with this view instance.
     */
    public AttributeSet getAttributeSet() {
        return mAttributeSet;
    }

    /**
     * @return The current layout type for this FileBrowserView instance.
     */
    public int getFileBrowserLayoutType() {
        return mFileBrowserLayoutType;
    }

    /**
     * @return A File object that points to the default directory that should be
     * displayed for this FileBrowserView instance.
     */
    public File getDefaultDirectory() {
        return mDefaultDir;
    }

    /**
     * @return Whether or not hidden files/folders should be displayed.
     */
    public boolean shouldShowHiddenFiles() {
        return mShowHiddenFiles;
    }

    /**
     * @return The file browser engine instance for this view.
     */
    public FileBrowserEngine getFileBrowserEngine() {
        return mFileBrowserEngine;
    }

    /**
     * @return The File object that represents the current directory.
     */
    public File getCurrentDir() {
        return mFileBrowserEngine.getCurrentDir();
    }

    /**
     * @return The File object that represents the current directory's parent dir.
     * Returns null if the current directory doesn't have a parent dir.
     */
    public File getParentDir() {
        return getCurrentDir().getParentFile();
    }

    /**
     * @return The adapter that backs the list/grid view for FileBrowserView instance.
     */
    public AbstractFileBrowserAdapter getFileBrowserAdapter() {
        return mAdapter;
    }

    /**
     * @return The list/grid view that displays the file system. Note that this method
     * does not return the specific subclass (ListView/GridView). You must manually
     * cast the returned object as a ListView or a GridView to fully access each
     * view's functionality.
     */
    public AbsListView getAbsListView() {
        return mFileBrowserLayout.getAbsListView();
    }

    /**
     * @return Whether or not each individual item's overflow menu should be displayed in the
     * AbsListView. The returned value has no effect if you are using a custom adapter
     * via {@link #setCustomAdapter(AbstractFileBrowserAdapter)}.
     */
    public boolean shouldShowOverflowMenus() {
        return mShowOverflowMenus;
    }

    /**
     * @return Whether or not each individual item's size should be displayed in the AbsListView.
     * The returned value has no effect if you are using a custom adapter via
     * {@link #setCustomAdapter(AbstractFileBrowserAdapter)}.
     */
    public boolean shouldShowItemSizes() {
        return mShowItemSizes;
    }

    /**
     * @return Whether or not each individual item's icon should be displayed in the AbsListView.
     * The returned value has no effect if you are using a custom adapter via
     * {@link #setCustomAdapter(AbstractFileBrowserAdapter)}.
     */
    public boolean shouldShowItemIcons() {
        return mShowIcons;

    }

    /**
     * @return Whether tabbed browsing is enabled or not for this FileBrowserView instance.
     */
    public boolean isTabbedBrowsingEnabled() {
        return mTabbedBrowsingEnabled;
    }

    /**
     * @return The FileExtensionFilter extension that is currently in use for this FileBrowserView.
     */
    public FileExtensionFilter getFileExtensionFilter() {
        return mFileExtensionFilter;
    }

    /**
     * @return Whether or not folders should be shown in the browser.
     */
    public boolean shouldShowFolders() {
        return mShouldShowFolders;
    }

}
