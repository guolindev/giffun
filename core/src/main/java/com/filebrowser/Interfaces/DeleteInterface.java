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

/**
 * Interface that provides callbacks during specific checkpoints
 * during the delete operation.
 *
 * @author Saravan Pantham
 */
public interface DeleteInterface {

    /**
     * This method is called right before the delete operation begins.
     * Any code you implement into this method will run on the main
     * thread.
     */
    public void preDeleteStartSync();

    /**
     * This method is called right before the delete operation begins.
     * Any code you implement into this method will run on the
     * delete operation's AsyncTask thread.
     */
    public void preDeleteStartAsync();

    /**
     * This method is called right after the delete operation finishes.
     * Any code you implement into this method will run on the main
     * thread.
     *
     * @param result Whether or not the delete operation completed successfully.
     */
    public void onDeleteCompleteSync(boolean result);

    /**
     * This method is called right after the delete operation finishes.
     * Any code you implement into this method will run on the delete
     * operation's AsyncTask thread.
     *
     * @param result Whether or not the delete operation completed successfully.
     */
    public void onDeleteCompleteAsync(boolean result);

}
