package com.askey.mobile.zwave.control.home.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class PreviewPhotoActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "PreviewPhotoActivity";
    private String path;
    private ImageView photo, back, yes;
    private Bitmap bitmap;
    private static ChangeBackgroundCallback callBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_photo);

        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    private void initView() {
        photo = (ImageView) findViewById(R.id.photo);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        yes = (ImageView) findViewById(R.id.yes);
        yes.setOnClickListener(this);
    }

    private void initData() {
        path = getIntent().getStringExtra("path");
        Log.d(TAG, path);
        Glide.with(this).load(new File(path)).error(R.drawable.nophoto).into(photo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap myBitmap = null;
                        try {
                            myBitmap = Glide.with(PreviewPhotoActivity.this)
                                    .load(new File(path))
                                    .asBitmap() //必须
                                    .centerCrop()
                                    .into(1080, 1920)
                                    .get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        final Bitmap finalMyBitmap = myBitmap;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callBack.changeBackground(finalMyBitmap, path);
                            }
                        });
                        finish();
                    }
                }).start();
                break;
            case R.id.back:
                startActivity(new Intent(this, TakePictureActivity.class));
                finish();
                break;
        }
    }

    public static interface ChangeBackgroundCallback{
        void changeBackground(Bitmap bitmap, String fileSrc);
    }
    public static void setChangeBackgroundCallback(ChangeBackgroundCallback callback){
        callBack = callback;
    }
}
