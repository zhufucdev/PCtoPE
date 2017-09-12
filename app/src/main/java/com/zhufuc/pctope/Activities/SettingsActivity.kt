package com.zhufuc.pctope.Activities


import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.SwitchPreference
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.MenuItem
import android.support.v4.app.NavUtils
import android.view.View
import android.widget.Toast

import com.zhufuc.pctope.Collectors.ActivityCollector
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.CompressImage
import com.zhufuc.pctope.Utils.GetPathFromUri4kitkat

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Locale

import java.lang.String.*

class SettingsActivity : AppCompatPreferenceActivity() {

    private val changeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        val key = preference.key
        if (key == "pref_language") {
            Toast.makeText(this@SettingsActivity, R.string.rebooting, Toast.LENGTH_LONG).show()
            ActivityCollector.finishAll()
            startActivity(Intent().setClass(this@SettingsActivity, FirstActivity::class.java))
        }
        true
    }

    private var listPreference: ListPreference? = null
    private var customDrawer: Preference? = null
    private var clearDrawer: Preference? = null
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        ActivityCollector.addActivity(this)

        addPreferencesFromResource(R.xml.perf_main)
        val bar = supportActionBar
        bar!!.setDisplayHomeAsUpEnabled(true)
        listPreference = findPreference("pref_language") as ListPreference
        listPreference!!.onPreferenceChangeListener = changeListener

        customDrawer = findPreference("pref_drawer_button")
        customDrawer!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, 1)
            false
        }

        clearDrawer = findPreference("pref_drawer_clear")
        refreshClearButton()
        clearDrawer!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            deleteFile("header_image.png")
            refreshClearButton()
            false
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val uri = data.data
                val path = GetPathFromUri4kitkat.getPath(this, uri)
                if (path == null) {
                    Toast.makeText(this, "File not found!", Toast.LENGTH_LONG).show()
                    return
                }
                val imagePath = File(path)
                if (!imagePath.exists()) {
                    Toast.makeText(this, "File not found!", Toast.LENGTH_LONG).show()
                    return
                }

                var outputStream: FileOutputStream? = null
                var bitmap: Bitmap? = BitmapFactory.decodeFile(path)
                if (bitmap == null) {
                    Toast.makeText(this, "File isn't an image!", Toast.LENGTH_LONG).show()
                    return
                }

                val width = bitmap.width
                val height = bitmap.height
                if (width + height > 1024) {
                    var compressWidthPercent = 1
                    var compressHeightPercent = 1
                    while (width / compressWidthPercent > 512)
                        compressWidthPercent++
                    while (height / compressHeightPercent > 512)
                        compressHeightPercent++

                    bitmap = CompressImage.getBitmap(bitmap, height / compressHeightPercent, width / compressHeightPercent)
                }

                try {
                    outputStream = openFileOutput("header_image.png", Context.MODE_PRIVATE)
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                } catch (e: FileNotFoundException) {
                    Toast.makeText(this, "File not found!", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

                finish()
            }

        }

    }

    fun refreshClearButton() {
        val image = File(this.filesDir.toString() + "/header_image.png")
        clearDrawer!!.isEnabled = image.exists()
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
}
