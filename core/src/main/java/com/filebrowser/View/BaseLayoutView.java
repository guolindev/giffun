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
import android.widget.AbsListView;

import com.filebrowser.Interfaces.NavigationInterface;

import java.io.File;

/**
 * @author Saravan Pantham
 */
public abstract class BaseLayoutView extends View {

    /**
     * Context intstance.
     */
    protected Context mContext;

    /**
     * The ListView/GridView that displays the file system.
     */
    protected AbsListView mAbsListView;

    /**
     * The interface instance that provides callbacks for filesystem
     * navigation events.
     */
    protected NavigationInterface mNavigationInterface;

    /**
     * Default constructor.
     */
    public BaseLayoutView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
    }

    /**
     * Override this method to implement your logic for loading a directory structure of the
     * specified dir and to set your AbsListView's adapter.
     *
     * @param directory The File object that points to the directory to load.
     */
    public abstract void showDir(File directory);

    /**
     * Sets the navigation interface instance for this view.
     *
     * @param navInterface The interface instance to assign to this view.
     */
    public void setNavigationInterface(NavigationInterface navInterface) {
        mNavigationInterface = navInterface;
    }

    /**
     * @return The ListView/GridView that displays the file system.
     */
    public AbsListView getAbsListView() {
        return mAbsListView;
    }

}
