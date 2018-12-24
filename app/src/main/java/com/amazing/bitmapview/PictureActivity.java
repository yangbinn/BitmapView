package com.amazing.bitmapview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Routing
 * Desc TODO
 * Source
 * Created by yb on 2018/12/21 14:56
 * Modify by yb on 2018/12/21 14:56
 * Version 1.0
 */
public class PictureActivity extends AppCompatActivity {

    private static final String TAG = "PictureActivity";

    private BitmapView mBitmapView;

    public static void startActivity(Context context, String path) {
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_picture);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        mBitmapView = findViewById(R.id.picture_iv);
        Log.i(TAG, "onCreate: path=" + path);
        mBitmapView.setPath(path);

        mBitmapView.setOnBitmapClickListener(new BitmapView.OnBitmapClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBitmapView != null){
            mBitmapView.close();
        }
    }
}
