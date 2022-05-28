package com.zhufucdev.pctope.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.AsyncTask
import android.os.Environment
import android.system.ErrnoException
import android.util.Log

import com.zhufucdev.pctope.R

import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by zhufu on 17-8-28.
 */

class TextureConversionUtils @Throws(FileNotFoundException::class)
constructor(private val FilePath: String, private val context: Context) {

    private fun makeSpace(i: Int): String {
        var spaces = ""
        for (j in 0..i) spaces += " "
        return spaces
    }

    var decisions: PackVersionDecisions? = null
    var verStr: String? = null

    private fun doVersionDecisions() {
        mLog.i("Conversion", "Version = $verStr")
        if (verStr == TextureCompat.brokenPE || verStr == TextureCompat.fullPE) {
            onPEDecisions()
        } else if (verStr == TextureCompat.brokenPC || verStr == TextureCompat.fullPC) {
            onPcDecisions()
        }
    }

    private fun doPCSpecialResourcesDecisions() {

        //需要特殊处理的贴图包括:
        //滞留药水 无法解决
        //怪物蛋手持贴图 无法解决
        //钟 无法解决
        //指南针 已解决
        //船手持贴图 无法解决
        //北极熊实体 已解决
        //豹猫 已解决
        //僵尸贴图错误 已解决
        //僵尸猪人实体 已解决
        //剥皮者贴图错误 已解决
        //卫道士实体
        //唤魔者实体
        //威克斯
        //箱子手持贴图及大箱子实体 无法解决<>已解决
        //活塞 无法解决
        //方块破坏崩裂贴图 已解决
        //背景 已解决
        //画 已解决
        //护甲

        val from = arrayOf(
            File("$textures/entity/chest/normal_double.png"),
            File("$textures/items/dragon_breath.png"),
            File("$textures/entity/bear/polarbear.png"),
            File("$textures/entity/cat/black.png"),
            File("$textures/entity/zombie_pigman.png"),
            File("$textures/gui/options_background.png"),
            File("$textures/painting/paintings_kristoffer_zetterstrand.png")
        )
        val to = arrayOf(
            File("$textures/entity/chest/double_normal.png"),
            File("$textures/items/dragons_breath.png"),
            File("$textures/entity/polarbear.png"),
            File("$textures/entity/cat/blackcat.png"),
            File("$textures/entity/pig/pigzombie.png"),
            File("$textures/gui/background.png"),
            File("$textures/painting/kz.png")
        )
        from.forEachIndexed { index, it ->
            it.renameTo(to[index])
        }


        //For compasses
        if (File("$textures/items/compass_00.png").exists()) {

            var imageHeight = 0

            val compasses = ArrayList<Bitmap>()
            for (i in 0..31) {
                val format = DecimalFormat("00")
                val image = File("$textures/items/compass_${format.format(i)}.png")
                if (image.exists()) {
                    val now = BitmapFactory.decodeFile(image.path)
                    compasses.add(now)
                    imageHeight += now.height
                    image.delete()
                }
            }

            val bitmap = Bitmap.createBitmap(compasses[0].width, imageHeight, compasses[0].config)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(compasses[0], Matrix(), null)

            for (i in 1 until compasses.size) {
                canvas.drawBitmap(compasses[i], 0f, (compasses[i - 1].height * i).toFloat(), null)
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            File("$textures/items/compass_atlas.png").writeBytes(byteArrayOutputStream.toByteArray())
        }

        //For zombies
        val images = arrayOf(
            File("$textures/entity/zombie/zombie.png"),
            File("$textures/entity/zombie/husk.png"),
            File("$textures/entity/pig/pigzombie.png")
        )
        for (image in images) {
            if (image.exists()) {
                val zombieImage = BitmapFactory.decodeFile(image.path)

                if (zombieImage.height == zombieImage.width) {
                    val bitmap = Bitmap.createBitmap(
                        zombieImage,
                        0,
                        0,
                        zombieImage.width,
                        zombieImage.height / 2
                    )

                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    image.writeBytes(byteArrayOutputStream.toByteArray())

                }

            }
        }

        //For destroy stages
        (0..9)
            .map { File("$path/textures/blocks/destroy_stage_$it.png") }
            .filter { it.exists() }
            .forEach { it.renameTo(File("$textures/environment/" + it.name)) }

        //For armors
        val armorFolder = File("$textures/models/armor")
        if (armorFolder.exists() && armorFolder.isDirectory) {
            val armorsList = armorFolder.listFiles()
            for (i in 0 until armorsList.size) {
                if (armorsList[i].name.contains("_layer")) {
                    var newPath: String
                    val oldName = armorsList[i].name
                    var newName = StringBuilder(oldName)
                    val index = oldName.indexOf("_layer")

                    if (oldName.contains("chainmail"))
                        when (oldName) {
                            "chainmail_layer_1.png" -> newName = StringBuilder("chain_1.png")
                            "chainmail_layer_2.png" -> newName = StringBuilder("chain_2.png")
                        }
                    else newName.delete(index, index + 6)

                    newPath = "${armorFolder.path}/$newName"
                    armorsList[i].renameTo(File(newPath))
                }
            }
        }

        //For Banners
        val bannersRoot = File("$textures/entity/banner")
        if (bannersRoot.exists() && bannersRoot.list().size == 39) {
            val bannerList = bannersRoot.listFiles()
            val bannerArray = ArrayList<File>()

            val banners = ArrayList<Bitmap>()
            var isItFake = false
            for (f in bannerList) {
                if (!f.path.endsWith(".png")) {
                    isItFake = true
                    break
                }
                mLog.i("Banners", "Added $f")
                bannerArray.add(f)
            }
            if (!isItFake) {
                bannerArray.sort()
                bannerArray.forEach {
                    banners.add(BitmapFactory.decodeFile(it.path))
                    it.delete()
                }

                //create base
                val perSize = banners[0].width
                val base = Bitmap.createBitmap(perSize * 8, perSize * 8, Bitmap.Config.ARGB_8888)
                var x = 0
                var y = 0
                val canvas = Canvas(base)
                //draw
                banners.forEach {
                    canvas.drawBitmap(it, perSize * x * 1f, perSize * y * 1f, null)
                    x++
                    if (x > 7) {
                        x = 0
                        y++
                    }
                }
                val outPut = ByteArrayOutputStream()
                base.compress(Bitmap.CompressFormat.PNG, 100, outPut)
                val img = File("$bannersRoot/banner.png")
                img.writeBytes(outPut.toByteArray())
            }
        }
    }

    private fun onPcDecisions() {
        val icon = File("$path/pack.png")
        val iconPE = File("$path/pack_icon.png")
        icon.renameTo(iconPE)//Rename icon to PE
        textures = File("$path/assets/minecraft/textures")
        val texturePE = File("$path/textures")
        mLog.i("Conversion", "Moving $textures to $texturePE")
        textures.renameTo(texturePE)//Move textures folder
        textures = texturePE

        //Delete something that we don't need
        File("$path/pack.mcmeta").delete()
        DeleteFolder.delete("$path/assets")

        doPCSpecialResourcesDecisions()

        val files = listFiles(textures)
        val texturesList = File("$textures/textures_list.json")
        val json = StringBuilder()
        json.append("[\n")
        files!!.forEach {
            mLog.i("Conversion", "Listing $it")
            json.append("$it,\n")
        }
        json.deleteCharAt(json.length - 2)//del the latest ","
        json.append(']')
        Log.i("Conversion", "JSON = \"$json\"")
        texturesList.writeText(json.toString(), Charset.defaultCharset())

    }


    private fun onPEDecisions() {
        textures = File("$path/textures")
    }

    private fun doJsonFixing(text: String): String {
        val lines = ArrayList<String>()
        lines.addAll(text.lines())
        var i = 0
        while (i < lines.size) {
            val thisLine = lines[i]
            if (thisLine.contains("textures/")) {
                val fileTest = File(
                    path + "/" + thisLine.substring(
                        thisLine.indexOf("textures/"),
                        thisLine.lastIndexOf('\"')
                    ) + ".png"
                )
                if (!fileTest.exists()) {
                    var first: Int = -1
                    var last: Int = -1
                    mLog.i("JsonFixing", i.toString() + ":" + fileTest.path + " doesn't exist")

                    for (j in i downTo 0) {
                        val line = lines[j]
                        if (line.endsWith('{')) {
                            first = j
                            break
                        }
                    }

                    for (j in i until lines.size) {
                        val line = lines[j]
                        if (line.contains('}')) {
                            last = j
                            break
                        }
                    }


                    if (first == -1 || last == -1)
                        continue

                    i = first - 1

                    for (t in first..last) {
                        lines.removeAt(first)
                    }

                }
            }
            i++
        }
        val string = StringBuilder()
        for (j in 0 until lines.size)
            string.append(lines[j] + "\n")
        return string.toString()
    }

    private fun putJSONPrefix(json: String): String =
        JSONObject(json).put("resource_pack_name", packname).toString()

    @Throws(JSONException::class)

    private fun doJSONWriting() {
        //==>define
        //for basic information
        val raw = context.resources
        val data = arrayOf(
            raw.openRawResource(R.raw.items_client),
            raw.openRawResource(R.raw.blocks),
            raw.openRawResource(R.raw.flipbook_textures),
            raw.openRawResource(R.raw.item_texture),
            raw.openRawResource(R.raw.terrain_texture)
        )
        val paths = arrayOf(File("$path/items_client.json"), File("$path/blocks.json"))

        for (i in paths.indices) {
            paths[i].writeBytes(data[i].readBytes())
        }

        //for terrain block textures
        var out = File("$textures/terrain_texture.json")
        var fixed = doJsonFixing(data[4].reader().readText())
        fixed = putJSONPrefix(fixed)
        out.writeText(fixed)
        //for item texture file

        var isCreated = true
        val temp = arrayOf(
            "$textures/item_texture.json",
            "$textures/flipbook_textures.json",
            "$path/manifest.json"
        )
        for (i in temp.indices) {
            val t = File(temp[i])
            if (!t.exists())
                try {
                    isCreated = t.createNewFile()
                } catch (e: IOException) {
                    mOnCrashListener.onCrash(e.toString())
                    e.printStackTrace()
                }

        }
        if (isCreated) {
            //For Item Texture
            out = File("$textures/item_texture.json")
            fixed = doJsonFixing(data[3].reader().readText())
            fixed = putJSONPrefix(fixed)
            out.writeText(fixed)

            //for flip book texture
            out = File("$textures/flipbook_textures.json")
            fixed = doJsonFixing(data[2].reader().readText())
            out.writeText(fixed)

            //Free up space
            fixed = ""

            //for manifest file
            val out1 = JSONObject()
            val versionArray = JSONArray()

            try {
                versionArray.put(0)
                versionArray.put(0)
                versionArray.put(1)

                out1.put("format_version", 1)
                val header = JSONObject()
                header.put("description", packdescription)
                header.put("name", packname)
                header.put("uuid", UUID.randomUUID().toString())
                header.put("version", versionArray)
                out1.put("header", header)

                val modules = JSONArray()
                val modulesObjs = JSONObject()
                modulesObjs.put("description", packdescription)
                modulesObjs.put("type", "resources")
                modulesObjs.put("uuid", UUID.randomUUID().toString())
                modulesObjs.put("version", versionArray)
                modules.put(modulesObjs)
                out1.put("modules", modules)

            } catch (e: JSONException) {
                e.printStackTrace()
                mOnCrashListener.onCrash(e.toString())
            }

            File("$path/manifest.json").writeText(out1.toString(), Charset.defaultCharset())

        } else
            mOnCrashListener.onCrash("Could not create JSON files.")

    }

    var compressFinalSize = 0
    private fun doMainInCompressing(n: File) {
        if (n.isFile) {
            //Show progress
            mLog.d("compression", "Compressing $n")
            mConversionChangeListener.inDoingImageCompressions(n.path)


            val str = n.path
            if (str.substring(str.lastIndexOf("."), str.length) != ".png")
                return

            val options = BitmapFactory.Options()
            options.inSampleSize = 1
            val image = BitmapFactory.decodeFile(n.path, options)
            //get compressed bitmap
            var compressHeight = compressFinalSize
            var compressWidth = compressFinalSize
            if (image.width - image.height < -5) {
                compressHeight *= (image.width / compressFinalSize)
            } else if (image.height - image.width > 5) {
                compressWidth *= (image.height / compressFinalSize)
            }
            val compressed = CompressImage.getBitmap(image, compressHeight, compressWidth)

            val baos = ByteArrayOutputStream()
            compressed!!.compress(Bitmap.CompressFormat.PNG, 100, baos)//png

            n.delete()
            n.writeBytes(baos.toByteArray())

        }
    }

    private fun doImageCompressions() {
        if (compressFinalSize == 0)
            return
        mLog.i("Pack Conversion", "Doing image compressions...")
        //get images
        val items = File("$textures/items").listFiles()
        val blocks = File("$textures/blocks").listFiles()
        if (items != null) {
            for (n in items)
                doMainInCompressing(n)
        }
        if (blocks != null) {
            for (n in blocks)
                doMainInCompressing(n)
        }
    }

    var path: String
    private var packname: String? = null
    private var packdescription: String? = null
    var skipUnzip: Boolean = false
    lateinit var textures: File

    init {
        this.path = context.externalCacheDir!!.path

        if (!skipUnzip) {
            if (!File(FilePath).exists()) {
                throw FileNotFoundException(context.getString(R.string.crash_converstion_input_file_not_found))
            }
            val fileName =
                FilePath.substring(FilePath.lastIndexOf('/') + 1, FilePath.lastIndexOf('.'))
            //==>get file name
            path += "/$fileName"
            mLog.d("unzip", "We will unzip $FilePath to $path")
        } else
            path = FilePath
    }

    /*
        Some Listeners...
     */
    private var mOnUncompressListener: OnUncompressListener = object : OnUncompressListener {
        override fun onPreUncompress() {}
        override fun inUncompressing() {}
        override fun onPostUncompress(result: Boolean, version: String?) {}
    }

    interface OnUncompressListener {
        fun onPreUncompress()
        fun inUncompressing()
        fun onPostUncompress(result: Boolean, version: String?)
    }

    private var mOnCrashListener: OnCrashListener = object : OnCrashListener {
        override fun onCrash(errorContent: String) {}
    }

    interface OnCrashListener {
        fun onCrash(errorContent: String)
    }

    fun setOnUncompressListener(listener: OnUncompressListener) {
        this.mOnUncompressListener = listener
    }

    fun setOnCrashListener(listener: OnCrashListener) {
        this.mOnCrashListener = listener
    }

    private var mConversionChangeListener: ConversionChangeListener =
        object : ConversionChangeListener {
            override fun inDoingVersionDecisions() {}
            override fun inDoingImageCompressions(whatsBeingCompressing: String) {}
            override fun inDoingJSONWriting() {}
            override fun inDoingMcpackCompressing(file: String) {}
            override fun onDone() {}
        }

    interface ConversionChangeListener {
        fun inDoingVersionDecisions()
        fun inDoingImageCompressions(whatsBeingCompressing: String)
        fun inDoingJSONWriting()
        fun inDoingMcpackCompressing(file: String)
        fun onDone()
    }

    fun setConversionChangeListener(listener: ConversionChangeListener) {
        this.mConversionChangeListener = listener
    }

    fun uncompressPack() {
        class UnzippingTask : AsyncTask<Void, Int, Boolean>() {

            override fun onPreExecute() {
                mOnUncompressListener.onPreUncompress()
            }

            override fun doInBackground(vararg params: Void): Boolean? {
                if (!skipUnzip) {
                    mOnUncompressListener.inUncompressing()
                    try {
                        mLog.d("unzip", "Unzipping to $path")
                        unzip(File(FilePath), path, "0")
                    } catch (e: Exception) {
                        return false
                    }

                }
                return isPathUseful//Find the true root path
            }

            private val isPathUseful: Boolean = false
                get() {
                    val pathDecisions = File(path)
                    if (pathDecisions.exists() && pathDecisions.isDirectory) {
                        val fileListInPath = pathDecisions.listFiles()
                        val dirs = ArrayList<File>()
                        var filesFound = 0
                        var dirsFound = 0
                        for (test in fileListInPath) {
                            if (test.isFile)
                                filesFound++
                            else if (test.isDirectory) {
                                dirs.add(test)
                                dirsFound++
                            }
                        }
                        if (filesFound >= 1 && dirsFound >= 1) {
                            return true
                        } else {
                            for (i in dirs.indices) {
                                path = dirs[i].path
                                if (field) {
                                    return true
                                }
                            }
                            return false
                        }
                    }
                    return false
                }


            override fun onPostExecute(result: Boolean?) {
                if (result!!) {
                    decisions = PackVersionDecisions(File(path))
                    verStr = decisions!!.packVersion
                    if (verStr!![0] != 'E') {
                        iconPath =
                            if (verStr == TextureCompat.fullPC || verStr == TextureCompat.brokenPC) path + "/pack.png" else if (verStr == TextureCompat.fullPE || verStr == TextureCompat.brokenPE) path + "/pack_icon.png" else null

                        mOnUncompressListener.onPostUncompress(true, verStr)
                    } else {
                        mOnUncompressListener.onPostUncompress(false, null)
                    }
                } else
                    mOnUncompressListener.onPostUncompress(false, null)
            }
        }

        UnzippingTask().execute()
    }

    fun doConverting(packName: String, packDescription: String, mcpackCompressDest: String) {
        this.packname = packName
        this.packdescription = packDescription

        mLog.i("Pack Conversion", "Doing version decisions...")
        mConversionChangeListener.inDoingVersionDecisions()
        doVersionDecisions()

        doImageCompressions()

        try {
            mLog.i("Pack Conversion", "Doing json Writing")
            mConversionChangeListener.inDoingJSONWriting()
            doJSONWriting()
        } catch (e: FileNotFoundException) {
            mOnCrashListener.onCrash(e.toString())
            e.printStackTrace()
        }

        //For if icon doesn't exist
        val iconTest = File("$path/pack_icon.png")
        if (!iconTest.exists()) {
            mLog.i("Pack Conversion", "Writing icon for non-icon-pack...")
            val inputStream = context.resources.openRawResource(R.raw.bug_pack_icon)

            File("$path/pack_icon.png").writeBytes(inputStream.readBytes())
        }

        //Move to dest
        mLog.i("Pack Conversion", "Moving to dest...")
        val dest = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/games/com.mojang/resource_packs/" + packName
        )
        if (dest.isDirectory && dest.exists()) dest.mkdirs()
        val rename = File(path).renameTo(dest)
        if (!rename) {
            File(path).copyRecursively(dest)
            DeleteFolder.delete(path)
        }

        if (mcpackCompressDest != "") {
            mConversionChangeListener.inDoingMcpackCompressing(mcpackCompressDest)
            compress(dest.path, mcpackCompressDest)
        }

        mConversionChangeListener.onDone()
        //Done
    }

    private var iconPath: String? = null
    var icon: Bitmap?
        get() = if (iconPath == null || !File(iconPath!!).exists()) null else BitmapFactory.decodeFile(
            iconPath
        )
        set(icon) {
            val path = iconPath
            if (path != null) {
                try {
                    val baos = ByteArrayOutputStream()
                    icon!!.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    File(path).writeBytes(baos.toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                    mOnCrashListener.onCrash(e.toString())
                }
            }
        }

    companion object {
        private val fileList = ArrayList<String>()

        private fun listFiles(path: File): ArrayList<String>? {
            val files = path.listFiles() ?: return null
            for (f in files) {
                if (f.isDirectory) {
                    listFiles(f)
                } else {
                    val s = f.path
                    if (s.endsWith(".png")) {
                        val content = s.substring(s.lastIndexOf("textures/"))
                        fileList.add(content)
                    }
                }
            }
            return fileList
        }

        @Throws(ZipException::class, net.lingala.zip4j.exception.ZipException::class)
        private fun unzip(zipFile: File, dest: String, passwd: String) {
            val zFile = net.lingala.zip4j.core.ZipFile(zipFile)
            if (!zFile.isValidZipFile) {
                throw ZipException("压缩文件不合法,可能被损坏.")
            }
            val destDir = File(dest)// 解压目录
            if (destDir.exists()) {
                DeleteFolder.delete(dest)
            }
            destDir.mkdirs()
            if (zFile.isEncrypted) {
                zFile.setPassword(passwd.toCharArray())
            }
            zFile.extractAll(dest)
        }

        private fun compress(baseIndex: String, toIndex: String) {
            mLog.i("Compressions", "Compressing $baseIndex to $toIndex")

            val src = File(toIndex)
            if (src.exists())
                src.delete()
            val result = File(toIndex)
            if (result.parentFile?.isDirectory == false) {
                result.parentFile!!.delete()
                result.parentFile!!.mkdirs()
            }

            val par = ZipParameters()
            par.compressionMethod = Zip4jConstants.COMP_DEFLATE
            par.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL

            val zipFile = net.lingala.zip4j.core.ZipFile(toIndex)
            zipFile.addFolder(baseIndex, par)
        }
    }

}
