package com.zhufucdev.pctope.adapters

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.pctope.R
import com.zhufucdev.pctope.utils.FileType
import java.io.File
import java.util.*

/**
 * Created by zhufu on 17-9-8.
 */

class FileChooserAdapter(private var root: String?, private var lastFixes: List<String>) :
    RecyclerView.Adapter<FileChooserAdapter.ViewHolder>() {
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
        files
            ?.filter { it.exists() }
            ?.forEach { f ->
                if (f.isFile) {
                    for (suffix in lastFixes)
                        if (f.path.endsWith(suffix)) {
                            fileList.add(f)
                            break
                        }
                } else
                    folderList.add(f)
            }

        folderList.sort()
        fileList.sort()
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

            if (lastFixes.contains("png") || lastFixes.contains("jpg")) {
                val imageView = holder.zipFileView.findViewById<ImageView>(R.id.chooser_zip)

                class loadImage : AsyncTask<Void, Void, Boolean>() {
                    lateinit var bitmap: Bitmap
                    override fun doInBackground(vararg p0: Void?): Boolean {
                        val type = FileType.get(file)
                        if (!(type == "ffd8ffe1" || type == "89504e47"))
                            return false

                        val options = BitmapFactory.Options()
                        options.inSampleSize = 2
                        bitmap = BitmapFactory.decodeFile(file.path, options)
                        return true
                    }

                    override fun onPostExecute(result: Boolean?) {
                        if (result != false)
                            imageView.setImageBitmap(bitmap)
                        super.onPostExecute(result)
                    }
                }
                loadImage().execute()
            }

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
        val data: Intent = Intent().putExtra("path", fileClicked)

        if (File(fileClicked).isFile) {
            onItemClickListener?.onClick(view, data)
            return@OnClickListener
        }

        root = fileClicked
        onItemClickListener?.onClick(view, data)
        initData()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    fun upLevel(toStop: String): Boolean {
        if (root == toStop) return false
        root = File(root).parent
        initData()
        notifyDataSetChanged()
        return true
    }

    interface OnItemClickListener {
        fun onClick(view: View, data: Intent)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun getPath(): String = root!!
}
