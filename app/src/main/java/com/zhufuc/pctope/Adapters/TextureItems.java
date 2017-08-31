package com.zhufuc.pctope.Adapters;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhufuc.pctope.Activities.ConversionActivity;
import com.zhufuc.pctope.Activities.DetailsActivity;
import com.zhufuc.pctope.Activities.MainActivity;
import com.zhufuc.pctope.Collectors.ActivityCollector;
import com.zhufuc.pctope.R;

import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * Created by zhufu on 7/22/17.
 */

public class TextureItems extends RecyclerView.Adapter<TextureItems.ViewHolder> {
    private ArrayList<Textures> mTextures;

    private final String fullPC = "Found:full PC pack.";
    private final String fullPE = "Found:full PE pack.";
    private final String brokenPE = "Found:broken PE pack.";
    private final String brokenPC = "Found:broken PC pack.";

    private OnItemClickListener mOnItemClickListener = null;

    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView TextureName;
        TextView TextureDescription;
        ImageView TextureIcon;
        ImageView AlertIcon;
        CardView cardView;


        public ViewHolder(View v){
            super(v);
            cardView = (CardView)v.findViewById(R.id.card_texture_card);
            TextureIcon = (ImageView)v.findViewById(R.id.card_texture_icon);
            TextureName = (TextView)v.findViewById(R.id.card_texture_name);
            TextureDescription = (TextView)v.findViewById(R.id.card_texture_name_subname);
            AlertIcon = (ImageView)v.findViewById(R.id.card_texture_alert_icon);
        }
    }

    public TextureItems(){
        this.mTextures = new ArrayList<>();
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.texture_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Waiting for update
                if (holder.AlertIcon.getVisibility() == View.VISIBLE){
                    Intent convert = new Intent(holder.AlertIcon.getContext(), ConversionActivity.class);
                    convert.putExtra("willSkipUnzipping",true);
                    convert.putExtra("filePath",holder.AlertIcon.getTag().toString());
                    holder.AlertIcon.getContext().startActivity(convert);
                }
                mOnItemClickListener.onItemClick(view,(int)view.getTag());
            }
        });
        holder.AlertIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(holder.AlertIcon.getContext());
                dialog.setTitle(R.string.broken_pc);
                dialog.setMessage(R.string.broken_pc_dialog_content);
                dialog.setNegativeButton(R.string.ok,null);
                dialog.show();
            }
        });


        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Textures textures = mTextures.get(position);
        //Titles
        String name = textures.getName(),description = textures.getDescription();
        //Change text if it isn't a PE pack
        if (name == null){
            name = holder.TextureName.getResources().getString(R.string.unable_to_get_name);
            holder.TextureDescription.setVisibility(GONE);
        }
        if (description==null||description.equals("")){
            description = "";
            holder.TextureDescription.setVisibility(GONE);
        }
        holder.TextureName.setText(name);
        holder.TextureDescription.setText(description);

        //Set position Tag
        holder.cardView.setTag(position);

        //Image view
        Bitmap icon = BitmapFactory.decodeFile(textures.getIcon());
        if (icon != null){
            holder.TextureIcon.setImageBitmap(icon);
        }
        else holder.TextureIcon.setImageResource(R.drawable.bug_pack_icon);

        //Alert icon
        if (!textures.IfIsResourcePack("PE")){
            String verStr = textures.getVersion();
            if (verStr.equals(fullPC)||verStr.equals(brokenPC)){
                holder.AlertIcon.setVisibility(View.VISIBLE);
                holder.AlertIcon.setTag(textures.getPath());
                holder.TextureDescription.setText(holder.TextureDescription.getResources().getString(R.string.broken_pc_subtitle));
                return;
            }
        }
        holder.AlertIcon.setVisibility(GONE);
    }


    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
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

    public boolean getIfIsAlertIconShown(View view){
        return new ViewHolder(view).AlertIcon.getVisibility()==View.VISIBLE;
    }

    public void clear(){mTextures.clear();}
}
