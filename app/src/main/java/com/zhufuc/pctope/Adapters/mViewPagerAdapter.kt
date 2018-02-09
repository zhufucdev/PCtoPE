package com.zhufuc.pctope.Adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.zhufuc.pctope.R

/**
 * Created by zhufu on 17-12-31.
 */
class mViewPagerAdapter(val viewList : List<View>,val context: Context) : PagerAdapter(){

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList[position])
        return viewList[position]
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = (view == `object`)

    override fun getCount(): Int = viewList.size

    override fun getPageTitle(position: Int): CharSequence?{
        when(position){
            0 -> return context.getString(R.string.editor_tab_json)
            1 -> return context.getString(R.string.editor_tab_image)
        }
        return null
    }
}