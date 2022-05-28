package com.zhufucdev.pctope.utils

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * Created by zhufu on 17-8-11.
 */

object CompressImage {
    fun getBitmap(bitmap: Bitmap, Height: Int, Width: Int): Bitmap? {
        val matrix = Matrix()
        val oldHeight = bitmap.height
        val oldWidth = bitmap.width
        if (oldHeight <= 0 || oldWidth <= 0)
            return null

        val scaleHeight = Height.toFloat() / oldHeight
        val scaleWidth = Width.toFloat() / oldWidth

        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, matrix, false)
    }

    fun getBitmap(bitmap: Bitmap, scale: Float): Bitmap {
        val matrix = Matrix()
        val height = bitmap.height
        val width = bitmap.width
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
    }

    fun testBitmap(testWidth: Int, testHeight: Int, bitmap: Bitmap): Boolean {
        return bitmap.height > testHeight || bitmap.width > testWidth
    }
}
