package com.zhufuc.pctope.View;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhufuc.pctope.R;

import java.util.ArrayList;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    private ArrayList<TagInfo> mList;
    TagAdapter(ArrayList<TagInfo> list){
        mList = list;
    }

    public static class TagInfo{
        public String title,value;
        public int color;
        public TagInfo(String title, String value, int backgroundColor) {
            this.title = title;
            this.value = value;
            this.color = backgroundColor;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title,value;
        public CardView background;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            value = itemView.findViewById(R.id.value);
            background = itemView.findViewById(R.id.background_card);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagging_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TagInfo tag = mList.get(position);
        holder.title.setText(tag.title);
        holder.value.setText(tag.value);
        holder.background.setCardBackgroundColor(tag.color);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
