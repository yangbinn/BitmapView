package com.amazing.bitmapview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.amazing.bitmapview.manager.BitmapEntity;
import com.amazing.bitmapview.manager.LoadBitmapManager;
import com.amazing.bitmapview.util.BitmapUtil;
import com.amazing.bitmapview.util.ScreenUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Routing
 * Desc TODO
 * Source
 * Created by yb on 2018/12/5 16:29
 * Modify by yb on 2018/12/5 16:29
 * Version 1.0
 */
public class BitmapView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = "BitmapView";

    private float mBlockLength; //块长度
    private int mWidth; //宽度
    private int mHeight; //高度
    private Paint mPaint; //画笔
    private GestureDetector mGestureDetector; //手势管理
    private int mMoveX; //X轴移动距离
    private int mMoveY; //Y轴移动距离
    private float mPointLength; // 点击长度
    private BitmapParams mBitmapParams; //图像参数管理
    private ValueAnimator mMoveAnimator;
    private ValueAnimator mScaleAnimator;
    private boolean mIsClickListener;
    private float mMinScale;
    private float mMaxScale;

    public BitmapView(Context context) {
        super(context);
        init(context);
    }

    public BitmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BitmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setOnDoubleTapListener(this);

        int width = ScreenUtils.getScreenWidth(context);
        int height = ScreenUtils.getScreenHeight(context);
        mBlockLength = Math.min(width, height) / 2f;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = right - left;
        mHeight = bottom - top;
        show();
        Log.i(TAG, "onLayout: width=" + mWidth + ",height=" + mHeight);
    }

    public void setPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            FileInputStream inputStream = new FileInputStream(path);
            setInputStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setInputStream(InputStream inputStream) {
        try {
            close();
            mBitmapParams = new BitmapParams(inputStream, mBlockLength);
            mBitmapParams.setCallback(new LoadBitmapManager.Callback() {
                @Override
                public void callback() {
                    postInvalidate();
                }
            });
            show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void show() {
        if (mBitmapParams == null || mWidth <= 0 || mHeight <= 0) {
            return;
        }
        //缩放级别
        float scaleW = mWidth * 1f / mBitmapParams.bitmapWidth;
        float scaleH = mHeight * 1f / mBitmapParams.bitmapHeight;
        mMinScale = Math.min(scaleW, scaleH);
        mMaxScale = 2;
        setScale(mMinScale);
    }

    public void setScale(float scale) {
        if (mBitmapParams != null) {
            mBitmapParams.setScale(scale, mWidth, mHeight);
        }
    }

    public float getScale() {
        return mBitmapParams.scale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0 || mBitmapParams == null) {
            return;
        }
        long time = System.currentTimeMillis();
        checkMove();
        drawBitmap(canvas);
        Log.i(TAG, "onDraw: time=" + (System.currentTimeMillis() - time));
    }

    private void checkMove() {
        int maxMoveX = mBitmapParams.bitmapScaleWidth > mWidth ? (int) (mBitmapParams.bitmapScaleWidth - mWidth) : 0;
        int maxMoveY = mBitmapParams.bitmapScaleHeight > mHeight ? (int) (mBitmapParams.bitmapScaleHeight - mHeight) : 0;
        if (mMoveX > 0) {
            mMoveX = 0;
        } else if (mMoveX < -maxMoveX) {
            mMoveX = -maxMoveX;
        }
        if (mMoveY > 0) {
            mMoveY = 0;
        } else if (mMoveY < -maxMoveY) {
            mMoveY = -maxMoveY;
        }
    }

    private void drawBitmap(Canvas canvas) {
        float scale = mBitmapParams.scale;
        int offsetX = mBitmapParams.offsetX;
        int offsetY = mBitmapParams.offsetY;

        for (BitmapEntity[] mBlock : mBitmapParams.bitmapEntities) {
            for (BitmapEntity entity : mBlock) {
                if (entity == null || entity.checkNull()) {
                    continue;
                }
                Rect bitmapRect = entity.getBitmapRect();
                int left = (int) (bitmapRect.left * scale + mMoveX + offsetX);
                int right = (int) (bitmapRect.right * scale + mMoveX + offsetX);
                int top = (int) (bitmapRect.top * scale + mMoveY + offsetY);
                int bottom = (int) (bitmapRect.bottom * scale + mMoveY + offsetY);
                if (!isVisible(left, top, right, bottom)) {
                    continue;
                }
                Rect newShowRect = new Rect(left, top, right, bottom);
                Bitmap bitmap = mBitmapParams.loadBitmapManager.getBitmap(entity);
                if (bitmap != null && !bitmap.isRecycled()) {
                    canvas.drawBitmap(bitmap, null, newShowRect, mPaint);
                } else {
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawRect(newShowRect, mPaint);
                }
            }
        }
    }

    private boolean isVisible(int left, int top, int right, int bottom) {
        return left < mWidth && top < mHeight && right > 0 && bottom > 0;
    }

    private boolean mScaling; //正在缩放

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int count = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN | 0x0100:
            case MotionEvent.ACTION_POINTER_DOWN | 0x0200: {
                mScaling = count == 2;
                mPointLength = getLengthByDown(event);
                mIsClickListener = count == 1;
                Log.i(TAG, "onTouchEvent,ACTION_DOWN: mPointLength=" + mPointLength);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mScaling && mBitmapParams != null) {
                    float length = getLengthByDown(event);
                    if (mPointLength == 0 || length == 0) {
                        return true;
                    }
                    float scale = length / mPointLength;
                    Log.i(TAG, "onScroll: mPointLength=" + mPointLength + ",length=" + length + ", scale=" + scale + ",mScale=" + mBitmapParams.scale);
                    setScale(mBitmapParams.scale * scale);
                    mPointLength = length;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                Log.i(TAG, "onTouchEvent: ACTION_UP, count=" + event.getPointerCount());
                if (mBitmapParams != null) {
                    if (mBitmapParams.scale < mMinScale) {
                        scaleAnimator(mMinScale);
                    } else if (mBitmapParams.scale > mMaxScale) {
                        scaleAnimator(mMaxScale);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                mScaling = count == 3;
                mPointLength = getLengthByUp(event, 0);
                Log.i(TAG, "onTouchEvent: mPointLength=" + mPointLength);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP | 0x0100: {
                mScaling = count == 3;
                mPointLength = getLengthByUp(event, 1);
                Log.i(TAG, "onTouchEvent: mPointLength=" + mPointLength);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP | 0x0200: {
                mScaling = count == 3;
                mPointLength = getLengthByUp(event, 2);
                Log.i(TAG, "onTouchEvent: mPointLength=" + mPointLength);
                break;
            }
        }
        return mScaling || mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.i(TAG, "onDown: ");
        if (mMoveAnimator != null) {
            mMoveAnimator.cancel();
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.i(TAG, "onShowPress: ");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i(TAG, "onSingleTapUp: ");
        return false;
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //移动
        mMoveX -= (int) distanceX;
        mMoveY -= (int) distanceY;
        postInvalidate();
        return true;
    }

    private float getLengthByDown(MotionEvent event) {
        if (event != null && event.getPointerCount() > 1) {
            float x0 = event.getX(0);
            float y0 = event.getY(0);
            float x1 = event.getX(1);
            float y1 = event.getY(1);
            return (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
        }
        return 0;
    }

    private float getLengthByUp(MotionEvent event, int point) {
        if (event != null && event.getPointerCount() == 3) {
            float x0 = -1;
            float y0 = -1;
            float x1 = -1;
            float y1 = -1;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                builder.append(event.getX(i)).append(event.getY(i)).append("-");
                if (i == point) {
                    continue;
                }
                if (x0 == -1 || y0 == -1) {
                    x0 = event.getX(i);
                    y0 = event.getY(i);
                } else {
                    x1 = event.getX(i);
                    y1 = event.getY(i);
                }
            }
            Log.i(TAG, "getLengthByUp: " + builder.toString());
            return (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
        }
        return mPointLength;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i(TAG, "onLongPress: ");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        Log.i(TAG, "onFling: velocityX=" + velocityX + ",velocityY=" + velocityY);
        mMoveAnimator = ValueAnimator.ofFloat(1f, 0);
        mMoveAnimator.setDuration(1000);
        mMoveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float x = velocityX * value * 0.01f;
                float y = velocityY * value * 0.01f;
                mMoveX += (int) x;
                mMoveY += (int) y;
                postInvalidate();
            }
        });
        mMoveAnimator.start();
        return true;
    }

    private void scaleAnimator(float scale) {
        if (mBitmapParams == null || scale == mBitmapParams.scale) {
            return;
        }
        if (mScaleAnimator != null) {
            mScaleAnimator.cancel();
        }
        mScaleAnimator = ValueAnimator.ofFloat(mBitmapParams.scale, scale);
        mScaleAnimator.addUpdateListener(mScaleUpdateListener);
        mScaleAnimator.setDuration(300);
        mScaleAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener mScaleUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            setScale(value);
        }
    };

    public void close() {
        mMoveX = 0;
        mMoveY = 0;
        if (mBitmapParams != null) {
            mBitmapParams.close();
            mBitmapParams = null;
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // 可以确认（通过单击DOWN后300ms没有下一个DOWN事件确认）这不是一个双击事件，而是一个单击事件的时候会回调。
        Log.i(TAG, "onSingleTapConfirmed: ");
        if (mIsClickListener && mBitmapClickListener != null) {
            mBitmapClickListener.onClick(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //双击事件的时候回调
        Log.i(TAG, "onDoubleTap: ");
        scaleAnimator(getScale() * 2);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.i(TAG, "onDoubleTap: ");
        return false;
    }

    public static class BitmapParams {

        InputStream inputStream;
        LoadBitmapManager loadBitmapManager;
        BitmapRegionDecoder decoder;

        LoadBitmapManager.Callback callback;
        float blockLength;
        float scale;
        BitmapEntity[][] bitmapEntities;
        int bitmapWidth;
        int bitmapHeight;
        float bitmapScaleWidth;
        float bitmapScaleHeight;
        int offsetX;
        int offsetY;

        public BitmapParams(InputStream inputStream, float blockLength) throws IOException {
            this.inputStream = inputStream;
            this.blockLength = blockLength;
            this.decoder = BitmapRegionDecoder.newInstance(inputStream, false);
            bitmapWidth = decoder.getWidth();
            bitmapHeight = decoder.getHeight();
            loadBitmapManager = new LoadBitmapManager(decoder);
            loadBitmapManager.setCallback(new LoadBitmapManager.Callback() {
                @Override
                public void callback() {
                    refresh();
                }
            });
            initBlock();
        }

        private void initBlock() {
            //开始分块
            int sizeX = (int) Math.ceil(bitmapWidth / blockLength);
            int sizeY = (int) Math.ceil(bitmapHeight / blockLength);
            Log.i(TAG, "initBlock: w=" + bitmapWidth + ",h=" + bitmapHeight + ",length=" + blockLength + ",sizeX=" + sizeX + ",sizeY=" + sizeY);
            bitmapEntities = new BitmapEntity[sizeY][sizeX];
            for (int y = 0; y < bitmapEntities.length; y++) {
                for (int x = 0; x < bitmapEntities[y].length; x++) {
                    bitmapEntities[y][x] = new BitmapEntity(x, y);
                    bitmapEntities[y][x].setKey(getKey(toString(), x, y));
                    bitmapEntities[y][x].setBitmapRect(getRect(x, y));
                }
            }
        }

        public void setScale(float scale, int width, int height) {
            if (this.scale == scale) {
                return;
            }
            Log.i(TAG, "setScale: scale=" + scale);
            this.scale = scale;
            //缩放后的图片大小
            bitmapScaleWidth = bitmapWidth * scale;
            bitmapScaleHeight = bitmapHeight * scale;
            //重置X,Y轴偏移量
            offsetX = width > bitmapScaleWidth ? (int) ((width - bitmapScaleWidth) / 2f) : 0;
            offsetY = height > bitmapScaleHeight ? (int) ((height - bitmapScaleHeight) / 2f) : 0;
            if (loadBitmapManager != null) {
                loadBitmapManager.setSampleSize(BitmapUtil.compressBitmapSize(bitmapScaleWidth, bitmapScaleHeight, bitmapWidth, bitmapHeight));
            }
            refresh();
        }

        private String getKey(String path, int x, int y) {
            return String.format(Locale.getDefault(), "%s|%d|%d", path, x, y);
        }

        public Rect getRect(int x, int y) {
            int left = (int) (x * blockLength);
            int top = (int) (y * blockLength);
            int right = (int) (left + blockLength);
            int bottom = (int) (top + blockLength);
            if (right > bitmapWidth) {
                right = bitmapWidth;
            }
            if (bottom > bitmapHeight) {
                bottom = bitmapHeight;
            }
            return new Rect(left, top, right, bottom);
        }

        public void setCallback(LoadBitmapManager.Callback callback) {
            this.callback = callback;
        }

        private void refresh() {
            if (callback != null) {
                callback.callback();
            }
        }

        private void close() {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (decoder != null) {
                    decoder.recycle();
                }
                if (loadBitmapManager != null) {
                    loadBitmapManager.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private OnBitmapClickListener mBitmapClickListener;

    public void setOnBitmapClickListener(OnBitmapClickListener listener) {
        mBitmapClickListener = listener;
    }

    public interface OnBitmapClickListener {

        void onClick(View view);

    }

}

