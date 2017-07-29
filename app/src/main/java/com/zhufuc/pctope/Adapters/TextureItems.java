package com.zhufuc.pctope.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhufuc.pctope.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhufu on 7/22/17.
 */

public class TextureItems extends RecyclerView.Adapter<TextureItems.ViewHolder> {
    private ArrayList<Textures> mTextures;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView TextureName;
        TextView TextureDescription;
        ImageView TextureIcon;
        CardView cardView;


        public ViewHolder(View v){
            super(v);
            cardView = (CardView)v.findViewById(R.id.card_texture_card);
            TextureIcon = (ImageView)v.findViewById(R.id.card_texture_icon);
            TextureName = (TextView)v.findViewById(R.id.card_texture_name);
            TextureDescription = (TextView)v.findViewById(R.id.card_texture_name_subname);

            //for sth else

        }
    }

    public TextureItems(){
        this.mTextures = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.texture_item,parent,false);
        ViewHolder holder = new ViewHolder(view);

        final ViewHolder holder1 = new ViewHolder(view);

        holder1.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Waiting for update
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Textures textures = mTextures.get(position);
        holder.TextureName.setText(textures.getName());
        holder.TextureDescription.setText(textures.getDescription());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize=2;

        if (!(textures.getIcon() == null)){
            String pathIcon = textures.getIcon().getPath();
            Bitmap bm = BitmapFactory.decodeFile(pathIcon,options);
            holder.TextureIcon.setImageBitmap(bm);
        }

    }

    @Override
    public int getItemCount(){
        return mTextures.size();
    }

    public void addItem(int index,Textures texture){
        mTextures.add(index,texture);
    }

    public void addItem(Textures texture){mTextures.add(texture);}

    public void remove(int position){mTextures.remove(position);}

    public Textures getItem(int index){return mTextures.get(index);}

    public void clear(){mTextures.clear();}
}
