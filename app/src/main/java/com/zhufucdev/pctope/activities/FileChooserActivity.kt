package com.zhufucdev.pctope.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.pctope.adapters.FileChooserAdapter

import com.zhufucdev.pctope.R
import java.io.File

class FileChooserActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_chooser)
        init()
    }

    lateinit var adapter: FileChooserAdapter
    val data = Intent()
    private fun init() {
        val externalRoot = Environment.getExternalStorageDirectory().path
        val recyclerView = findViewById<RecyclerView>(R.id.chooser_act_recycler)
        val intent = intent
        val dataCommit = intent.getStringArrayExtra("format")
        val list: List<String> = if (dataCommit == null) {
            mutableListOf("png", "jpg")
        } else {
            dataCommit.toList()
        }

        adapter = FileChooserAdapter(externalRoot, list)
        adapter.setOnItemClickListener(object : FileChooserAdapter.OnItemClickListener {
            override fun onClick(view: View, data: Intent) {
                val file = File(data.getStringExtra("path"))
                if (file.isFile) {
                    data.putExtra("path", file.path)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
                supportActionBar!!.subtitle = adapter.getPath()
            }

        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL)

        val fab = findViewById<FloatingActionButton>(R.id.chooser_act_upper)
        supportActionBar!!.subtitle = adapter.getPath()
        fab.setOnClickListener {
            if (!adapter.upLevel(externalRoot))
                Snackbar.make(it, R.string.non_upper_level, Snackbar.LENGTH_SHORT).show()
            supportActionBar!!.subtitle = adapter.getPath()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.choosing_alert)
    }


    private fun cancelOperation() {
        data.putExtra("path", "")
        setResult(Activity.RESULT_CANCELED, data)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        cancelOperation()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        cancelOperation()
        super.onBackPressed()
    }
}
