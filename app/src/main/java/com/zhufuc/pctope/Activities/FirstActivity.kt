package com.zhufuc.pctope.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.widget.FrameLayout

import com.tencent.bugly.crashreport.CrashReport
import com.zhufuc.pctope.R

import java.util.ArrayList


class FirstActivity : BaseActivity() {

    private val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1){
            isGranted = (grantResults.all { it == PackageManager.PERMISSION_GRANTED }  )
            init()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    lateinit var toolbar : android.support.v7.widget.Toolbar
    lateinit var frame : FrameLayout
    private var needsToDoNext = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        toolbar = findViewById(R.id.first_toolbar)
        frame = findViewById(R.id.first_frame)
        Handler().postDelayed({
            Snackbar.make(frame,R.string.click_to_jump,Snackbar.LENGTH_INDEFINITE).show()
            var lastTime : Long = 0
            var i = 0
            frame.setOnClickListener {
                if (i == 0) lastTime = System.currentTimeMillis()

                i++
                if (System.currentTimeMillis() - lastTime >= 500) i = 0
                else if (i>=3) onActivityResult(0,0,null)
                lastTime = System.currentTimeMillis()

                if (i>3) i=0
            }
        },5000)

        initBugprt()

        //request permissions
        if (needsToDoNext)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ActivityCompat.requestPermissions(this@FirstActivity, permission, 1)
            else
                init()

    }

    private fun init() {
        val preferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val isbooted = preferences.getBoolean("isbooted", false)
        //is first boot
        if (!isbooted) {
            //for Tutorial Activity
            updateDefaults()
            editor.putBoolean("isbooted", true)
            editor.apply()
        }
        val main = Intent(this@FirstActivity, MainActivity::class.java)
        main.putExtra("isGranted", isGranted)
        main.putExtra("isFromShortcut",false)
        startActivity(main)
        finish()
    }

    private fun updateDefaults(){
        val settings = getSharedPreferences("com.zhufuc.pctope_preferences", Context.MODE_PRIVATE)
        val settingsEditor = settings.edit()
        settingsEditor.putString("pref_conversion_style","new")
        settingsEditor.putString("pref_language","auto")
        settingsEditor.apply()
    }

    private fun initBugprt() {
        val preferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (preferences.getBoolean("ifHasBrokenDownLastStart", false)) {
            val intent = Intent(this@FirstActivity, UserBugReport::class.java)
            needsToDoNext = false
            startActivity(intent)
            finish()
        }
        editor.putBoolean("ifHasBrokenDownLastStart", false)
        editor.apply()

        val userStrategy = CrashReport.UserStrategy(applicationContext)
        userStrategy.setCrashHandleCallback(object : CrashReport.CrashHandleCallback() {
            override fun onCrashHandleStart(p0: Int, p1: String?, p2: String?, p3: String?): MutableMap<String, String> {
                editor.putBoolean("ifHasBrokenDownLastStart", true)
                editor.apply()
                return super.onCrashHandleStart(p0, p1, p2, p3)
            }
        })

        CrashReport.initCrashReport(applicationContext,"e79f664cfd",ApplicationInfo.FLAG_DEBUGGABLE == 1,userStrategy)
    }
}