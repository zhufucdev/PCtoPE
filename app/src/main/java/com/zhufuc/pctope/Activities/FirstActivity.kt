package com.zhufuc.pctope.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.os.Bundle
import android.util.Log
import com.tencent.bugly.CrashModule

import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.crash.CrashDetailBean
import com.zhufuc.pctope.R

import java.util.ArrayList

import za.co.riggaroo.materialhelptutorial.TutorialItem
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity
import java.nio.charset.Charset


class FirstActivity : BaseActivity() {

    private val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)

    private fun getTutorialItems(context: Context): ArrayList<TutorialItem> {
        val item1 = TutorialItem(context.getString(R.string.tutorial_welcome), context.getString(R.string.tutorial_wlcome_subtitle), R.color.amber_primary, 0, R.drawable.ic_launcher)
        val item2 = TutorialItem(context.getString(R.string.tutorial_step1), context.getString(R.string.tutorial_step1_subtitle), R.color.app_blue_dark, R.drawable.for_step2_fab, R.drawable.for_step2_background)
        val item3 = TutorialItem(context.getString(R.string.tutorial_step2), context.getString(R.string.tutorial_step2_subtitle), R.color.app_green, R.drawable.for_step3)
        val item4 = TutorialItem(context.getString(R.string.tutorial_step3), context.getString(R.string.tutorial_step3_subtitle), R.color.app_red, R.drawable.for_step4)
        val item5 = TutorialItem(context.getString(R.string.tutorial_step4), context.getString(R.string.tutorial_step4_subtitle), R.color.deep_purple_primary, R.drawable.for_step5)
        val item6 = TutorialItem(context.getString(R.string.tutorial_step5), context.getString(R.string.tutorial_step5_subtitle), R.color.orange_primary, R.drawable.for_step6)
        val item7 = TutorialItem(context.getString(R.string.tutorial_hope), context.getString(R.string.tutorial_hope_subtitle), R.color.yellow_primary_dark, R.drawable.happy_face)
        val tutorialItems = ArrayList<TutorialItem>()
        tutorialItems.add(item1)
        tutorialItems.add(item2)
        tutorialItems.add(item3)
        tutorialItems.add(item4)
        tutorialItems.add(item5)
        tutorialItems.add(item6)
        tutorialItems.add(item7)
        return tutorialItems
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            val main = Intent(this@FirstActivity, MainActivity::class.java)
            main.putExtra("isGranted", isGranted)
            main.putExtra("isFromShortcut",false)
            startActivity(main)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1){
            isGranted = (ContextCompat.checkSelfPermission(this@FirstActivity, permission[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this@FirstActivity, permission[1]) == PackageManager.PERMISSION_GRANTED)
            InitTutorial()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private var needsToDoNext = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_first)

        initBugprt()

        //request permissions
        if (needsToDoNext)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ActivityCompat.requestPermissions(this@FirstActivity, permission, 1)
            else
                InitTutorial()
    }

    private fun InitTutorial() {
        val preferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val isbooted = preferences.getBoolean("isbooted", false)
        //is first boot
        if (!isbooted) {
            //for Tutorial Activity
            val tutorialActivity = Intent(this@FirstActivity, MaterialTutorialActivity::class.java)
            tutorialActivity.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, getTutorialItems(this))
            startActivityForResult(tutorialActivity, 0)

            editor.putBoolean("isbooted", true)
            editor.apply()
        } else
            onActivityResult(0, 0, null)
    }

    fun initBugprt() {
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