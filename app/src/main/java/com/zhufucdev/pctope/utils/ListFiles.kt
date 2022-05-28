package com.zhufucdev.pctope.utils

import java.io.File
import java.io.IOException

/**
 * Created by zhufu on 17-11-25.
 */
class ListFiles(path : String) {
    private val filePath = File(path)
    init {
        if (!filePath.exists() || !filePath.isDirectory)
            throw IOException("Path doesn't exist or isn't a directory.")
    }

    fun getFileList(suffix : String) : List<File> = list(suffix,filePath)
    fun getFileList() : List<File> = list("",filePath)

    fun getList(suffix: String) : List<String> {
        val result = ArrayList<String>()
        list(suffix,filePath).forEach{ result.add(it.path) }
        return result
    }
    fun getList() : List<String> = getList("")

    companion object {
        private fun list(suffix: String,path : File) : List<File>{
            val list = path.listFiles()
            val result = ArrayList<File>()
            for (f in list){
                if (f.isDirectory){
                    list(suffix,f).forEach { result.add(it) }
                }
                else{
                    if (!suffix.isEmpty() && !f.path.endsWith(suffix)){
                        continue
                    }
                    result.add(f)
                }
            }
            return result.toList()
        }
    }
}