package com.zhufuc.pctope.Adapters

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.zhufuc.pctope.R

import java.io.File
import java.util.ArrayList
import java.util.Collections

/**
 * Created by zhufu on 17-9-8.
 */

class FileChooserAdapter(private var root: String?) : RecyclerView.Adapter<FileChooserAdapter.ViewHolder>() {
    private var list = ArrayList<File>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var folderView: CardView
        var zipFileView: CardView

        init {
            folderView = itemView.findViewById(R.id.folder_layout)
            zipFileView = itemView.findViewById(R.id.zip_file_layout)
        }
    }

    init {
        initData()
    }

    private fun initData() {
        val fileList = ArrayList<File>()
        val folderList = ArrayList<File>()
        list = ArrayList()
        val files = File(root!!).listFiles()
        for (f in files)
            if (f.exists()) {
                if (f.isFile) {
                    if (!f.name.contains(".zip"))
                        continue
                    fileList.add(f)
                } else
                    folderList.add(f)
            }

        Collections.sort(folderList)
        Collections.sort(fileList)
        list.addAll(folderList)
        list.addAll(fileList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.file_chooser, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = list[position]
        val name: TextView

        if (file.isFile) {
            holder.zipFileView.visibility = View.VISIBLE
            holder.folderView.visibility = View.GONE
            holder.zipFileView.tag = file.path
            name = holder.zipFileView.findViewById(R.id.file_title)
            name.text = file.name

            holder.zipFileView.setOnClickListener(onClickListener)
        } else {
            holder.zipFileView.visibility = View.GONE
            holder.folderView.visibility = View.VISIBLE
            holder.folderView.tag = file.path
            name = holder.folderView.findViewById(R.id.folder_title)
            name.text = file.name

            holder.folderView.setOnClickListener(onClickListener)
        }

    }

    internal var onClickListener: View.OnClickListener = View.OnClickListener { view ->
        val fileClicked = view.tag as String
        val data: Intent = Intent().putExtra("path",fileClicked)

        onItemClickListener?.onClick(view,data)

        if (File(fileClicked).isFile) return@OnClickListener

        root = fileClicked
        initData()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    fun upLevel(toStop : String){
        if (root == toStop) return
        root = File(root).parent
        initData()
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onClick(view: View ,data: Intent)
    }

    private var onItemClickListener : OnItemClickListener? = null

    fun setOnItemClickListener (listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun getPath(): String = root!!
}
