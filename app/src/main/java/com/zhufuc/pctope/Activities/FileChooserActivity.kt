package com.zhufuc.pctope.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.MenuItem
import android.view.View
import com.zhufuc.pctope.Adapters.FileChooserAdapter

import com.zhufuc.pctope.R
import java.io.File

class FileChooserActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_chooser)
        init()
    }

    lateinit var adapter : FileChooserAdapter
    val data = Intent()
    private fun init(){
        val externalRoot = Environment.getExternalStorageDirectory().path
        val recyclerView = findViewById(R.id.chooser_act_recycler) as RecyclerView

        adapter = FileChooserAdapter(externalRoot, mutableListOf("png","jpg"))
        adapter.setOnItemClickListener(object : FileChooserAdapter.OnItemClickListener{
            override fun onClick(view: View, data: Intent) {
                val file = File(data.getStringExtra("path"))
                if (file.isFile){
                    data.putExtra("path",file.path)
                    setResult(Activity.RESULT_OK,data)
                    finish()
                }
                supportActionBar!!.subtitle = adapter.getPath()
            }

        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2,OrientationHelper.VERTICAL)

        val fab = findViewById(R.id.chooser_act_upper) as FloatingActionButton
        supportActionBar!!.subtitle = adapter.getPath()
        fab.setOnClickListener({
            if (!adapter.upLevel(externalRoot))
                Snackbar.make(it,R.string.non_upper_level,Snackbar.LENGTH_SHORT).show()
            supportActionBar!!.subtitle = adapter.getPath()
        })

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.choosing_alert)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        data.putExtra("path","")
        setResult(Activity.RESULT_CANCELED,data)
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onOptionsItemSelected(null)
        super.onBackPressed()
    }
}
