package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.askey.mobile.zwave.control.R;

public class RouterConnectFourActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView right,left;
    private ImageView icon;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_connect_four);

        right = (ImageView) findViewById(R.id.iv_right);
        left = (ImageView) findViewById(R.id.iv_left);
        icon = (ImageView) findViewById(R.id.iv_icon);
        layout = (LinearLayout) findViewById(R.id.layout);
//        decodeResource();
        right.setOnClickListener(this);
        left.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                finish();
                break;
            case R.id.iv_right:
                Intent intent = new Intent(this,DeviceGuideActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void decodeResource() {
        Bitmap bm = decodeBitmapFromResource();
        layout.setBackground(new BitmapDrawable(bm));

//        layout.setImageBitmap(bm);
    }

    private Bitmap decodeBitmapFromResource(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.mipmap.qbee_connect_wifi_icon, options);
        options.inSampleSize = calculateSampleSize(options,200,200);
        options.inJustDecodeBounds =false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return  BitmapFactory.decodeResource(getResources(), R.mipmap.qbee_connect_wifi_icon,options);
    }

    // 计算合适的采样率(当然这里还可以自己定义计算规则)，reqWidth为期望的图片大小，单位是px
    private int calculateSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        Log.i("========","calculateSampleSize reqWidth:"+reqWidth+",reqHeight:"+reqHeight);
        int width = options.outWidth;
        int height =options.outHeight;
        Log.i("========","calculateSampleSize width:"+width+",height:"+height);
        int inSampleSize = 1;
        int halfWidth = width/2;
        int halfHeight = height/2;
        while((halfWidth/inSampleSize)>=reqWidth&& (halfHeight/inSampleSize)>=reqHeight){
            inSampleSize*=2;
            Log.i("========","calculateSampleSize inSampleSize:"+inSampleSize);
        }
        return inSampleSize;
    }

}
