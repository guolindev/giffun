/*
 * Copyright (C) guolin, Suzhou Quxiang Inc. Open source codes for study only.
 * Do not use for commercial purpose.
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

package com.quxianggif.util.glide

import android.graphics.Bitmap
import android.graphics.drawable.TransitionDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.OriginalKey
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.signature.EmptySignature
import com.quxianggif.core.GifFun
import com.quxianggif.core.extension.logDebug
import com.quxianggif.core.extension.logError
import com.quxianggif.core.extension.logWarn
import com.quxianggif.util.FileUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Glide的工具类。
 *
 * @author guolin
 * @since 17/3/30
 */
object GlideUtil {

    private val TAG = "GlideUtil"

    /**
     * 获取Glide的图片缓存目录。
     * @return Glide的图片缓存目录。
     */
    val cacheDir: File?
        get() {
            val factory = Glide.get(GifFun.getContext()).diskCacheFactory
            return factory?.cacheDir
        }

    /**
     * 获取Glide当前的缓存大小，单位是字节。
     * @return Glide当前的缓存大小。
     */
    val cacheSize: Long
        get() {
            try {
                val cacheDir = cacheDir
                if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory) {
                    return 0
                }
                val diskLruCacheWrapper = DiskLruCacheWrapper.get(cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE) as DiskLruCacheWrapper
                val diskLruCache = diskLruCacheWrapper.diskCache
                return diskLruCache.size()
            } catch (e: IOException) {
                logWarn(TAG, e.message, e)
            }

