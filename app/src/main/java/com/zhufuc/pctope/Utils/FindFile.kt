package com.zhufuc.pctope.Utils

import java.io.File

/**
 * Created by zhufu on 17-8-10.
 */

object FindFile {
    fun withKeywordOnce(keyword: String, findFrom: String): File? {
        val listBase = File(findFrom).listFiles()
        for (n in listBase) {
            if (n.isFile) {
                val str = n.path
                val keyLength = keyword.length
                val start = str.lastIndexOf('/') + 1
                if (str.indexOf(keyword, start) != -1)
                    return n
            } else {
                val mayReturn = FindFile.withKeywordOnce(keyword, n.path)
                if (mayReturn != null) return mayReturn
            }
        }
        return null
    }
}
