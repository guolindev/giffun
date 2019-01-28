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
package com.filebrowser.FileBrowserEngine;

import java.util.ArrayList;

/**
 * Container class for the file browser's adapter to use.
 *
 * @author Saravan Pantham
 */
public class AdapterData {

    private ArrayList<String> mNamesList;

    private ArrayList<Integer> mTypesList;

    private ArrayList<String> mPathsList;

    private ArrayList<String> mSizesList;

    public AdapterData(ArrayList<String> namesList, ArrayList<Integer> typesList,
                       ArrayList<String> pathsList, ArrayList<String> sizesList) {
        mNamesList = namesList;
        mTypesList = typesList;
        mPathsList = pathsList;
        mSizesList = sizesList;
    }

    public ArrayList<String> getNamesList() {
        return mNamesList;
    }

    public void setNamesList(ArrayList<String> namesList) {
        mNamesList = namesList;
    }

    public ArrayList<Integer> getTypesList() {
        return mTypesList;
    }

    public void setTypesList(ArrayList<Integer> typesList) {
        mTypesList = typesList;
    }

    public ArrayList<String> getPathsList() {
        return mPathsList;
    }

    public void setPathsList(ArrayList<String> pathsList) {
        mPathsList = pathsList;
    }

    public ArrayList<String> getSizesList() {
        return mSizesList;
    }

    public void setSizesList(ArrayList<String> sizesList) {
        mSizesList = sizesList;
    }
}
