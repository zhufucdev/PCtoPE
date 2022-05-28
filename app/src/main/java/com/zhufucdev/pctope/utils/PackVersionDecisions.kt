package com.zhufucdev.pctope.utils

import android.view.View

import com.zhufucdev.pctope.R

import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.nio.charset.Charset

/**
 * Created by zhufu on 7/30/17.
 */

class PackVersionDecisions(private val path: File) {
    var name: String? = null
        private set
    var description: String? = null
        private set

    init {
        if (File("$path/manifest.json").exists()) readManifest()
    }

    val packVersion: String
        get() {
            if (!path.exists()) return "E:file not found."
            if (path.isDirectory) {
                val manifest = File("$path/manifest.json")
                var v = ""
                v = if (manifest.exists()) {
                    if (name != null && description != null) {
                        if (File("$path/pack_icon.png").exists())
                            "full"
                        else
                            "broken"
                    } else
                        "broken"
                } else {
                    if (File("$path/pack.png").exists())
                        "full"
                    else
                        "broken"
                }

                fun check(folder: File): Boolean {
                    if (!folder.exists()) {
                        return false
                    }
                    val list = folder.list() ?: return false
                    if (list.count { it.endsWith(".png") } >= 10) {
                        return true
                    }
                    return false
                }

                fun checkIfAny(vararg folders: Pair<String, String>): String {
                    for (pair in folders) {
                        if (check(File(pair.first))) {
                            return "Found:$v ${pair.second} pack."
                        }
                    }
                    return "E:nothing found."
                }

                return checkIfAny(
                    "$path/textures/blocks" to "PE",
                    "$path/assets/minecraft/textures/blocks" to "PC",
                    "$path/textures/items" to "PE",
                    "$path/assets/minecraft/textures/items" to "PC"
                )
            } else
                return "E:File isn't a directory."
        }

    fun getIfIsResourcePack(testVersion: String): Boolean {
        if (path.isDirectory)
            if (testVersion == "PE" || testVersion == "ALL") {
                val test = File("$path/textures").listFiles()
                if (test != null)
                    for (f in test)
                        if (f.isDirectory) return true
            }
        if (testVersion == "PC" || testVersion == "ALL") {
            val test = File("$path/assets/minecraft/textures").listFiles()
            if (test != null)
                for (f in test)
                    if (f.isDirectory) return true
        }

        return false
    }

    fun getInMinecraftVer(v: View): String? {
        val metaFile = File("$path/pack.mcmeta")
        if (metaFile.exists()) {

            val content = metaFile.readText(Charset.defaultCharset())

            //Find version code
            val posStart = content.indexOf(':', content.indexOf("pack_format"))
            val posEnd = content.indexOf(',', posStart)
            val startToEnd = content.substring(posStart, posEnd)
            for (i in startToEnd.indices) {
                val now = startToEnd[i]
                if (now.code in 48..57) {
                    return when (now.code - 48) {
                        1 -> v.resources.getString(R.string.type_before_1_9)
                        2 -> v.resources.getString(R.string.type_1_9_1_10)
                        3 -> v.resources.getString(R.string.type_after_1_11)
                        else -> null
                    }
                }
            }
        }
        return null
    }

    private fun readManifest() {
        val manifest = File("$path/manifest.json")
        if (manifest.exists()) {
            val intro = manifest.readText(Charset.defaultCharset())

            try {
                val jsonObjectOut = JSONObject(intro)
                val jsonObjectIn = jsonObjectOut.getJSONObject("header")
                name = jsonObjectIn.getString("name")
                description = jsonObjectIn.getString("description")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }
}
