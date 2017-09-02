package com.zhufuc.pctope.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by zhufu on 17-8-11.
 */

public class CompressImage {
    public static Bitmap getBitmap(Bitmap bitmap,int Height,int Width){
        Matrix matrix = new Matrix();
        int oldHeight = bitmap.getHeight(),oldWidth = bitmap.getWidth();
        if (oldHeight<=0 || oldWidth<=0)
            return null;

        Float scaleHeight = (float) Height/oldHeight,scaleWidth = (float) Width/oldWidth;

        matrix.postScale(scaleWidth,scaleHeight);

        return Bitmap.createBitmap(bitmap,0,0,oldWidth,oldHeight,matrix,false);
    }

    public static Bitmap getBitmap(Bitmap bitmap,float scale){
        Matrix matrix = new Matrix();
        int height = bitmap.getHeight(),width = bitmap.getWidth();
        matrix.postScale(scale,scale);
        return Bitmap.createBitmap(bitmap,0,0,width,height,matrix,false);
    }

    public static boolean testBitmap(int testWidth,int testHeight,Bitmap bitmap){
        return (bitmap.getHeight()>testHeight || bitmap.getWidth()>testWidth);
    }
}
