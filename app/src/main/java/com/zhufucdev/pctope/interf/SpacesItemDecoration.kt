package com.zhufucdev.pctope.interf

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by zhufu on 8/16/17.
 */

class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space
        outRect.top = space
    }
}
