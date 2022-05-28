package com.zhufucdev.pctope.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by zhufu on 17-7-29.
 */

class DiffUtilCallback(private val oldTemp: RecyclerView.Adapter<*>, private val newTemp: RecyclerView.Adapter<*>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldTemp.itemCount
    }

    override fun getNewListSize(): Int {
        return newTemp.itemCount
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTemp.getItemId(oldItemPosition) == newTemp.getItemId(newItemPosition)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTemp.javaClass == newTemp.javaClass
    }
}
