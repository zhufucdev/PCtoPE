package com.zhufucdev.pctope.utils

import java.io.File

/**
 * Created by zhufu on 17-7-29.
 */

object DeleteFolder {
    /**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    private fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.isFile && file.exists()) {
            file.delete()
        } else false
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    private fun deleteDirectory(filePath: String): Boolean {
        var filePath = filePath
        var flag = false
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator
        }
        val dirFile = File(filePath)
        if (!dirFile.exists() || !dirFile.isDirectory) {
            return false
        }
        flag = true
        val files = dirFile.listFiles()
        //遍历删除文件夹下的所有文件(包括子目录)
        for (i in files.indices) {
            if (files[i].isFile) {
                //删除子文件
                flag = deleteFile(files[i].absolutePath)
                if (!flag) break
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].absolutePath)
                if (!flag) break
            }
        }
        return if (!flag) false else dirFile.delete()
        //删除当前空目录
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     * @param filePath  要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    fun delete(filePath: String): Boolean {
        val file = File(filePath)
        return if (!file.exists()) {
            false
        } else {
            if (file.isFile) {
                // 为文件时调用删除文件方法
                deleteFile(filePath)
            } else {
                // 为目录时调用删除目录方法
                deleteDirectory(filePath)
            }
        }
    }
}
