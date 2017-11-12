package com.zhufuc.pctope.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.zhufuc.pctope.Collectors.ActivityCollector

import com.zhufuc.pctope.R

class UserBugReport : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_bug_report)
        val confirm = findViewById<Button>(R.id.user_report_confirm)
        val sc = findViewById<Switch>(R.id.user_report_restart)
        sc.isChecked = true

        confirm.setOnClickListener {
            Toast.makeText(this@UserBugReport, R.string.thanks, Toast.LENGTH_LONG).show()

            if (sc.isChecked) {
                val intent = Intent(this@UserBugReport, FirstActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

    }
}
