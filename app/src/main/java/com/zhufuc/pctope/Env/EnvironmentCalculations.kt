package com.zhufuc.pctope.Env

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import com.zhufuc.pctope.R

/**
 * Created by zhufu on 1/29/18.
 */
object EnvironmentCalculations {
    fun <T : Any> getNoRepeat(items: ArrayList<T>) : ArrayList<T>{
        val fix = ArrayList<T>()

        for (item in items) {
            val isRepeat = fix.contains(item)
            if (isRepeat)
                continue
            fix.add(item)
        }

        fix.forEach{
            print(it)
        }

        return fix
    }

    fun MakeErrorDialog(msg: String,context: Context) {
        //make up a error dialog
        val activity = context as Activity

        val error_dialog = AlertDialog.Builder(context)
        error_dialog.setTitle(R.string.error)
        error_dialog.setMessage(context.getString(R.string.error_dialog) + msg)
        error_dialog.setIcon(R.drawable.alert_octagram)
        error_dialog.setCancelable(false)
        error_dialog.setPositiveButton(R.string.close){ dialogInterface: DialogInterface, i: Int -> activity.finish() }
        error_dialog.setNegativeButton(R.string.copy) { dialogInterface, i ->
            val copy = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            copy.primaryClip = ClipData.newPlainText("ErrorMessage",msg)
        }.show()
    }
}