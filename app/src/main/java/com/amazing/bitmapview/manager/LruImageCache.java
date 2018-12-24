package com.amazing.bitmapview.manager;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import java.util.Locale;


/**
 * 图片缓存
 *
 * @author youngbin
 * 2016-08-26
 */
public class LruImageCache {

    public static final String TAG = "LruImageCache";

    private LruCache<String, Bitmap> mMemoryCache;
    private static LruImageCache mInstance;

    public static LruImageCache getInstance() {
        if (null == mInstance) {
            synchronized (LruImageCache.class) {
                if (null == mInstance)
                    mInstance = new LruImageCache();
            }
        }
        return mInstance;
    }

    private LruImageCache() {
        if (mMemoryCache == null) {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 16;
            Log.i(TAG, "LruImageCache: maxMemory=" + maxMemory + ",cacheSize=" + cacheSize);
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

                @Override
                protected int sizeOf(String key, Bitmap value) {
                    if (value != null && !value.isRecycled())
                        return value.getByteCount() / 1024;
                    return 0;
                }
            };
        }
    }

    public Bitmap getBitmap(String s) {
        if (mMemoryCache == null || TextUtils.isEmpty(s)) {
            Log.i(TAG, "getBitmap, params is error.");
            return null;
        }
        return mMemoryCache.get(s);
    }

    public void putBitmap(String s, Bitmap bitmap) {
        if (mMemoryCache == null || TextUtils.isEmpty(s) || bitmap == null || bitmap.isRecycled()) {
            Log.i(TAG, "putBitmap, params is error.");
            return;
        }
        mMemoryCache.put(s, bitmap);
    }

    public void removeBitmapCache(String key) {
        if (mMemoryCache == null || TextUtils.isEmpty(key)) {
            Log.i(TAG, "removeBitmapCache, params is error.");
            return;
        }
        Bitmap b = mMemoryCache.get(key);
        if (b != null) {
            b.recycle();
        }
        mMemoryCache.remove(key);

    }

    public void clearCache() {
        if (mMemoryCache != null && mMemoryCache.size() > 0) {
            mMemoryCache.evictAll();
            mMemoryCache = null;
        }
    }

    public String getKey(String path, int width, int height) {
        if (TextUtils.isEmpty(path)) {
            return path;
        }
        return String.format(Locale.getDefault(), "%s|%d*%d", path, width, height);
    }

}
