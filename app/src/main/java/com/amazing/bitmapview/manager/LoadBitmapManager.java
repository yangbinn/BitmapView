package com.amazing.bitmapview.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;

import java.io.IOException;

public class LoadBitmapManager {

    private static final String TAG = "LoadBitmapManager";

    private LruImageCache mLruImageCache;
    private BitmapRegionDecoder mDecoder;
    private BitmapFactory.Options mOptions;
    private Worker mWorker;
    private volatile int mSize;

    public LoadBitmapManager(BitmapRegionDecoder decoder) throws IOException {
        mDecoder = decoder;
        mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = false;
        mOptions.inMutable = true; //是否复用
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;//设置像素格式
        mOptions.inSampleSize = mSize = 1;

        mLruImageCache = LruImageCache.getInstance();
        mWorker = new Worker();
    }

    public void setSampleSize(int size) {
        mSize = size;
        if (mOptions != null) {
            mOptions.inSampleSize = size;
        }
    }

    public Bitmap getBitmap(BitmapEntity entity) {
        if (entity == null || entity.checkNull()) {
            return null;
        }
        String newKey = entity.getKeyBySize(mSize);
        Bitmap bitmap = mLruImageCache.getBitmap(newKey);
        if (bitmap == null || bitmap.isRecycled()) {
            mWorker.enqueue(entity);
            start();
        }
        return bitmap == null || bitmap.isRecycled() ? mLruImageCache.getBitmap(entity.getKey()) : bitmap;
    }

    public synchronized void stop() {
        if (mWorker != null && !mWorker.isStarted()) {
            mWorker.stop();
        }
    }

    public synchronized void start() {
        if (mWorker != null && !mWorker.isStarted()) {
            mWorker.start();
        }
    }

    private void loadBitmap(BitmapEntity entity) {
        if (entity == null || entity.checkNull()) {
            return;
        }
        String newKey = entity.getKeyBySize(mSize);
        Bitmap bitmap = mLruImageCache.getBitmap(newKey);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = mDecoder.decodeRegion(entity.getBitmapRect(), mOptions);
            mLruImageCache.putBitmap(newKey, bitmap);
            Bitmap minBitmap = mLruImageCache.getBitmap(entity.getKey());
            if (minBitmap == null || minBitmap.isRecycled() || minBitmap.getByteCount() > bitmap.getByteCount()) {
                mLruImageCache.putBitmap(entity.getKey(), bitmap);
            }
        }
        if (mCallback != null) {
            mCallback.callback();
        }
    }

    private class Worker implements Runnable {

        private LimitQueue<BitmapEntity> mQueue = new LimitQueue<BitmapEntity>(10);

        private volatile boolean mStarted;

        /**
         * 入队
         */
        public void enqueue(BitmapEntity entity) {
            mQueue.put(entity);
        }

        /**
         * 是否在循环
         *
         * @return boolean
         */
        public boolean isStarted() {
            synchronized (this) {
                return mStarted;
            }
        }

        /**
         * 启动循环
         */
        public void start() {
            synchronized (this) {
                new Thread(this).start();
                mStarted = true;
            }
        }

        @Override
        public void run() {
            try {
                BitmapEntity entity;
                //noinspection ConstantConditions
                while (mStarted && (entity = mQueue.take()) != null) {
                    loadBitmap(entity);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                synchronized (this) {
                    mStarted = false;
                }
            }
        }

        public void stop() {
            mStarted = false;
            mQueue.clear();
        }
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void callback();
    }

    public void close() {
        if (mWorker != null) {
            mWorker.stop();
        }
        if (mDecoder != null) {
            mDecoder.recycle();
        }
    }

}
