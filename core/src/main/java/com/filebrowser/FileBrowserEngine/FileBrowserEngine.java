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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.filebrowser.View.FileBrowserView;
import com.quxianggif.core.R;

import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Convenience class that exposes the Android file system and provides a
 * way to browse it via {@link FileBrowserView}.
 *
 * @author Saravan Pantham
 */
public class FileBrowserEngine {
    //Context.
    private Context mContext;

    //Parent FileBrowserView instance.
    private FileBrowserView mFileBrowserView;

    //Current dir instance.
    private File mCurrentDir;

    //Current dir instance.
    private File nowCurrentDir;

    //File type constants.
    public static final int FILE_AUDIO = 0;

    public static final int FILE_VIDEO = 1;

    public static final int FILE_PICTURE = 2;

    public static final int FILE_GENERIC = 3;
    public static final int FOLDER = 4;

    //File size/unit dividers
    private final long KILOBYTES = 1024;

    private final long MEGABYTES = KILOBYTES * KILOBYTES;

    private final long GIGABYTES = MEGABYTES * KILOBYTES;

    private final long TERABYTES = GIGABYTES * KILOBYTES;

    public FileBrowserEngine(Context context, FileBrowserView fileBrowserView) {
        mContext = context;
        mFileBrowserView = fileBrowserView;
    }

