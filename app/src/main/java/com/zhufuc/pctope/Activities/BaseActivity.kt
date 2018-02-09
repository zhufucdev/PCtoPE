package com.zhufuc.pctope.Activities

import android.app.ActivityOptions
import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.transition.Transition
import android.transition.TransitionInflater
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView

import com.zhufuc.pctope.Collectors.ActivityCollector
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.myContextWrapper
import top.wefor.circularanim.CircularAnim

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Locale

/**
 * Created by zhufu on 8/17/17.
 */

open class BaseActivity : AppCompatActivity() {

    internal var isGranted: Boolean = true

    internal var navigationView: NavigationView? = null
    internal var drawerLayout: DrawerLayout? = null

    var isMainActivity : Boolean = false
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        //Collector
        ActivityCollector.addActivity(this)
        Log.i("Activity","On Create ${this.javaClass.simpleName} for id ${this.taskId}")
        isMainActivity  = this.javaClass == MainActivity::class.java
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(myContextWrapper(newBase).wrap())
    }

    override fun onStart() {
        super.onStart()

        val thisClass = javaClass
        if (thisClass == MainActivity::class.java || thisClass == PCtoPEEditor::class.java) {
            //if now the context is main or editor
            if (isMainActivity) {
                navigationView!!.setCheckedItem(R.id.nav_manager)
            }
            else{
                navigationView!!.setCheckedItem(R.id.nav_pctope_editor)
            }

            navigationView!!.setNavigationItemSelectedListener { item ->
                drawerLayout!!.closeDrawer(GravityCompat.START)
                var mIntent = Intent()
                val color : Int = if (isMainActivity) R.color.colorPrimary else R.color.white
                when (item.itemId) {
                    R.id.nav_pctope_editor -> {
                        mIntent = Intent(this,PCtoPEEditor::class.java)
                    }
                    R.id.nav_manager -> {
                        mIntent = Intent(this,MainActivity::class.java)
                        mIntent.putExtra("isFromShortcut",false)
                    }
                    R.id.nav_settings -> {
                        mIntent = Intent(this, SettingsActivity::class.java)
                    }
                    R.id.nav_about -> {
                        mIntent = Intent(this, AboutActivity::class.java)
                    }
                    R.id.nav_log -> {
                        mIntent = Intent(this, ShowLogActivity::class.java)
                    }
                }
                if (item.itemId == R.id.nav_pctope_editor && thisClass == PCtoPEEditor::class.java)
                    return@setNavigationItemSelectedListener true
                else if (item.itemId == R.id.nav_manager && thisClass == MainActivity::class.java)
                    return@setNavigationItemSelectedListener true
                CircularAnim.fullActivity(this,findViewById(R.id.toolbar))
                        .duration(450)
                        .colorOrImageRes(color)
                        .go {
                            startActivity(mIntent)
                        }
                true
            }

            //for header image
            var ifImageExists = true

            var content: Bitmap? = null
            try {
                val inputStream = openFileInput("header_image.png")
                content = BitmapFactory.decodeStream(inputStream)
            } catch (e: Throwable) {
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
        if (navigationView!=null)
            navigationView!!.setOnClickListener(null)
        ActivityCollector.removeActivity(this)
    }

}
