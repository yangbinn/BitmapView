package com.amazing.bitmapview;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onShow(View view) {
        Log.i(TAG, "onShow: ");
        try {
//            InputStream inputStream = getResources().getAssets().open("test.jpg");
//            InputStream inputStream = getResources().getAssets().open("timg.jpg");
//            mImageView.setInputStream(inputStream);
            File file = new File(getSDCardFolder(), "test/timg.jpg");
            Log.i(TAG, "onShow: path=" + file.getPath());
            if (!file.exists()) {
                Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_LONG).show();
                return;
            }
//            mImageView.setPath(file.getPath());
            PictureActivity.startActivity(MainActivity.this, file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File getSDCardFolder() {
        File root = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            root = Environment.getExternalStorageDirectory();
        } else {
            root = Environment.getDataDirectory();
        }
        return root;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
