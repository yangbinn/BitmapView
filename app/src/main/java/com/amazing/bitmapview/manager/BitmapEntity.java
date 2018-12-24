package com.amazing.bitmapview.manager;

import android.graphics.Rect;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Routing
 * Desc TODO
 * Source
 * Created by yb on 2018/12/14 11:45
 * Modify by yb on 2018/12/14 11:45
 * Version 1.0
 */
public class BitmapEntity {

    private String key;
    private Rect bitmapRect;
    private int x;
    private int y;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Rect getBitmapRect() {
        return bitmapRect;
    }

    public void setBitmapRect(Rect bitmapRect) {
        this.bitmapRect = bitmapRect;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public BitmapEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean checkNull() {
        return TextUtils.isEmpty(key) || bitmapRect == null;
    }

    public String getKeyBySize(int size){
        return String.format(Locale.getDefault(), "%s|%d", key, size);

    }

}
