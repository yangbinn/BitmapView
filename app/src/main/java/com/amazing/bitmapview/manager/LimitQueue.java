package com.amazing.bitmapview.manager;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Routing
 * Desc TODO
 * Source
 * Created by yb on 2018/12/13 15:37
 * Modify by yb on 2018/12/13 15:37
 * Version 1.0
 */
public class LimitQueue<E> extends ArrayBlockingQueue<E> {

    private int mLimit;

    public LimitQueue(int i) {
        this(i, false);
    }

    public LimitQueue(int i, boolean b) {
        super(i, b);
        mLimit = i;
    }

    public LimitQueue(int i, boolean b, Collection<? extends E> collection) {
        super(i, b, collection);
        mLimit = i;
    }

    @Override
    public boolean add(E e) {
        if(size() >= mLimit){
            remove();
        }
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return super.addAll(collection);
    }

    @Override
    public void put(E e) {
        if(size() >= mLimit){
            remove();
        }
        try {
            super.put(e);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

}
