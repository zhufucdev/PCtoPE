package com.zhufucdev.pctope.activities

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

import com.zhufucdev.pctope.R

class AboutActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val version = findViewById<TextView>(R.id.about_version_text)
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

        val visitGithub = findViewById<ImageView>(R.id.visit_github)
        visitGithub.setOnClickListener(this)

        val visitCoolapk = findViewById<ImageView>(R.id.visit_coolapk)
        visitCoolapk.setOnClickListener(this)

        val show = AnimationUtils.loadAnimation(this, R.anim.cards_show)
        val icon = findViewById<ImageView>(R.id.about_icon)
        icon.visibility = View.INVISIBLE
        Handler().postDelayed({icon.startAnimation(show)},500)
        show.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                icon.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {

            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })

        supportActionBar!!.setTitle(R.string.nav_about)
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