package com.zhufucdev.pctope.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.pctope.R
import com.zhufucdev.pctope.utils.mLog

class ShowLogActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_log)

        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        if (!pref.getBoolean("hasLogDialogShown", false)) {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            dialog.setTitle(R.string.logs)
            dialog.setMessage(R.string.use_of_log)
            dialog.setPositiveButton(R.string.confirm) { _, _ ->
                val editor = pref.edit()
                editor.putBoolean("hasLogDialogShown", true)
                editor.apply()
            }
            dialog.show()
        }


        buildLogs()

        mLog.setOnLogCountChangeListener(object : mLog.Companion.OnLogCountChangeListener {
            override fun onLogChange() {
                buildLogs()
            }
        })

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.logs)
    }

    fun buildLogs() {
        val textView = findViewById<TextView>(R.id.log_textview)
        val logs = StringBuilder()
        for (i in 0 until mLog.getLogsCount())
            logs.append(mLog.getI()[i] + "\n")

        val logString = logs.toString()
        if (logString == "") {
            textView.gravity = Gravity.CENTER
            textView.textSize = 25f
            textView.setText(R.string.nothing)
            textView.setOnClickListener(null)
        } else {
            textView.gravity = Gravity.START
            textView.textSize = 16f
            textView.text = logString

            textView.setOnClickListener {
                val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipBoard.setPrimaryClip(ClipData.newPlainText("LogCopied", logString))
                Snackbar.make(textView, R.string.copied, Snackbar.LENGTH_SHORT).show()
            }
        }

        val scroll = findViewById<ScrollView>(R.id.log_scroll)
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}
