package com.zhufuc.pctope.View;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhufuc.pctope.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhufu on 2/3/18.
 */

public class Tagger extends RecyclerView {
    private ArrayList<TagAdapter.TagInfo> mList = new ArrayList<>();
    private TagAdapter adapter;
    public Tagger(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        adapter = new TagAdapter(mList);
        setAdapter(adapter);
        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
    }

    public void add(String title,String value,int background){
        mList.add(new TagAdapter.TagInfo(title,value,background));
    }

    public void add(TagAdapter.TagInfo tagInfo){
        mList.add(tagInfo);
        adapter.notifyDataSetChanged();
    }

    public void addAll(List<TagAdapter.TagInfo> list){
        mList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    public void clear(){
        mList.clear();
        adapter.notifyDataSetChanged();
    }
}