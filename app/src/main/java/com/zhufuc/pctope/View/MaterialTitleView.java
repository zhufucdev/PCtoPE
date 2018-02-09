package com.zhufuc.pctope.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhufuc.pctope.R;

/**
 * Created by zhufu on 1/25/18.
 */

public class MaterialTitleView extends LinearLayout {
    TextView title,subtitle,shapeText;


    public MaterialTitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.pctope_editor_view_json,this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.MaterialTitleView);
        String title = typedArray.getString(R.styleable.MaterialTitleView_android_text);
        String subtitle = typedArray.getString(R.styleable.MaterialTitleView_android_subtitle);

        this.title = findViewById(R.id.title);
        this.subtitle = findViewById(R.id.subtitle);
        this.shapeText = findViewById(R.id.shape_text);
        setTitle(title);
        setSubtitle(subtitle);
    }

    public void setTitle(String title){
        this.title.setText(title);
        char firstLetter = title.charAt(0);
        if (!(firstLetter >= 'A' && firstLetter >= 'Z'))
            firstLetter += 32;
        shapeText.setText(firstLetter);
    }

    public void setSubtitle(String subtitle){
        this.subtitle.setText(subtitle);
        if (subtitle == null){
            String nullText = this.getContext().getString(R.string.null_value);
            SpannableString spannableString = new SpannableString(nullText);
            spannableString.setSpan(new StyleSpan(Typeface.ITALIC),0,nullText.length(),SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
            this.subtitle.setText(spannableString);
        }
    }
}