            return 0
        }

    /**
     * 获取ImageView当中的GifDrawable对象，如果获取失败则返回null。
     * @param imageView
     * 包含GIF图片的ImageView
     * @return ImageView当中的GifDrawable对象。
     */
    fun getGifDrawable(imageView: ImageView): GifDrawable? {
        val drawable = imageView.drawable ?: return null
        // 获取到图片并检查是否是GIF图
        var gifDrawable: GifDrawable? = null
        if (drawable is GifDrawable) {
            gifDrawable = drawable
        } else if (drawable is TransitionDrawable) {
            for (i in 0 until drawable.numberOfLayers) {
                if (drawable.getDrawable(i) is GifDrawable) {
                    gifDrawable = drawable.getDrawable(i) as GifDrawable
                    break
                }
            }
        }
        return gifDrawable
    }

    /**
     * 删除指定图片地址的Glide缓存。
     * @param imageUrl
     * 图片的URL地址。
     * @return 删除成功返回true，否则返回false。
     */
    fun removeImageCache(imageUrl: String): Boolean {
        try {
            val cacheDir = cacheDir
            if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory) {
                return false
            }
            val diskLruCacheWrapper = DiskLruCacheWrapper.get(cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE) as DiskLruCacheWrapper
            val diskLruCache = diskLruCacheWrapper.diskCache
            val keyUrl = CustomUrl(imageUrl).cacheKey // 获取实际缓存的URL，不受URL中带有不同参数的影响
            return diskLruCache.remove(getCachedKey(keyUrl))
        } catch (e: Exception) {
            logError(TAG, e.message, e)
        }

        return false
    }

    /**
     * 将指定路径下的图片保存到Glide缓存当中。
     * @param imageSourcePath
     * 图片的源路径。
     * @param imageUrl
     * 图片的URL（用作缓存Key）。
     * @return 保存成功返回true，否则返回false。
     */
    fun saveImagePathToCache(imageSourcePath: String, imageUrl: String): Boolean {
        try {
            val imageSourceFile = File(imageSourcePath)
            if (!imageSourceFile.exists() || imageSourceFile.isDirectory) {
                return false
            }
            val cacheDir = cacheDir
            if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory) {
                return false
            }
            val diskLruCacheWrapper = DiskLruCacheWrapper.get(cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE) as DiskLruCacheWrapper
            val diskLruCache = diskLruCacheWrapper.diskCache
            val editor = diskLruCache.edit(getCachedKey(imageUrl))
            if (editor != null) {
                try {
                    val file = editor.getFile(0)
                    logDebug(TAG, "saveImagePathToCache file is $file")
                    if (FileUtil.copyFile(imageSourceFile, file)) {
                        editor.commit()
                        return true
                    }
                } finally {
                    editor.abortUnlessCommitted()
                }
            }
        } catch (e: Exception) {
            logError(TAG, e.message, e)
        }
        return false
    }

    /**
     * 将传入的Bitmap对象保存到Glide缓存当中。
     * @param bitmap
     * 要缓存的Bitmap对象。
     * @param imageUrl
     * 图片的URL（用作缓存Key）。
     * @return 保存成功返回true，否则返回false。
     */
    fun saveBitmapToCache(bitmap: Bitmap, imageUrl: String): Boolean {
        try {
            val cacheDir = cacheDir
            if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory) {
                return false
            }
            val diskLruCacheWrapper = DiskLruCacheWrapper.get(cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE) as DiskLruCacheWrapper
            val diskLruCache = diskLruCacheWrapper.diskCache
            val editor = diskLruCache.edit(getCachedKey(imageUrl))
            if (editor != null) {
                var fos: FileOutputStream? = null
                try {
                    val file = editor.getFile(0)
                    logDebug(TAG, "saveBitmapToCache file is $file")
                    fos = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                    fos.flush()
                    editor.commit()
                    return true
                } finally {
                    editor.abortUnlessCommitted()
                    if (fos != null) {
                        fos.close()
                    }
                }
            }
        } catch (e: Exception) {
            logError(TAG, e.message, e)
        }

        return false
    }

    /**
     * 获取指定URL地址的图片的缓存File，注意此方法只对使用DiskCacheStrategy.SOURCE缓存策略的图片有效。
     * @param url
     *          图片的url地址。
     * @return 图片的缓存File或者null。
     */
    fun getCacheFile(url: String): File? {
        val cacheDir = cacheDir
        if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory) {
            return null
        }
        return File(cacheDir, getCachedKey(url)!! + ".0")
    }

    /**
     * 判断指定URL地址的图片是否已经缓存，注意此方法只对使用DiskCacheStrategy.SOURCE缓存策略的图片有效。
     * @param url
     * 图片的url地址。
     * @return 已缓存返回true，否则返回false。
     */
    fun isSourceCached(url: String): Boolean {
        return  getCacheFile(url)?.exists() ?: false
    }

    /**
     * 判断指定GlideUrl对象的图片是否已经缓存，注意此方法只对使用DiskCacheStrategy.SOURCE缓存策略的图片有效。
     * @param glideUrl
     * 包含图片url地址的GlideUrl对象。
     * @return 已缓存返回true，否则返回false。
     */
    fun isSourceCached(glideUrl: GlideUrl): Boolean {
        val cacheDir = cacheDir
        if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory) {
            return false
        }
        val cacheFile = File(cacheDir, getCachedKey(glideUrl)!! + ".0")
        return cacheFile.exists()
    }

    /**
     * 获取指定URL地址图片的缓存Key。
     * @param url
     * 图片的url地址。
     * @return 指定URL地址图片的缓存Key。
     */
    fun getCachedKey(url: String): String? {
        val originalKey = OriginalKey(url, EmptySignature.obtain())
        val safeKeyGenerator = SafeKeyGenerator.getInstance()
        return safeKeyGenerator.getSafeKey(originalKey)
    }

    /**
     * 获取指定GlideUrl对象图片的缓存Key。
     * @param glideUrl
     * 包含图片url地址的GlideUrl对象。
     * @return 指定GlideUrl对象图片的缓存Key。
     */
    fun getCachedKey(glideUrl: GlideUrl): String? {
        return getCachedKey(glideUrl.cacheKey)
    }

    /**
     * 清除Glide的所有缓存。
     */
    fun clearCache() {
        val cacheDir = cacheDir
        if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory) {
            return
        }
        val diskLruCacheWrapper = DiskLruCacheWrapper.get(cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE) as DiskLruCacheWrapper
        diskLruCacheWrapper.clear()
    }

}
