package com.zhufuc.pctope.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.view.Gravity
import android.widget.ScrollView
import android.widget.TextView

import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.mLog

class ShowLogActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_log)

        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        if(!pref.getBoolean("hasLogDialogShown",false)){
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.logs)
            dialog.setMessage(R.string.use_of_log)
            dialog.setPositiveButton(R.string.confirm,{ dialogInterface, i ->  val editor = pref.edit()
                editor.putBoolean("hasLogDialogShown",true)
                editor.apply()
            })
            dialog.show()
        }


        buildLogs()

        mLog.setOnLogCountChangeListener(object : mLog.Companion.OnLogCountChangeListener{
            override fun onLogChange() {
                buildLogs()
            }
        })

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.logs)
    }

    fun buildLogs(){
        val textView = findViewById(R.id.log_textview) as TextView
        val logs = StringBuilder()
        for (i in 0 until mLog.getLogsCount())
            logs.append(mLog.getI()[i]+"\n")

        val logString = logs.toString()
        if (logString==""){
            textView.gravity = Gravity.CENTER
            textView.textSize = 25f
            textView.setText(R.string.nothing)
        }
        else{
            textView.gravity = Gravity.START
            textView.textSize = 16f
            textView.text = logString
        }

        textView.setOnClickListener{
            val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipBoard.primaryClip = ClipData.newPlainText("LogCopied",logString)
            Snackbar.make(textView,R.string.copied,Snackbar.LENGTH_SHORT).show()
        }

        val scroll = findViewById(R.id.log_scroll) as ScrollView
        scroll.post({ scroll.fullScroll(ScrollView.FOCUS_DOWN) })

    }
}
