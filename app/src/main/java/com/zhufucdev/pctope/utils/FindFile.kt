package com.zhufucdev.pctope.utils

/**
 * Created by zhufu on 17-8-10.
 */

object FindFile {
    fun withKeywordOnce(keyword: String, findFrom: String): String? {
        val lister = ListFiles(findFrom)
        val list = lister.getList()
        for (n in list) {
            if (n.indexOf(keyword)!=-1){
                return n
            }
        }
        return ""
    }
}
