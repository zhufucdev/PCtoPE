package com.zhufuc.pctope.Adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.zhufuc.pctope.Activities.ConversionActivity
import com.zhufuc.pctope.R

import android.view.View.GONE
import com.zhufuc.pctope.Utils.TextureCompat.brokenPC
import com.zhufuc.pctope.Utils.TextureCompat.fullPC
import com.zhufuc.pctope.Utils.Textures
import kotlin.collections.ArrayList

/**
 * Created by zhufu on 7/22/17.
 */

class TextureItems(textures: ArrayList<Textures>) : RecyclerView.Adapter<TextureItems.ViewHolder>() {
    private val mTextures: ArrayList<Textures>
    private var viewHolders = ArrayList<ViewHolder>()
    var isInSelectMode = false

    private var mOnItemClickListener: OnItemClickListener = object : OnItemClickListener{
        override fun onItemClick(view: View, position: Int) {}
        override fun onLongPress(view: View, position: Int) {}
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onLongPress(view: View, position: Int)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var TextureName: TextView
        var TextureDescription: TextView
        var TextureIcon: ImageView
        var AlertIcon: ImageView
        var cardView: CardView

        init {
            TextureIcon = v.findViewById(R.id.card_texture_icon)
            TextureName = v.findViewById(R.id.card_texture_name)
            TextureDescription = v.findViewById(R.id.card_texture_name_subname)
            AlertIcon = v.findViewById(R.id.card_texture_alert_icon)
            cardView = v.findViewById(R.id.card_texture_card)
        }

        var isSet : Boolean = false
        fun setForeground(){
            if (!isSet){
                val foreground = ContextCompat.getDrawable(cardView.context,R.drawable.foreground_selected)
                cardView.foreground = foreground
            }
            else{
                cardView.foreground = null
            }
            isSet = !isSet
        }
    }

    init {
        this.mTextures = textures
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.texture_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val textures = mTextures[position]
        viewHolders.add(holder)
        //Titles
        var name = textures.name
        var description = textures.description
        //Change text if it isn't a PE pack
        if (name == null) {
            name = holder.TextureName.resources.getString(R.string.unable_to_get_name)
            holder.TextureDescription.visibility = GONE
        }
        if (description == null || description == "") {
            description = ""
            holder.TextureDescription.visibility = GONE
        }
        holder.TextureName.text = name
        holder.TextureDescription.text = description

        //Set position Tag
        textures.position = position

        //Image view
        val icon = BitmapFactory.decodeFile(textures.icon)
        if (icon != null) {
            holder.TextureIcon.setImageBitmap(icon)
        } else
            holder.TextureIcon.setImageResource(R.drawable.bug_pack_icon)

        holder.cardView.setOnClickListener {
            if (!isInSelectMode) {
                if (holder.AlertIcon.visibility == View.VISIBLE) {
                    val convert = Intent(holder.AlertIcon.context, ConversionActivity::class.java)
                    convert.putExtra("willSkipUnzipping", true)
                    convert.putExtra("filePath", textures.position)
                    holder.AlertIcon.context.startActivity(convert)
                }
            }
            else{
                holder.setForeground()
                if (holder.isSet)
                    selectedItems.add(position)
                else
                    selectedItems.remove(position)
                selectedItems.sort()
            }
            mOnItemClickListener.onItemClick(it, textures.position)
        }
        holder.AlertIcon.setOnClickListener {
            val dialog = AlertDialog.Builder(holder.AlertIcon.context)
            dialog.setTitle(R.string.broken_pc)
            dialog.setMessage(R.string.broken_pc_dialog_content)
            dialog.setNegativeButton(R.string.ok, null)
            dialog.show()
        }
        holder.cardView.setOnLongClickListener({
            holder.setForeground()
            isInSelectMode = holder.isSet
            if (isInSelectMode)
                selectedItems.add(position)
            else
                selectedItems.remove(position)
            selectedItems.sort()
            mOnItemClickListener.onLongPress(it,position)
            true
        })

        //Alert icon
        if (!textures.IfIsResourcePack("PE")!!) {
            val verStr = textures.getVersion()
            if (verStr.equals(fullPC) || verStr.equals(brokenPC)) {
                holder.AlertIcon.visibility = View.VISIBLE
                holder.TextureDescription.text = holder.TextureDescription.resources.getString(R.string.broken_pc_subtitle)
                return
            }
        }
        holder.AlertIcon.visibility = GONE
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mOnItemClickListener = listener
    }

    override fun getItemCount(): Int = mTextures.size

    fun getItem(index: Int): Textures = mTextures[index]

    fun getIfIsAlertIconShown(view: View): Boolean = ViewHolder(view).AlertIcon.visibility == View.VISIBLE

    fun deselectAll(){
        selectedItems = ArrayList()
        viewHolders.forEach {
            if (it.isSet)
                it.setForeground()
        }
    }

    var selectedItems = ArrayList<Int>()
}
