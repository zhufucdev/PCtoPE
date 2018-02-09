package com.zhufuc.pctope.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.zhufuc.pctope.R;

import org.w3c.dom.Text;

/**
 * Created by zhufu on 1/19/18.
 */

public class TitleCard extends CardView {

    TextView mTextView;

    public TitleCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title_card,this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.TitleCard);
        String title = typedArray.getString(R.styleable.TitleCard_android_text);
        mTextView = findViewById(R.id.title);
        mTextView.setText(title);
    }

    public void setTitle(String text){
        mTextView.setText(text);
    }

    public RecyclerView getRecyclerView(){
        return (RecyclerView) findViewById(R.id.recycler);
    }
}
