package com.zhufuc.pctope.Utils;

import android.support.v7.util.DiffUtil;

import com.zhufuc.pctope.Adapters.TextureItems;

import java.util.Objects;

/**
 * Created by zhufu on 17-7-29.
 */

public class DiffUtilCallback extends DiffUtil.Callback {

    private TextureItems oldTemp,newTemp;

    public DiffUtilCallback(TextureItems oldTemp,TextureItems newTemp){
        this.oldTemp = oldTemp;
        this.newTemp = newTemp;
    }

    @Override
    public int getOldListSize() {
        return oldTemp.getItemCount();
    }

    @Override
    public int getNewListSize() {
        return newTemp.getItemCount();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return Objects.equals(oldTemp.getItem(oldItemPosition),newTemp.getItem(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return Objects.equals(oldTemp.getItem(oldItemPosition).getName(),newTemp.getItem(newItemPosition).getName());
    }
}
