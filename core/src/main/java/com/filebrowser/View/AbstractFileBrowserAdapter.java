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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


import com.filebrowser.FileBrowserEngine.AdapterData;

import java.util.ArrayList;

/**
 * Extend this abstract class if you want to create your own adapter for the ListView/GridView.
 *
 * @author Saravan Pantham
 */
public abstract class AbstractFileBrowserAdapter extends ArrayAdapter<String> {

    protected Context mContext;

    protected AdapterData mAdapterData;

    protected FileBrowserView mFileBrowserView;

    public AbstractFileBrowserAdapter(Context context, FileBrowserView fileBrowserView,
                                      ArrayList<String> namesList) {
        super(context, -1, namesList);
        mContext = context;
        mFileBrowserView = fileBrowserView;
    }

    @Override
    public int getCount() {
        return getNamesList().size();
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    /**
     * Called when the overflow menu button/icon is clicked. This is where you should implement
     * your code for displaying the overflow menu (or any other action that should be performed
     * when the overflow menu button is clicked). Note that this method has no effect if you
     * use your own custom adapter via
     * {@link FileBrowserView#setCustomAdapter(AbstractFileBrowserAdapter)}.
     *
     * @param overflowView The view that should be used as the overflow menu's anchor.
     */
    public abstract void onOverflowClick(View overflowView);

    /**
     * Updates the adapter with the specified {@link AdapterData} object. Must be called
     * BEFORE the adapter is first applied to the list/grid view.
     *
     * @param adapterData The {@link AdapterData} object to set to this adapter instance.
     */
    public void setAdapterData(AdapterData adapterData) {
        mAdapterData = adapterData;
    }

    /**
     * @return The list of file/subfolder names within the current directory.
     */
    public ArrayList<String> getNamesList() {
        if (mAdapterData != null)
            return mAdapterData.getNamesList();
        else
            return new ArrayList<String>();

    }

    /**
     * @return The list of file/subfolder types within the current directory.
     */
    public ArrayList<Integer> getTypesList() {
        if (mAdapterData != null)
            return mAdapterData.getTypesList();
        else
            return new ArrayList<Integer>();

    }

    /**
     * @return The list of file/subfolder paths within the current directory.
     */
    public ArrayList<String> getPathsList() {
        if (mAdapterData != null)
            return mAdapterData.getPathsList();
        else
            return new ArrayList<String>();

    }

    /**
     * @return The list of file/subfolder sizes within the current directory.
     */
    public ArrayList<String> getSizesList() {
        if (mAdapterData != null)
            return mAdapterData.getSizesList();
        else
            return new ArrayList<String>();

    }

}
