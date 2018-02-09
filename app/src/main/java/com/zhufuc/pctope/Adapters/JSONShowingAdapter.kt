package com.zhufuc.pctope.Adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.Textures
import kotlinx.android.synthetic.main.pctope_editor_view_json.view.*
import java.io.File

/**
 * Created by zhufu on 17-12-31.
 */
class JSONShowingAdapter(var JSONList : List<Textures.JSONInfo>) : RecyclerView.Adapter<JSONShowingAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onClick(id: Int,file: File)
    }

    private var mOnItemClickListener : OnItemClickListener = object : OnItemClickListener{
        override fun onClick(id: Int, file: File) {}
    }
    fun setOnItemClickListener(l: OnItemClickListener) {
        this.mOnItemClickListener = l
    }

    init {
        Log.i("JSON","Adapter init. Count = ${JSONList.size}")
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val shapeText = itemView.findViewById<TextView>(R.id.shape_text)
        val title = itemView.findViewById<TextView>(R.id.title)
        val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
        val item_view = itemView.findViewById<LinearLayout>(R.id.json_item_view)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int){
        val shapeText = holder!!.shapeText
        val title = holder.title
        val subtitle = holder.subtitle
        val thisItem = JSONList[position]
        shapeText.text = thisItem.title[0].toUpperCase().toString()
        title.text = thisItem.getUserTitle(title.context)

        val subt = thisItem.getUserSubtitle(subtitle.context)
        subtitle.text = if (subt == "[Value]") thisItem.subtitle else thisItem.getUserSubtitle(subtitle.context)


        holder.item_view.setOnClickListener {
            mOnItemClickListener.onClick(thisItem.id,File(thisItem.getPath()))
        }
    }

    override fun getItemCount(): Int {
        return JSONList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.pctope_editor_view_json,parent,false)
        return ViewHolder(view)
    }
}