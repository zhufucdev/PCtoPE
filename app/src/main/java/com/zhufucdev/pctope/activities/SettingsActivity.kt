package com.zhufucdev.pctope.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.util.Log
import android.view.MenuItem
import android.widget.Toast

import com.zhufucdev.pctope.collectors.ActivityCollector
import com.zhufucdev.pctope.R
import com.zhufucdev.pctope.utils.CompressImage
import com.zhufucdev.pctope.utils.DeleteFolder
import com.zhufucdev.pctope.utils.myContextWrapper
import java.io.*

import java.nio.channels.FileChannel
import kotlin.math.roundToInt

class SettingsActivity : AppCompatPreferenceActivity() {

    private val changeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        val key = preference.key
        if (key == "pref_language") {
            Toast.makeText(this@SettingsActivity, R.string.rebooting, Toast.LENGTH_LONG).show()
            ActivityCollector.finishAll()
            startActivity(Intent().setClass(this@SettingsActivity, FirstActivity::class.java))
        }
        else if (key == "pref_conversion_style"){
            Log.i("Settings","Conversion UI Styles have been changed.")
        }
        true
    }

    private var listPreference: ListPreference? = null
    private var customDrawer: Preference? = null
    private var clearDrawer: Preference? = null
    private var clearCache: Preference? = null
    lateinit var conversionStyle : ListPreference
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        ActivityCollector.addActivity(this)

        addPreferencesFromResource(R.xml.perf_main)
        val bar = supportActionBar
        bar!!.setDisplayHomeAsUpEnabled(true)
        listPreference = findPreference("pref_language") as ListPreference
        listPreference!!.onPreferenceChangeListener = changeListener
        conversionStyle = findPreference("pref_conversion_style") as ListPreference
        conversionStyle.onPreferenceChangeListener = changeListener

        customDrawer = findPreference("pref_drawer_button")
        customDrawer!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(this@SettingsActivity,FileChooserActivity::class.java)

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


        clearCache = findPreference("pref_clear_cache")
        Thread { refreshCacheClear() }.start()

        clearCache!!.onPreferenceClickListener = Preference.OnPreferenceClickListener{
            Thread {
                externalCacheDir?.path?.let { cache -> DeleteFolder.delete(cache) }
                refreshCacheClear()
            }.start()

            clearCache!!.isEnabled = false
            true
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val path = data.getStringExtra("path")
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

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(myContextWrapper(newBase).wrap())
    }

    private fun refreshClearButton() {
        val image = File(this.filesDir.toString() + "/header_image.png")
        clearDrawer!!.isEnabled = image.exists()
    }

    private fun refreshCacheClear(){
        runOnUiThread { clearCache!!.setSummary(R.string.calculating) }
        val size = externalCacheDir?.let { getFolderTotalSize(it.path) } ?: 0
        runOnUiThread {
            clearCache!!.isEnabled = size != 0L
            clearCache!!.summary =
                (if (size == 0L) "0B" else "${(size / (1024 * 1024f)).roundToInt()}MB")
        }
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

    private fun getFolderTotalSize(path: String): Long {
        val files = File(path).listFiles()
        var size: Long = 0
        for (f in files)
            if (f.exists()) {
                if (f.isFile) {
                    var fc: FileChannel? = null
                    var inputStream: FileInputStream? = null
                    try {
                        inputStream = FileInputStream(f)
                        fc = inputStream.channel
                        size += fc!!.size()
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                } else
                    size += getFolderTotalSize(f.path)
            }

        return size
    }
}
