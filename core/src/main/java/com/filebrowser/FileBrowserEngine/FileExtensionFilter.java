package com.filebrowser.FileBrowserEngine;

import java.util.HashMap;

/**
 * Class to handle filtering files by their extension. Each extension
 * is stored in a HashMap (for fast searching) which is accessible via,
 * the getFilterMap() method.
 *
 * @author Saravan Pantham
 */
public class FileExtensionFilter {

    private HashMap<String, Boolean> mFilterMap;

    public FileExtensionFilter() {
        mFilterMap = new HashMap<String, Boolean>();
    }

    /**
     * Adds the specified extension to the HashMap. The extension MUST be
     * in the following format: ".xxx", where xxx is the actual file extension.
     * If the format doesn't match up, the extension will not be added to the
     * HashMap.
     *
     * @param extension The extension String to add to the HashMap.
     * @return True, if the extension was successfully added to the HashMap. False otherwise.
     */
    public boolean addExtension(String extension) {
        if (extension.startsWith("."))
            throw new IllegalArgumentException("Invalid file extension format. You must " +
                                               "start the extension with a period (.), " +
                                               "followed by the actual extension itself. " +
                                               "Exception thrown for the following extension: " +
                                                extension);
        return getFilterMap().put(extension, false);
    }

    /**
     * @return The HashMap that stores all the file extensions for this filter object.
     */
    public HashMap<String, Boolean> getFilterMap() {
        return mFilterMap;
    }

}
