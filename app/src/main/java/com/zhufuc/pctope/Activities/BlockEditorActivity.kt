package com.zhufuc.pctope.Activities

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.zhufuc.pctope.Adapters.BlocksAdapter
import com.zhufuc.pctope.Interf.SpacesItemDecoration
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.Block
import com.zhufuc.pctope.Utils.Textures
import java.io.File

class BlockEditorActivity : BaseActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter : BlocksAdapter
    var blockList = ArrayList<Block>()
    lateinit var textures : Textures
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_editor)
        recyclerView = findViewById(R.id.recycler_view)
        loadingMsg = findViewById(R.id.loading_msg)

        class LoadingTask : AsyncTask<Void,Int,Boolean>(){
            override fun onPreExecute() {
                super.onPreExecute()
                showLoading()
            }

            override fun doInBackground(vararg params: Void?): Boolean {
                textures = Textures(File(intent.getStringExtra("path")))
                blockList.addAll(textures.getBlocks()!!)
                adapter = BlocksAdapter(,{

                })
                return true
            }

            override fun onPostExecute(result: Boolean?) {
                super.onPostExecute(result)
                hideLoading()
                recyclerView.adapter = adapter
                recyclerView.layoutManager = StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL)
                recyclerView.addItemDecoration(SpacesItemDecoration(18))
            }

        }
        LoadingTask().execute()

    }

    lateinit var loadingMsg : LinearLayout
    fun showLoading(){
        loadingMsg.visibility = View.VISIBLE
    }
    fun hideLoading(){
        loadingMsg.visibility = View.GONE
    }
}