    /**
     * Loads the specified folder.
     *
     * @param directory The file object to points to the directory to load.
     * @return An {@link AdapterData} object that holds the data of the specified directory.
     */
    public AdapterData loadDir(File directory) {
        if (mCurrentDir != null) {
            nowCurrentDir = mCurrentDir;
        }
        mCurrentDir = directory;
        //Init the directory's data arrays.
        ArrayList<String> namesList = new ArrayList<String>();
        ArrayList<String> pathsList = new ArrayList<String>();
        ArrayList<Integer> typesList = new ArrayList<Integer>();
        ArrayList<String> sizesList = new ArrayList<String>();

        //这里是文件夹的list
        ArrayList<String> folderNamesList = new ArrayList<String>();
        ArrayList<String> folderPathsList = new ArrayList<String>();
        ArrayList<Integer> folderTypesList = new ArrayList<Integer>();
        ArrayList<String> folderSizesList = new ArrayList<String>();

        //这里是文件的list
        ArrayList<String> fileNamesList = new ArrayList<String>();
        ArrayList<String> filePathsList = new ArrayList<String>();
        ArrayList<Integer> fileTypesList = new ArrayList<Integer>();
        ArrayList<String> fileSizesList = new ArrayList<String>();

        //Grab a list of all files/subdirs within the specified directory.
        File[] files = directory.listFiles();
        if (files != null) {
            //Sort the files/subdirs by name.
            Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
            for (File file : files) {
                if (file.isHidden()) {
                    continue;
                }
                if ((mFileBrowserView.shouldShowHiddenFiles()) && file.canRead()) {
                    if (file.isDirectory() && mFileBrowserView.shouldShowFolders()) {
                        /*
                         * Starting with Android 4.2, /storage/emulated/legacy/...
                         * is a symlink that points to the actual directory where
                         * the user's files are stored. We need to detect the
                         * actual directory's file path here.
                         */
                        String filePath;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                            filePath = getRealFilePath(file.getAbsolutePath());
                        else
                            filePath = file.getAbsolutePath();
                        folderPathsList.add(filePath);
                        folderNamesList.add(file.getName());
                        File[] listOfFiles = file.listFiles();
                        if (listOfFiles != null) {
                            folderTypesList.add(FOLDER);
                            if (listOfFiles.length == 1) {
                                if (listOfFiles[0].isHidden()) {
                                    folderSizesList.add("0" + mContext.getResources().getString(R.string.folder_size));
                                } else {
                                    folderSizesList.add("" + listOfFiles.length + mContext.getResources().getString(R.string.folder_size));
                                }
                            } else {
                                int listSize = 0;
                                for (File listOfFile : listOfFiles) {
                                    if (listOfFile.isHidden()) {
                                        continue;
                                    }
                                    listSize = listSize + 1;
                                }
                                if (listSize > 0) {
                                    folderSizesList.add("" + listSize + mContext.getResources().getString(R.string.folder_size));
                                } else {
                                    folderSizesList.add("0" + mContext.getResources().getString(R.string.folder_size));
                                }
                            }
                        } else {
                            folderTypesList.add(FOLDER);
                            folderSizesList.add("0" + mContext.getResources().getString(R.string.folder_size));
                        }
                    } else {
                        try {
                            String path = file.getCanonicalPath();
                            if (mFileBrowserView != null) {
                                //Check if the file ends with an excluded extension.
                                String[] splits = path.split(".");
                                if (splits.length > 1) {
                                    if (mFileBrowserView.getFileExtensionFilter()
                                            .getFilterMap()
                                            .containsKey("." + splits[splits.length - 1]))
                                        continue;
                                }
                            }
                            filePathsList.add(path);
                        } catch (IOException e) {
                            continue;
                        }
                        fileNamesList.add(file.getName());
                        String fileName = "";
                        try {
                            fileName = file.getCanonicalPath();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        //Add the file element to typesList based on the file type.
                        if (getFileExtension(fileName).equalsIgnoreCase("mp3") ||
                                getFileExtension(fileName).equalsIgnoreCase("3gp") ||
                                getFileExtension(fileName).equalsIgnoreCase("mp4") ||
                                getFileExtension(fileName).equalsIgnoreCase("m4a") ||
                                getFileExtension(fileName).equalsIgnoreCase("aac") ||
                                getFileExtension(fileName).equalsIgnoreCase("ts") ||
                                getFileExtension(fileName).equalsIgnoreCase("flac") ||
                                getFileExtension(fileName).equalsIgnoreCase("mid") ||
                                getFileExtension(fileName).equalsIgnoreCase("xmf") ||
                                getFileExtension(fileName).equalsIgnoreCase("mxmf") ||
                                getFileExtension(fileName).equalsIgnoreCase("midi") ||
                                getFileExtension(fileName).equalsIgnoreCase("rtttl") ||
                                getFileExtension(fileName).equalsIgnoreCase("rtx") ||
                                getFileExtension(fileName).equalsIgnoreCase("ota") ||
                                getFileExtension(fileName).equalsIgnoreCase("imy") ||
                                getFileExtension(fileName).equalsIgnoreCase("ogg") ||
                                getFileExtension(fileName).equalsIgnoreCase("mkv") ||
                                getFileExtension(fileName).equalsIgnoreCase("wav")) {
                            //The file is an audio file.
                            fileTypesList.add(FILE_AUDIO);
                            fileSizesList.add("" + getFormattedFileSize(file.length()));
                        } else if (getFileExtension(fileName).equalsIgnoreCase("jpg") ||
                                getFileExtension(fileName).equalsIgnoreCase("gif") ||
                                getFileExtension(fileName).equalsIgnoreCase("png") ||
                                getFileExtension(fileName).equalsIgnoreCase("bmp") ||
                                getFileExtension(fileName).equalsIgnoreCase("webp")) {
                            //The file is a picture file.
                            fileTypesList.add(FILE_PICTURE);
                            fileSizesList.add("" + getFormattedFileSize(file.length()));
                        } else if (getFileExtension(fileName).equalsIgnoreCase("3gp") ||
                                getFileExtension(fileName).equalsIgnoreCase("mp4") ||
                                getFileExtension(fileName).equalsIgnoreCase("3gp") ||
                                getFileExtension(fileName).equalsIgnoreCase("ts") ||
                                getFileExtension(fileName).equalsIgnoreCase("webm") ||
                                getFileExtension(fileName).equalsIgnoreCase("mkv")) {
                            //The file is a video file.
                            fileTypesList.add(FILE_VIDEO);
                            fileSizesList.add("" + getFormattedFileSize(file.length()));
                        } else {
                            //We don't have an icon for this file type so give it the generic file flag.
                            fileTypesList.add(FILE_GENERIC);
                            fileSizesList.add("" + getFormattedFileSize(file.length()));
                        }
                    }
                }
            }
        }
        namesList.addAll(folderNamesList);
        namesList.addAll(fileNamesList);
        pathsList.addAll(folderPathsList);
        pathsList.addAll(filePathsList);
        typesList.addAll(folderTypesList);
        typesList.addAll(fileTypesList);
        sizesList.addAll(folderSizesList);
        sizesList.addAll(fileSizesList);
        if (namesList.size() == 0 && typesList.size() == 0 && pathsList.size() == 0 && sizesList.size() == 0) {
            mCurrentDir = nowCurrentDir;
            return null;
        } else {
            return new AdapterData(namesList, typesList, pathsList, sizesList);
        }
    }

    /**
     * Resolves the /storage/emulated/legacy paths to
     * their true folder path representations. Required
     * for Nexii and other devices with no SD card.
     *
     * @return The true, resolved file path to the input path.
     */
    @SuppressLint("SdCardPath")
    private String getRealFilePath(String filePath) {
        if (filePath.equals("/storage/emulated/0") ||
                filePath.equals("/storage/emulated/0/") ||
                filePath.equals("/storage/emulated/legacy") ||
                filePath.equals("/storage/emulated/legacy/") ||
                filePath.equals("/storage/sdcard0") ||
                filePath.equals("/storage/sdcard0/") ||
                filePath.equals("/sdcard") ||
                filePath.equals("/sdcard/") ||
                filePath.equals("/mnt/sdcard") ||
                filePath.equals("/mnt/sdcard/")) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return filePath;
    }

    /**
     * Resolves the file extension for the specified file.
     *
     * @param fileName The name of the file (including its extension).
     * @return The extension of the file (excluding the dot ".").
     */
    private String getFileExtension(String fileName) {
        String fileNameArray[] = fileName.split("\\.");
        return fileNameArray[fileNameArray.length - 1];
    }

    /**
     * Formats the input value in terms of file size.
     *
     * @param value The value to convert to a file size.
     * @return The formatted size of the input file.
     */
    private String getFormattedFileSize(final long value) {
        final long[] dividers = new long[]{TERABYTES, GIGABYTES, MEGABYTES, KILOBYTES, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "bytes"};
        if (value < 1) {
            return "";
        }
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = formatFileSizeString(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    /**
     * Formats the input value as a file size string.
     *
     * @param value   The value to format.
     * @param divider The value to divide the input value by.
     * @param unit    The output units.
     * @return The formatted file size string.
     */
    private String formatFileSizeString(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }

    public File getCurrentDir() {
        return mCurrentDir;
    }

}
