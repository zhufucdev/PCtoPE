package com.zhufuc.pctope.Adapters

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.mJSON
import com.zhufuc.pctope.Utils.mJSON.Companion.TYPE_ARRAY
import com.zhufuc.pctope.Utils.mJSON.Companion.TYPE_OBJECT
import com.zhufuc.pctope.Utils.mJSON.Companion.TYPE_VALUE
import com.zhufuc.pctope.Utils.mLog
import org.json.JSONObject

/**
 * Created by zhufu on 18-1-5.
 */

class JSONEditingAdapter(val JSON: mJSON, val pathData: String = "", overrideItem : JSONObject? = null,val alwaysShowChild: Boolean) : RecyclerView.Adapter<JSONEditingAdapter.ViewHolder>() {
    var infos = ArrayList<InfoHolder>()
    init {
        infos = InfoHolder.listWithJSON(JSON,pathData)
        if (overrideItem != null){
            val keys = overrideItem.keys()
            while (keys.hasNext()){
                val key = keys.next()
                JSON.set(key,overrideItem[key].toString())
            }
        }
    }

    class InfoHolder(val type: Int,val name: String = "",val subtile : String = "",val pathData: String,var childAdapter : JSONEditingAdapter? = null){
        companion object {
            fun listWithJSON(JSON: mJSON,pathData: String) : ArrayList<InfoHolder>{
                val result = ArrayList<InfoHolder>()
                JSON.list(pathData).forEach {
                    val path = "$pathData/$it"
                    result.add(InfoHolder(JSON.getType(path),it,JSON.get(path),path))
                }
                return result
            }
        }
    }

    override fun getItemCount(): Int = infos.size

    override fun onBindViewHolder(holder: JSONEditingAdapter.ViewHolder?, position: Int) {
        val title = holder!!.title
        val shapeText = holder.shapeText
        val subtitle = holder.subtitle
        val itemView = holder.item_view
        val thisItem = infos[position]
        val childRecycler = holder.childRecycerView
        shapeText.text = thisItem.name[0].toUpperCase().toString()
        title.text = thisItem.name
        subtitle.text = if (thisItem.type == TYPE_VALUE) thisItem.subtile else if (thisItem.type == TYPE_OBJECT) itemView.context.getString(R.string.json_object) else itemView.context.getString(R.string.json_array)
        if(alwaysShowChild)
            showChild(position,childRecycler)
        val childAdapter = infos[position].childAdapter
        childRecycler.adapter = childAdapter
        childRecycler.layoutManager = LinearLayoutManager(childRecycler.context)

        itemView.setOnClickListener {
            if (alwaysShowChild) return@setOnClickListener
            if (!holder.isChildShown) {
                if (!(thisItem.type == TYPE_VALUE)) {
                    showChild(position,childRecycler)
                }
            }
            else {
                if (!(thisItem.type == TYPE_VALUE)) {
                    hideChild(position,childRecycler)
                }
            }
        }
    }

    fun showChild(position: Int,childRecycler: RecyclerView){
        childRecycler.visibility = View.VISIBLE
        var needsToRebuild = false
        if (infos[position].childAdapter == null){
            needsToRebuild = true
        }
        else if (infos[position].childAdapter!!.isEmpty()){
            needsToRebuild = true
        }
        if (needsToRebuild){
            val path = infos[position].pathData
            mLog.i("Editor.JSON","Rebuilt adapter for $path")
            infos[position].childAdapter = JSONEditingAdapter(JSON,path,null,alwaysShowChild)
        }
        if (!alwaysShowChild)
            notifyDataSetChanged()
    }

    fun hideChild(position: Int, childRecycler: RecyclerView){
        infos[position].childAdapter = null
        childRecycler.visibility = View.GONE
        notifyDataSetChanged()
    }

    fun isEmpty() : Boolean = infos.isEmpty()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): JSONEditingAdapter.ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.pctope_editor_view_json_recursion,parent,false)
        return JSONEditingAdapter.ViewHolder(view)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val shapeText = itemView.findViewById<TextView>(R.id.shape_text)
        val title = itemView.findViewById<TextView>(R.id.title)
        val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
        val item_view = itemView.findViewById<LinearLayout>(R.id.json_item_view)
        val childRecycerView = itemView.findViewById<RecyclerView>(R.id.recycle_view_recursion)

        init {
            childRecycerView.visibility = View.INVISIBLE
        }

        var isChildShown: Boolean = false
        get() = childRecycerView.visibility == View.VISIBLE
    }
}