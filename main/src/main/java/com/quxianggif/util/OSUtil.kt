package com.quxianggif.util

import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

/**
 * 用于判断手机操作系统类型的工具类。
 *
 * @author guolin
 * @since 17/7/16
 */
object OSUtil {

    private val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
    private val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
    private val KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage"

    val isMiUI8OrLower: Boolean
        get() {
            try {
                val prop = BuildProperties.newInstance()
                val isMiUI = (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                        || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                        || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null)
                if (isMiUI) {
                    val versionName = prop.getProperty(KEY_MIUI_VERSION_NAME, null)
                    if (!TextUtils.isEmpty(versionName) && versionName!!.startsWith("V")) {
                        val versionNumber = versionName.replace("V", "")
                        val versionCode = Integer.parseInt(versionNumber)
                        if (versionCode < 9) {
                            return true
                        }
                    }
                }
                return false
            } catch (e: Exception) {
                return false
            }

        }

}

internal class BuildProperties @Throws(IOException::class)
private constructor() {

    private val properties: Properties

    val isEmpty: Boolean
        get() = properties.isEmpty

    init {
        properties = Properties()
        properties.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
    }

    fun containsKey(key: Any): Boolean {
        return properties.containsKey(key)
    }

    fun containsValue(value: Any): Boolean {
        return properties.containsValue(value)
    }

    fun entrySet(): MutableSet<MutableMap.MutableEntry<Any, Any>> {
        return properties.entries
    }

    fun getProperty(name: String): String {
        return properties.getProperty(name)
    }

    fun getProperty(name: String, defaultValue: String?): String? {
        return properties.getProperty(name, defaultValue)
    }

    fun keys(): Enumeration<Any> {
        return properties.keys()
    }

    fun keySet(): Set<Any> {
        return properties.keys
    }

    fun size(): Int {
        return properties.size
    }

    fun values(): Collection<Any> {
        return properties.values
    }

    companion object {

        @Throws(IOException::class)
        fun newInstance(): BuildProperties {
            return BuildProperties()
        }
    }

}
