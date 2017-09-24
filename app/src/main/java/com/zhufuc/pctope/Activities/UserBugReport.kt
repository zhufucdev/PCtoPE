package com.zhufuc.pctope.Activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

import com.netease.nis.bugrpt.CrashHandler
import com.zhufuc.pctope.Collectors.ActivityCollector
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.mLog

class UserBugReport : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_bug_report)
        val content = findViewById(R.id.user_report) as TextView
        val confirm = findViewById(R.id.user_report_confirm) as Button
        val sc = findViewById(R.id.user_report_restart) as Switch
        sc.isChecked = true

        confirm.setOnClickListener {
            Toast.makeText(this@UserBugReport, R.string.thanks, Toast.LENGTH_LONG).show()

            if (content.text.toString() != "") {
                val tm = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                CrashHandler.uploadUserDefineLog("用户反馈", Build.MODEL + ">" + tm.deviceId + ":" + content.text.toString())
                mLog.i("Report","User's Report is committed.")
            }
            if (sc.isChecked) {
                val intent = Intent(this@UserBugReport, FirstActivity::class.java)
                startActivity(intent)
                ActivityCollector.finishAll()
            }
            finish()
        }

    }
}
