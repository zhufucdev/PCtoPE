package com.zhufuc.pctope.Activities

import android.os.AsyncTask
import android.os.Bundle
import com.zhufuc.pctope.Adapters.CardLayoutActivity
import com.zhufuc.pctope.Adapters.JSONEditingAdapter
import com.zhufuc.pctope.Adapters.JSONShowingAdapter
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.TextureCompat
import com.zhufuc.pctope.Utils.Textures
import com.zhufuc.pctope.Utils.mJSON
import org.json.JSONObject
import java.io.File

class JSONEditActivity : CardLayoutActivity() {

    var id : Int = 0
    lateinit var file : File
    var JSON : mJSON? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getIntExtra("id",0)

        file = File(intent.getStringExtra("path"))
        JSON = mJSON(file.readText())

        initActivity()
    }

    fun initActivity(){
        class LoadTask : AsyncTask<Void,Int,Int>(){
            override fun onPreExecute() {
                super.onPreExecute()
                mode = MODE_LOADING
                showLoading()
            }

            override fun doInBackground(vararg params: Void?): Int {
                if (id == TextureCompat.JSON_MANIFEST){
                    val tmp = ArrayList<Textures.JSONInfo>()
                    TextureCompat.MANIFEST.NeededItems.forEach {
                        tmp.add(Textures.JSONInfo(it.id,it.title,JSON!!.get(it.getPath())))
                    }

                    adapterForNeededCard = JSONShowingAdapter(tmp.toList())

                    val overrideItem = JSONObject()
                    val item = JSONObject()
                    item.put(getString(R.string.to_overwrite_header),getString(R.string.to_overwrite_header_subtitle))
                    overrideItem.put("header",item)
                    adapterForExtraCard = JSONEditingAdapter(JSON!!,"",overrideItem,false)

                    return MODE_TWO_CARDS
                }

                return 0
            }

            override fun onPostExecute(result: Int?) {
                super.onPostExecute(result)
                mode = result!!
            }

        }
        LoadTask().execute()
    }
}
