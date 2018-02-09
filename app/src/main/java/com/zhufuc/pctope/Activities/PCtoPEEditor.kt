package com.zhufuc.pctope.Activities

import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.Space
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.zhufuc.pctope.Adapters.JSONShowingAdapter
import com.zhufuc.pctope.Adapters.mViewPagerAdapter
import com.zhufuc.pctope.Interf.SpacesItemDecoration
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.Block
import com.zhufuc.pctope.Utils.TextureCompat
import com.zhufuc.pctope.Utils.Textures
import java.io.File

class PCtoPEEditor : BaseActivity() {
    lateinit var viewPager : ViewPager
    lateinit var tabLayout : TabLayout
    lateinit var texture : Textures
    lateinit var JSONPager : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pctope_editor)
        initToolbar()
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        navigationView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_main)


        JSONPager = layoutInflater.inflate(R.layout.pctope_editor_page_json,null)
        val ImagePager = layoutInflater.inflate(R.layout.tutorial_conversion_step2,null)
        updateInformation()

        val pagerAdapter = mViewPagerAdapter(listOf(JSONPager/*,ImagePager*/),this)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        neededJSONAdapter.setOnItemClickListener(object : JSONShowingAdapter.OnItemClickListener{
            override fun onClick(id: Int, file: File) {
                val intent : Intent
                if (id == TextureCompat.JSON_BLOCKS){
                    intent = Intent(this@PCtoPEEditor,BlockEditorActivity::class.java)
                    Log.i("PCtoPE Editor","Starting Block Editor for ${texture.path}")
                    intent.putExtra("path",texture.path)
                }
                else {
                    intent = Intent(this@PCtoPEEditor,JSONEditActivity::class.java)
                    intent.putExtra("id",id)
                    intent.putExtra("path",file.path)
                }
                startActivity(intent)
            }

        })
    }

    lateinit var neededJSONAdapter : JSONShowingAdapter
    fun updateInformation(){
        val testPath = File("${Environment.getExternalStorageDirectory()}/games/com.mojang/resource_packs/Unnamed")

        val neededJSONView = JSONPager.findViewById<RecyclerView>(R.id.recycle_view_needed_json)

        texture = Textures(testPath)
        val extraDataOfManifest :String = if (texture.getManifest() != null && texture.getManifest()!!.isUsable) {"exists,path:${texture.manifestFile.path}"} else {texture.manifestFile.path}
        val extraDataOfBlocks :String = if (texture.blockFile.exists()) "exists" else ""

        neededJSONAdapter = JSONShowingAdapter(listOf(Textures.JSONInfo(TextureCompat.JSON_MANIFEST,"Manifest","",extraDataOfManifest),
                Textures.JSONInfo(TextureCompat.JSON_BLOCKS,"Blocks","",extraDataOfBlocks)))
                neededJSONView.adapter = neededJSONAdapter
                neededJSONView.layoutManager = LinearLayoutManager(this)

        val needJSONInfo : ImageView = JSONPager.findViewById(R.id.info_needed_json)
        needJSONInfo.setOnClickListener{
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage(R.string.message_needed_json)
            dialog.setNegativeButton(R.string.confirm,null)
            dialog.show()
        }
    }

    fun initToolbar(){
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar ?: return
        supportActionBar.title = ""
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.setHomeAsUpIndicator(R.drawable.menu_black)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            android.R.id.home -> drawerLayout!!.openDrawer(GravityCompat.START)
        }
        return true
    }

    override fun onBackPressed() {
        if (!drawerLayout!!.isDrawerOpen(GravityCompat.START))
            super.onBackPressed()
        else{
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }
    }
}
