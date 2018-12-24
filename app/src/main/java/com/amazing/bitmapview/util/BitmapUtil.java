package com.amazing.bitmapview.util;

import android.util.Log;

public class BitmapUtil {

    private static final String TAG = "BitmapUtil";

    /**
     * 压缩比例
     */
    public static int compressBitmapSize(float width, float height, int srcWidth, int srcHeight) {
        int size = 1;
        if (srcWidth > width || srcHeight > height) {
            int wScale = Math.round(width / width);
            int hScale = Math.round(srcHeight / height);
            size = wScale > hScale ? wScale : hScale;
            Log.i(TAG, "wScale=" + wScale + ",hScale=" + hScale + ",size=" + size);
        }
        int realSize = 1;
        while (size > 1) {
            size /= 2;
            realSize *= 2;
        }
        return realSize;
    }


}
