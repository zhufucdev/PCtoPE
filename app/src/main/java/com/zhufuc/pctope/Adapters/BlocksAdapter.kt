package com.zhufuc.pctope.Adapters

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.Block
import com.zhufuc.pctope.View.TagAdapter
import com.zhufuc.pctope.View.Tagger

/**
 * Created by zhufu on 2/3/18.
 * @param data Call when loading data of blocks
 */
class BlocksAdapter(val totalItemCount: Int,data: (Int) -> Block) : RecyclerView.Adapter<BlocksAdapter.ViewHolder>() {
    var mList = ArrayList<Block>()
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.pctope_editor_block_info,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val block = mList[position]
        holder!!.title = mList[position].name

        val tags = ArrayList<TagAdapter.TagInfo>()
        if (block.blockShape == "invisible")
            tags.add(TagAdapter.TagInfo("visibility","invisible",R.color.google_blue))
        else
            tags.add(TagAdapter.TagInfo("visibility","visible",R.color.green_primary))
        holder.tagger.addAll(tags)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView){
        private val titleView: TextView = itemView!!.findViewById(R.id.block_name)
        var title: String
        get() = titleView.text.toString()
        set(value) { titleView.text = value }

        val tagger : Tagger = itemView!!.findViewById(R.id.tags)
        val icons : RecyclerView = itemView!!.findViewById(R.id.icons)
        val delButton : AppCompatImageView = itemView!!.findViewById(R.id.delete)
    }
}