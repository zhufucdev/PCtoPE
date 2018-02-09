package com.zhufuc.pctope.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.zhufuc.pctope.R
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Created by zhufu on 7/22/17.
 */

open class Textures(path: File) {

    class JSONInfo(val id: Int,val title: String,val subtitle: String = "",val extraData: String=""){
        fun getUserSubtitle(context: Context) : String
            = when(id){
                TextureCompat.JSON_MANIFEST -> context.getString(R.string.subtitle_manifest)
                TextureCompat.JSON_BLOCKS -> context.getString(R.string.subtitle_blocks)
            else -> subtitle
        }

        fun getUserTitle(context: Context) : String
        = when(id){
            TextureCompat.JSON_MANIFEST -> context.getString(R.string.title_manifest)
            TextureCompat.JSON_BLOCKS -> context.getString(R.string.title_blocks)
            TextureCompat.MANIFEST.name -> context.getString(R.string.title_name)
            TextureCompat.MANIFEST.description -> context.getString(R.string.title_description)
            TextureCompat.MANIFEST.version -> context.getString(R.string.title_version)
            TextureCompat.MANIFEST.uuid -> context.getString(R.string.title_uuid)
            else -> title
        }

        val exists: Boolean
        get() = extraData.indexOf("exists") == -1
        fun getPath(): String{
            val path = extraData.indexOf("path")
            val mark = extraData.indexOf(':',path)

            if (path == -1 || mark == -1) return ""

            var isUsable = true
            extraData.substring(path+4,mark).forEach {
                if (it != ' '){
                    isUsable = false
                }
            }

            if (isUsable){
                val markB = extraData.indexOf(',',mark)
                if (markB == -1)
                    return extraData.substring(mark+1)
                else
                    return extraData.substring(mark+1,markB)
            }
            return ""
        }
    }

    abstract class JSONBase(val file: File){
        abstract fun apply()
        abstract override fun toString() : String
        abstract fun setContent(string: String)
        var raw: String
        get() = toString()
        set(value) = setContent(value)
    }

    class manifest(file: File) : JSONBase(file){
        override fun toString(): String {
            return JSONContent.toString()
        }

        override fun setContent(string: String) {
            JSONContent = JSONObject(string)
        }

        var JSONContent = JSONObject()
        var JSONHeader = JSONObject()
        var isUsable = false
        var name : String = ""
        var description : String = ""
        var uuid : String = ""
        init {
            if (file.exists()) {
                JSONContent = JSONObject(file.readText())
                if (!JSONContent.isNull("header")) {
                    updateHeaderInfos()
                }
            }
        }

        fun autoFix(){
            if (JSONHeader.isNull("name"))
                JSONHeader.put("name","")
            if (JSONHeader.isNull("description"))
                JSONHeader.put("description","")
            if (JSONHeader.isNull("uuid"))
                JSONHeader.put("uuid",UUID.randomUUID().toString())

            JSONContent.put("header",JSONHeader)

            updateHeaderInfos()
        }

        fun buildWith(info: Info){
            JSONHeader = JSONObject()
            if (!info.name.isEmpty())
                JSONHeader.put("name",info.name)
            if (!info.description.isEmpty())
                JSONHeader.put("description",info.description)
            if (!info.uuid.isEmpty())
                JSONHeader.put("uuid",info.uuid)
            if (!info.version.isEmpty()){
                val version = info.version.split('.')
                val array = JSONArray(version)
                JSONHeader.put("version",array)
            }
            if (!(info.extraHeaderData.length() == 0)){
                val keys =info.extraHeaderData.keys()
                while (keys.hasNext()) {
                    val name = keys.next()
                    JSONHeader.put(name, info.extraHeaderData.getString(name))
                }
            }
            if (!(info.extraData.length() == 0)){
                val keys = info.extraData.keys()
                while (keys.hasNext()) {
                    val name = keys.next()
                    JSONContent.put(name,info.extraData.getString(name) )
                }
            }
            JSONContent.put("header",JSONHeader)
            updateHeaderInfos()
        }

        override fun apply() = file.writeText(toString(), Charset.defaultCharset())

