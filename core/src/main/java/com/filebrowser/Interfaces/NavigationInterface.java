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
package com.filebrowser.Interfaces;

import java.io.File;

/**
 * Interface that provides callbacks for navigation events, such as
 * browsing into a new directory, opening a file, etc.
 *
 * @author Saravan Pantham
 */
public interface NavigationInterface {

    /**
     * Called when a new directory is opened by the user.
     *
     * @param dirFile The file that points to the new directory
     *                that has just been loaded.
     */
    public void onNewDirLoaded(File dirFile);

    /**
     * Called when a new file is opened by the user.
     *
     * @param file The file that was just opened.
     */
    public void onFileOpened(File file);

    /**
     * Called when the user navigates back to the parent directory.
     *
     * @param dirFile The file that points to the parent directory that
     *                was just opened.
     */
    public void onParentDirLoaded(File dirFile);

    /**
     * Called when a file or folder fails to open.
     *
     * @param file The file/folder that failed to open.
     */
    public void onFileFolderOpenFailed(File file);

    /**
     * Called when a file or folder fails to open.
     *
     * @param imagePath The file/folder that failed to open.
     */
    public void getImagePath(String imagePath);

}
