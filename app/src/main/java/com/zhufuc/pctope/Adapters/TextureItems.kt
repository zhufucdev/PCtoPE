package com.zhufuc.pctope.Adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
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

    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var TextureName: TextView
        var TextureDescription: TextView
        var TextureIcon: ImageView
        var AlertIcon: ImageView
        var cardView: CardView


        init {
            TextureIcon = v.findViewById<View>(R.id.card_texture_icon) as ImageView
            TextureName = v.findViewById<View>(R.id.card_texture_name) as TextView
            TextureDescription = v.findViewById<View>(R.id.card_texture_name_subname) as TextView
            AlertIcon = v.findViewById<View>(R.id.card_texture_alert_icon) as ImageView
            cardView = v.findViewById<View>(R.id.card_texture_card) as CardView
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
        holder.cardView.tag = position

        //Image view
        val icon = BitmapFactory.decodeFile(textures.icon)
        if (icon != null) {
            holder.TextureIcon.setImageBitmap(icon)
        } else
            holder.TextureIcon.setImageResource(R.drawable.bug_pack_icon)

        holder.cardView.setOnClickListener {
            if (holder.AlertIcon.visibility == View.VISIBLE) {
                val convert = Intent(holder.AlertIcon.context, ConversionActivity::class.java)
                convert.putExtra("willSkipUnzipping", true)
                convert.putExtra("filePath", holder.AlertIcon.tag.toString())
                holder.AlertIcon.context.startActivity(convert)
                return@setOnClickListener
            }
            mOnItemClickListener!!.onItemClick(it, it.tag as Int)
        }
        holder.AlertIcon.setOnClickListener {
            val dialog = AlertDialog.Builder(holder.AlertIcon.context)
            dialog.setTitle(R.string.broken_pc)
            dialog.setMessage(R.string.broken_pc_dialog_content)
            dialog.setNegativeButton(R.string.ok, null)
            dialog.show()
        }

        //Alert icon
        if (!textures.IfIsResourcePack("PE")!!) {
            val verStr = textures.getVersion()
            if (verStr.equals(fullPC) || verStr.equals(brokenPC)) {
                holder.AlertIcon.visibility = View.VISIBLE
                holder.AlertIcon.tag = textures.path
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

    fun remove(position: Int) {
        mTextures.removeAt(position)
    }

    fun getItem(index: Int): Textures = mTextures[index]

    fun getIfIsAlertIconShown(view: View): Boolean = ViewHolder(view).AlertIcon.visibility == View.VISIBLE

    fun clear() {
        mTextures.clear()
    }

}
