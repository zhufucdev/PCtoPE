package com.zhufuc.pctope;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

/**
 * Created by zhufu on 7/22/17.
 */

public class TextureItems extends RecyclerView.Adapter<TextureItems.ViewHolder> {
    private List<Textures> mTextures;

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
        }
    }

    public TextureItems(List<Textures> textureList){
        mTextures = textureList;
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
        Bitmap bm = BitmapFactory.decodeFile(textures.getIcon().getPath(),options);
        holder.TextureIcon.setImageBitmap(bm);
        Animation swipe = AnimationUtils.loadAnimation(holder.cardView.getContext(),R.anim.show_swipe);
        holder.cardView.startAnimation(swipe);
    }

    @Override
    public int getItemCount(){
        return mTextures.size();
    }
}