        private fun updateHeaderInfos(){
            isUsable = true
            JSONHeader = JSONContent.getJSONObject("header")
            if (JSONHeader.has("name"))
                name = JSONHeader.getString("name")
            else isUsable = false

            if (JSONHeader.has("description"))
                description = JSONHeader.getString("description")
            else isUsable = false

            if (JSONHeader.has("uuid"))
                uuid = JSONHeader.getString("uuid")
            else isUsable = false
        }

        class Info(val name: String = "", val description: String = "", val uuid: String = "", val version: String = "",val extraHeaderData: JSONObject = JSONObject(),val extraData : JSONObject = JSONObject())
    }


    val path: String = path.path
    val name: String
    val description: String

    val blockFile : File
    get() = File("$path/blocks.json")

    val terrainBlockFile : File
    get() = File("$path/textures/terrain_texture.json")

    val manifestFile : File
    get() = File("${this.path}/manifest.json")

    private var Manifest : manifest? = null
    private var Blocks : List<Block>? = null

    fun getManifest() : manifest?{
        if (Manifest == null && manifestFile.exists()) {
            val temp = manifest(manifestFile)
            Manifest = temp
        }
        return Manifest
    }

    fun getBlocks() : List<Block>?{
        if (Blocks == null && blockFile.exists()){
            val temp = ArrayList<Block>()
            val blockJSON = mJSON(blockFile.readText())
            val terrainJSON = terrainBlockFile.readText()
            blockJSON.list().forEach {
                mLog.i("Blocks","Get Block Object for $it")
                temp.add(Block.getBlockByJSON(it,blockJSON.toString(),terrainJSON))
            }
            Blocks = temp.toList()
        }
        return Blocks
    }


    var position : Int = -1

    private val version: PackVersionDecisions = PackVersionDecisions(path)

    init {
        name = version.name
        description = version.description
    }

    fun IfIsResourcePack(testVersion: String): Boolean? = version.getIfIsResourcePack(testVersion)

    fun getVersion(): String = version.packVersion

    val icon: String?
        get() {
            val icon = File(path + "/pack_icon.png")
            return if (icon.exists()) {
                icon.path
            } else
                null
        }



    class Edit(private val path: String) {

        private var intro: String? = null

        private val textures: Textures

        interface CompressionProgressChangeListener {
            fun OnProgressChangeListener(whatsBeingCompressed: String?, isDone: Boolean)
        }

        private var compressionProgressChangeListener: CompressionProgressChangeListener? = null
        fun setOnCompressionProgressChangeListener(listener: CompressionProgressChangeListener) {
            this.compressionProgressChangeListener = listener
        }

        interface OnCrashListener {
            fun onCrash(e: String)
        }

        private var onCrashListener: OnCrashListener? = null
        fun setOnCrashListener(listener: OnCrashListener) {
            this.onCrashListener = listener
        }

        init {
            textures = Textures(File(path))
        }

        fun changeNameAndDescription(nameIndex: String, descriptionIndex: String) {
            readManifest()

            Log.i("Change Existed Pack", "Name Set=$nameIndex, Description Set=$descriptionIndex, Manifest Read=\n$intro")

            try {
                val `object` = JSONObject(intro)
                if (`object`.has("header")) {
                    val header = `object`.getJSONObject("header")
                    header.put("name", nameIndex)
                    `object`.put("header", header)
                    intro = `object`.toString()

                    header.put("description", descriptionIndex)
                    `object`.put("header", header)
                    intro = `object`.toString()
                } else
                    intro = overwriteManifest(nameIndex, descriptionIndex)
                if (`object`.has("modules")) {
                    val array = `object`.getJSONArray("modules")
                    val descriptionObj = array.getJSONObject(0)
                    descriptionObj.put("description", descriptionIndex)
                    `object`.put("modules", array)
                    intro = `object`.toString()
                } else
                    intro = overwriteManifest(nameIndex, descriptionIndex)
            } catch (e: JSONException) {
                intro = overwriteManifest(nameIndex, descriptionIndex)
            }

            writeResult(JsonFormatTool.formatJson(intro!!))
        }

