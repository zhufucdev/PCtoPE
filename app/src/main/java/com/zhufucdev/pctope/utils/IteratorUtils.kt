package com.zhufucdev.pctope.utils

/**
 * Created by zhufu on 17-10-7.
 */
class IteratorUtils {
    companion object {
        fun convertIntoList(iterator : Iterator<String>) : List<String>{
            val list : ArrayList<String> = ArrayList()
            while (iterator.hasNext()){
                list.add(iterator.next())
            }
            return list
        }
    }
}