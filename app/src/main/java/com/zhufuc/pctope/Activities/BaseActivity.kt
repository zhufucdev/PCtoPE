package com.zhufuc.pctope.Activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.transition.Transition
import android.transition.TransitionInflater
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView

import com.zhufuc.pctope.Collectors.ActivityCollector
import com.zhufuc.pctope.R

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Locale

/**
 * Created by zhufu on 8/17/17.
 */

open class BaseActivity : AppCompatActivity() {

    internal var isGranted: Boolean = false

    internal var navigationView: NavigationView? = null
    internal var drawerLayout: DrawerLayout? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        //Translate animation
        //Window window = getWindow();
        //window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        //Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.fade);
        //window.setEnterTransition(fade);
        //window.setReenterTransition(fade);
        //window.setExitTransition(fade);

        //Collector
        ActivityCollector.addActivity(this)
        //Language
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        setLanguage(settings.getString("pref_language", "auto"))

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
            imageView = layout.findViewById<View>(R.id.drawer_header_image) as ImageView

            if (ifImageExists) {
                imageView.setImageBitmap(content)
                //imageView.setImageResource(0);
            } else {
                imageView.setImageBitmap(null)
                imageView.setImageResource(R.drawable.material_design_4)
            }
        }

    }

    protected fun setLanguage(language: String?) {
        var resources = applicationContext.resources
        var configuration = resources.configuration
        var metrics = resources.displayMetrics
        when (language) {
            "en" -> configuration.setLocale(Locale.ENGLISH)
            "ch" -> configuration.setLocale(Locale.CHINESE)
            else -> configuration.setLocale(SystemLanguage())
        }
        resources.updateConfiguration(configuration, metrics)
    }

    fun SystemLanguage(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            resources.configuration.locales.get(0)
        else
            resources.configuration.locale
    }

    override fun onDestroy() {
        super.onDestroy()

        ActivityCollector.removeActivity(this)
    }

}
