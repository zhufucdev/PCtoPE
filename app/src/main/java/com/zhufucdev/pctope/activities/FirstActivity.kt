package com.zhufucdev.pctope.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.*
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.tencent.bugly.crashreport.CrashReport
import com.zhufucdev.pctope.R
import za.co.riggaroo.materialhelptutorial.TutorialItem
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity


class FirstActivity : BaseActivity() {
    private fun getTutorialItems(context: Context): ArrayList<TutorialItem> {
        val item1 = TutorialItem(
            context.getString(R.string.tutorial_welcome),
            context.getString(R.string.tutorial_wlcome_subtitle),
            R.color.amber_primary,
            0,
            R.drawable.ic_launcher
        )
        val item2 = TutorialItem(
            context.getString(R.string.tutorial_step1),
            context.getString(R.string.tutorial_step1_subtitle),
            R.color.app_blue_dark,
            R.drawable.for_step2_fab,
            R.drawable.for_step2_background
        )
        val item3 = TutorialItem(
            context.getString(R.string.tutorial_step2),
            context.getString(R.string.tutorial_step2_subtitle),
            R.color.app_green,
            R.drawable.for_step3
        )
        val item4 = TutorialItem(
            context.getString(R.string.tutorial_step3),
            context.getString(R.string.tutorial_step3_subtitle),
            R.color.app_red,
            R.drawable.for_step4
        )
        val item5 = TutorialItem(
            context.getString(R.string.tutorial_step4),
            context.getString(R.string.tutorial_step4_subtitle),
            R.color.deep_purple_primary,
            R.drawable.for_step5
        )
        val item6 = TutorialItem(
            context.getString(R.string.tutorial_step5),
            context.getString(R.string.tutorial_step5_subtitle),
            R.color.orange_primary,
            R.drawable.for_step6
        )
        val item7 = TutorialItem(
            context.getString(R.string.tutorial_hope),
            context.getString(R.string.tutorial_hope_subtitle),
            R.color.yellow_primary_dark,
            R.drawable.happy_face
        )
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
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            val main = Intent(this@FirstActivity, MainActivity::class.java)
            main.putExtra("isGranted", isGranted)
            main.putExtra("isFromShortcut", false)
            startActivity(main)
            finish()
        } else if (requestCode == 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isGranted) initTutorial()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                try {
                    startActivityForResult(
                        Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            .addCategory("android.intent.category.DEFAULT")
                            .setData(Uri.parse("package:${applicationContext.packageName}")),
                        2
                    )
                } catch (ignored: Exception) {
                    startActivityForResult(
                        Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
                        2
                    )
                }
            }
            else {
                initTutorial()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var frame: FrameLayout
    private var shouldContinue = true
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_first)

        toolbar = findViewById(R.id.first_toolbar)
        frame = findViewById(R.id.first_frame)

        Handler(Looper.getMainLooper()).postDelayed({
            Snackbar.make(frame, R.string.click_to_jump, Snackbar.LENGTH_INDEFINITE).show()
            var lastTime: Long = 0
            var i = 0
            frame.setOnClickListener {
                if (i == 0) lastTime = System.currentTimeMillis()

                i++
                if (System.currentTimeMillis() - lastTime >= 500) i = 0
                else if (i >= 3) onActivityResult(0, 0, null)
                lastTime = System.currentTimeMillis()

                if (i > 3) i = 0
            }
        }, 5000)

        initBugprt()

        //request permissions
        if (!shouldContinue)
            return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this@FirstActivity, permission, 1)
        } else {
            initTutorial()
        }
    }

    private fun initTutorial() {
        val preferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val booted = preferences.getBoolean("isbooted", false)
        //is first boot
        if (!booted) {
            //for Tutorial Activity
            val tutorialActivity = Intent(this@FirstActivity, MaterialTutorialActivity::class.java)
            tutorialActivity.putParcelableArrayListExtra(
                MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS,
                getTutorialItems(this)
            )
            startActivityForResult(tutorialActivity, 0)

            updateDefaults()
            editor.putBoolean("isbooted", true)
            editor.apply()
        } else
            onActivityResult(0, 0, null)
    }

    private fun updateDefaults() {
        val settings = getSharedPreferences("com.zhufuc.pctope_preferences", Context.MODE_PRIVATE)
        val settingsEditor = settings.edit()
        settingsEditor.putString("pref_conversion_style", "new")
        settingsEditor.putString("pref_language", "auto")
        settingsEditor.apply()
    }

    private fun initBugprt() {
        val preferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (preferences.getBoolean("ifHasBrokenDownLastStart", false)) {
            val intent = Intent(this@FirstActivity, UserBugReport::class.java)
            shouldContinue = false
            startActivity(intent)
            finish()
        }
        editor.putBoolean("ifHasBrokenDownLastStart", false)
        editor.apply()

        val userStrategy = CrashReport.UserStrategy(applicationContext)
        userStrategy.setCrashHandleCallback(object : CrashReport.CrashHandleCallback() {
            override fun onCrashHandleStart(
                p0: Int,
                p1: String?,
                p2: String?,
                p3: String?
            ): MutableMap<String, String> {
                editor.putBoolean("ifHasBrokenDownLastStart", true)
                editor.apply()
                return super.onCrashHandleStart(p0, p1, p2, p3)
            }
        })

        CrashReport.initCrashReport(
            applicationContext,
            "e79f664cfd",
            ApplicationInfo.FLAG_DEBUGGABLE == 1,
            userStrategy
        )
    }
}