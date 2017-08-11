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
        Float scaleHeight = (float) Height/oldHeight,scaleWidth = (float) Width/oldWidth;

        matrix.postScale(scaleWidth,scaleHeight);

        return Bitmap.createBitmap(bitmap,0,0,oldWidth,oldHeight,matrix,false);
    }
}
