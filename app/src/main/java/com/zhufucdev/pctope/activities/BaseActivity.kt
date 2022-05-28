package com.zhufucdev.pctope.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.zhufucdev.pctope.collectors.ActivityCollector
import com.zhufucdev.pctope.R
import com.zhufucdev.pctope.utils.myContextWrapper
import java.io.FileNotFoundException

/**
 * Created by zhufu on 8/17/17.
 */

open class BaseActivity : AppCompatActivity() {
    internal val permission =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            )
        else
            arrayOf(Manifest.permission.READ_PHONE_STATE)

    val isGranted: Boolean
        get() = permission.all {
            ContextCompat.checkSelfPermission(this, it) ==
                    PackageManager.PERMISSION_GRANTED
        } && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true

    internal var navigationView: NavigationView? = null
    internal var drawerLayout: DrawerLayout? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        //Collector
        ActivityCollector.addActivity(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(myContextWrapper(newBase).wrap())
    }

    override fun onStart() {
        super.onStart()

        val thisClass = javaClass
        if (thisClass == MainActivity::class.java) {
            //if now the context is main or compression
            if (thisClass == MainActivity::class.java) {
                navigationView!!.setCheckedItem(R.id.nav_manager)
            }

            //for header image
            var ifImageExists = true

            var content: Bitmap? = null
            try {
                val inputStream = openFileInput("header_image.png")
                content = BitmapFactory.decodeStream(inputStream)
            } catch (e: FileNotFoundException) {
                //e.printStackTrace();
                ifImageExists = false
            }

            var imageView: ImageView? = null
            val layout = navigationView!!.getHeaderView(0)
            imageView = layout.findViewById<ImageView>(R.id.drawer_header_image)

            if (ifImageExists) {
                imageView.setImageBitmap(content)
                //imageView.setImageResource(0);
            } else {
                imageView.setImageBitmap(null)
                imageView.setImageResource(R.drawable.material_design_4)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        ActivityCollector.removeActivity(this)
    }

}
