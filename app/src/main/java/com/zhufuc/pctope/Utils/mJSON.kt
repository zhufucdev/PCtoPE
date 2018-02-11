package com.zhufuc.pctope.Utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by zhufu on 18-1-6.
 */
class mJSON(content: String) {
    private var JSONContent : String = content
    var rootType : Int
    init {
        rootType = getTypeWithPlainText(content)
    }
    companion object {
        const val TYPE_OBJECT = 1;
        const val TYPE_ARRAY = 2;
        const val TYPE_VALUE = 3;
        const val DATA_NOT_FOUND_FLAG = "[Data Not Found]"
    }

    fun set(pathData: String,value: String){
        if (JSONContent.isEmpty()) return
        /**
         *  @pathData  path from JSON root
         *       e.g "grass/textures/top"
         *
         *  @value   value that the path data sets
         */
        val path = pathData.split("/")
        var next : String = value

        for (i in path.size-1 downTo 1){
            var type = TYPE_OBJECT
            if (path[i].indexOf("array")!=-1 && path[i].indexOf('.')!=-1)
                type = TYPE_ARRAY

            val temp : String
            = if (type == TYPE_OBJECT){
                val obj = JSONObject()
                obj.put(path[i],next)
                obj.toString()
            }
            else {
                val array = JSONArray()
                val index = path[i].substring(path[i].indexOf('.')).toInt()
                array.put(index,next)
                array.toString()
            }
            next = temp
        }
        JSONContent =
        if (rootType == TYPE_OBJECT) {
            val tmp = JSONObject(JSONContent)
            tmp.put(path.first(),next)
            tmp.toString()
        }
        else if (rootType == TYPE_ARRAY){
            val tmp = JSONArray()
            tmp.put(next)
            tmp.toString()
        }
        else
            value
    }

    fun get(pathData: String) : String{
        if (JSONContent.isEmpty()) return ""
        val path = pathData.split("/")
        var last = JSONContent
        Log.d("mJSON","path = $path")

        for(i in 0 until path.size){

            if (path[i].isEmpty()) continue

            val type = getTypeWithPlainText(last)
            val temp : String
            = if (type == TYPE_OBJECT){
                val obj = JSONObject(last)
                if (!obj.has(path[i])) return DATA_NOT_FOUND_FLAG
                obj.getString(path[i])
            }
            else if (type == TYPE_ARRAY && path[i].indexOf("array")!=-1 && path[i].indexOf('.')!=-1){
                val array = JSONArray(last)
                val index : Int = path[i].substring(path[i].indexOf('.')+1).toInt()
                if (array.length()<=index) return DATA_NOT_FOUND_FLAG
                array.getString(index)
            }
            else {
                last.removePrefix("\"")
                last.removeSuffix("\"")
                return last
            }

            last = temp
        }
        return last
    }

    fun list(pathData: String = "") : List<String>{
        val type = getType(pathData)
        val result = ArrayList<String>()
        if (type == TYPE_OBJECT){
            val keys = JSONObject(get(pathData)).keys()
            while (keys.hasNext()){
                result.add(keys.next())
            }
        }
        else if (type == TYPE_ARRAY){
            val array = JSONArray(get(pathData))
            for (i in 0 until array.length()){
                result.add("array.$i")
            }
        }

        return result
    }

    fun getSize(pathData: String = "") : Int{
        val data = get(pathData)
        val type = getTypeWithPlainText(data)
        if (type == TYPE_OBJECT){
            return JSONObject(data).length()
        }
        else if (type == TYPE_ARRAY){
            return JSONArray(data).length()
        }
        else {
            return 0
        }
    }

    fun getType(pathData: String) : Int{
        return getTypeWithPlainText(get(pathData))
    }

    private fun getTypeWithPlainText(text : String) : Int{
        if (text.isEmpty()) return TYPE_VALUE
        when(text.first()){
            '{' -> return TYPE_OBJECT
            '[' -> return TYPE_ARRAY
            else -> return TYPE_VALUE
        }
    }

    override fun toString(): String = JSONContent
}