        @Throws(IOException::class)
        fun iconEdit(icon: String) {
            val baos = ByteArrayOutputStream()
            val bitmap = BitmapFactory.decodeFile(icon)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

            var output: FileOutputStream? = null
            output = FileOutputStream(path + "/pack_icon.png")
            output.write(baos.toByteArray())
            output.close()
        }

        @Throws(IOException::class)
        fun iconEdit(bitmap: Bitmap) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

            var output: FileOutputStream? = null
            output = FileOutputStream(path + "/pack_icon.png")
            output.write(baos.toByteArray())
            output.close()
        }

        fun compressImages(compressFinalSize: Int) {
            if (compressFinalSize == 0)
                return
            Log.i("Pack Conversion", "Doing image compressions...")
            //get images
            val items = File(path + "/textures/items").listFiles()
            val blocks = File(path + "/textures/blocks").listFiles()
            if (items != null) {
                for (n in items)
                    doMainInCompressing(n, compressFinalSize)
            }
            if (blocks != null) {
                for (n in blocks)
                    doMainInCompressing(n, compressFinalSize)
            }
            compressionProgressChangeListener!!.OnProgressChangeListener(null, true)
        }

        private fun doMainInCompressing(n: File, compressFinalSize: Int) {
            if (n.isFile) {
                //Show progress
                compressionProgressChangeListener!!.OnProgressChangeListener(n.path, false)
                Log.d("compression", "Compressing " + n)

                val str = n.path
                if (str.substring(str.lastIndexOf(""), str.length) != ".png")
                    return

                val image = BitmapFactory.decodeFile(n.path)
                //get compressed bitmap
                var compressHeight = compressFinalSize
                var compressWidth = compressFinalSize
                if (image.width - image.height < -5) {
                    compressHeight = compressHeight * (image.width / compressFinalSize)
                } else if (image.height - image.width > 5) {
                    compressWidth = compressWidth * (image.height / compressFinalSize)
                }
                val compressed = CompressImage.getBitmap(image, compressHeight, compressWidth)

                if (compressed == null) {
                    onCrashListener!!.onCrash("Compressing Resources: could not compress " + n.path)
                }

                val baos = ByteArrayOutputStream()
                compressed!!.compress(Bitmap.CompressFormat.PNG, 100, baos)//png

                try {
                    val outputStream = FileOutputStream(n)
                    outputStream.write(baos.toByteArray())

                    outputStream.flush()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        private fun writeResult(result: String) {
            try {
                val outputStream = FileOutputStream(path + "/manifest.json")
                outputStream.write(result.toByteArray())

                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        private fun readManifest() {
            val manifest = File(path + "/manifest.json")
            if (intro != null)
                return

            if (manifest.exists()) {
                intro = manifest.readText(Charset.defaultCharset())
            }
        }

        fun overwriteManifest(name: String, description: String): String {
            val out = JSONObject()
            val versionArray = JSONArray()
            try {
                versionArray.put(0)
                versionArray.put(0)
                versionArray.put(1)

                out.put("format_version", 1)
                val header = JSONObject()
                header.put("description", description)
                header.put("name", name)
                header.put("uuid", UUID.randomUUID().toString())
                header.put("version", versionArray)
                out.put("header", header)

                val modules = JSONArray()
                val modulesObjs = JSONObject()
                modulesObjs.put("description", description)
                modulesObjs.put("type", "resources")
                modulesObjs.put("uuid", UUID.randomUUID().toString())
                modulesObjs.put("version", versionArray)
                modules.put(modulesObjs)
                out.put("modules", modules)

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return JsonFormatTool.formatJson(out.toString())

        }

        fun setMcpack(dest : String){
            val src = File(dest)
            if (src.exists())
                src.delete()
            if (!src.parentFile.exists()) src.parentFile.mkdirs()

            val par = ZipParameters()
            par.compressionMethod = Zip4jConstants.COMP_DEFLATE
            par.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL

            val zipFile = net.lingala.zip4j.core.ZipFile(dest)
            zipFile.addFolder(path,par)
        }
    }

}
