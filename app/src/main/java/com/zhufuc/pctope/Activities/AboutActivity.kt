package com.zhufuc.pctope.Activities

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

import com.zhufuc.pctope.R

class AboutActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val version = findViewById(R.id.about_version_text) as TextView
        val packageManager = packageManager
        var info: PackageInfo? = null
        try {
            info = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val versionName = info!!.versionName
        val versionCode = info.versionCode
        version.text = "PCtoPE $versionName($versionCode)"

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val visitGithub = findViewById(R.id.visit_github) as ImageView
        visitGithub.setOnClickListener(this)

        val visitCoolapk = findViewById(R.id.visit_coolapk) as ImageView
        visitCoolapk.setOnClickListener(this)

        val show = AnimationUtils.loadAnimation(this, R.anim.cards_show)
        val icon = findViewById(R.id.about_icon) as ImageView
        icon.visibility = View.INVISIBLE
        Thread(Runnable {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            runOnUiThread { icon.startAnimation(show) }
        }).start()
        show.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                icon.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {

            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    override fun onClick(view: View) {
        var uri: Uri? = null
        val site = Intent(Intent.ACTION_VIEW)
        when (view.id) {
            R.id.visit_github -> uri = Uri.parse("https://github.com/zhufucdev/PCtoPE")
            R.id.visit_coolapk -> uri = Uri.parse("https://www.coolapk.com/apk/com.zhufuc.pctope")
        }
        site.data = uri
        startActivity(site)
    }
}