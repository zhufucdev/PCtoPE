package com.zhufucdev.pctope.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zhufu on 17-9-23.
 */

class mLog {
    companion object {
        private var logs = ArrayList<String>()
        fun i(tag: String, msg: String) {
            Log.i(tag, msg)

            val SDF = SimpleDateFormat("hh:mm:ss")
            val date = SDF.format(Date())
            logs.add("[$date] $tag : $msg")
            logCountChangeListener!!.onLogChange()
        }

        fun d(tag: String, msg: String) {
            Log.d(tag, msg)
        }

        fun getI(): List<String> = logs.toList()
        fun getLogsCount(): Int = logs.size

        interface OnLogCountChangeListener {
            fun onLogChange()
        }

        private var logCountChangeListener: OnLogCountChangeListener =
            object : OnLogCountChangeListener {
                override fun onLogChange() {}
            }

        fun setOnLogCountChangeListener(listener: OnLogCountChangeListener) {
            logCountChangeListener = listener
        }
    }
}