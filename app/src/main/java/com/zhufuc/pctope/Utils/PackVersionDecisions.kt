package com.zhufuc.pctope.Utils

import android.os.Environment
import android.view.View

import com.zhufuc.pctope.R

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.Objects
import java.util.StringTokenizer

/**
 * Created by zhufu on 7/30/17.
 */

class PackVersionDecisions(private val path: File) {
    var name: String = ""
    var description: String = ""

    init {
        if (File(path.toString() + "/manifest.json").exists()) readManifest()
    }

    val packVersion: String
        get() {
            if (!path.exists()) return "E:file not found."
            if (path.isDirectory) {
                val manifest = File(path.toString() + "/manifest.json")
                var v = ""
                if (manifest.exists()) {
                    if (name.isNotEmpty() && description.isNotEmpty()) {
                        if (File(path.toString() + "/pack_icon.png").exists())
                            v = "full"
                        else
                            v = "broken"
                    } else
                        v = "broken"
                } else {
                    if (File(path.toString() + "/pack.png").exists())
                        v = "full"
                    else
                        v = "broken"
                }

                if (File(path.toString() + "/textures/blocks").exists()) {
                    val PEblock = File(path.toString() + "/textures/blocks").list()
                    var i = 0
                    for (n in PEblock) {
                        if (n.lastIndexOf('.') != -1)
                            if (n.substring(n.lastIndexOf('.'), n.length) == ".png")
                                i++
                        if (i >= 10) return "Found:$v PE pack."
                    }
                } else if (File(path.toString() + "/assets/minecraft/textures/blocks").exists()) {
                    val PCblock = File(path.toString() + "/assets/minecraft/textures/blocks").list()
                    var i = 0
                    for (n in PCblock) {
                        if (n.lastIndexOf('.') != -1)
                            if (n.substring(n.lastIndexOf('.'), n.length) == ".png")
                                i++
                        if (i >= 10) return "Found:$v PC pack."
                    }
                } else if (File(path.toString() + "/assets/minecraft/textures/items").exists()) {
                    val PEitem = File(path.toString() + "/textures/items").list()
                    var i = 0
                    for (n in PEitem) {
                        if (n.lastIndexOf('.') != -1)
                            if (n.substring(n.lastIndexOf('.'), n.length) == ".png")
                                i++
                        if (i >= 10) return "Found:$v PE pack."
                    }
                } else if (File(path.toString() + "/assets/minecraft/textures/items").exists()) {
                    val PCitem = File(path.toString() + "/assets/minecraft/textures/items").list()
                    var i = 0
                    for (n in PCitem) {
                        if (n.lastIndexOf('.') != -1)
                            if (n.substring(n.lastIndexOf('.'), n.length) == ".png")
                                i++
                        if (i >= 10) return "Found:$v PC pack."
                    }
                }

                return "E:nothing found."
            } else
                return "E:File isn't a directory."
        }

    fun getIfIsResourcePack(testVersion: String): Boolean {
        if (path.isDirectory)
            if (testVersion == "PE" || testVersion == "ALL") {
                val test = File(path.toString() + "/textures").listFiles()
                if (test != null)
                    for (f in test)
                        if (f.isDirectory) return true
            }
        if (testVersion == "PC" || testVersion == "ALL") {
            val test = File(path.toString() + "/assets/minecraft/textures").listFiles()
            if (test != null)
                for (f in test)
                    if (f.isDirectory) return true
        }

        return false
    }

    fun getInMinecraftVer(v: View): String? {
        val metaFile = File(path.toString() + "/pack.mcmeta")
        if (metaFile.exists()) {

            val content = metaFile.readText(Charset.defaultCharset())

            //Find version code
            val posStart = content.indexOf(':', content.indexOf("pack_format"))
            val posEnd = content.indexOf(',', posStart)
            val StartToEnd = content.substring(posStart, posEnd)
            for (i in 0..StartToEnd.length - 1) {
                val now = StartToEnd.get(i)
                if (now.toInt() >= 48 && now.toInt() <= 57) {
                    when (now.toInt() - 48) {
                        1 -> return v.resources.getString(R.string.type_before_1_9)
                        2 -> return v.resources.getString(R.string.type_1_9_1_10)
                        3 -> return v.resources.getString(R.string.type_after_1_11)
                        else -> return null
                    }
                }
            }
        }
        return null
    }

    private fun readManifest() {
        val manifest = File(path.toString() + "/manifest.json")
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
