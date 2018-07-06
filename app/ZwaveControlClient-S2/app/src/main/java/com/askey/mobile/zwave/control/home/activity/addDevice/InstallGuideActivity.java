package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.util.Const;

import java.io.InputStream;

public class InstallGuideActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView brand_icon, device_icon, step_iv, step_icon;
    private TextView brand_name, device_name, step_index, step_notify;
    private LinearLayout linear_info, linear_step;
    private String brand = "";
    private String deviceType = "";
    private int icon;
    private int[] step_icons = new int[]{};
    private int[] step_notifys = new int[]{};
    private int steps;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_guide);

        initView();
        Bitmap bitmap = readBitMap(this, R.drawable.smart_switch_02);
        step_icon.setImageBitmap(bitmap);
        initData();
    }

    private void initView() {
        step_icon = (ImageView) findViewById(R.id.step_icon);
        brand_icon = (ImageView) findViewById(R.id.brand_icon);
        device_icon = (ImageView) findViewById(R.id.device_icon);
        step_iv = (ImageView) findViewById(R.id.step_iv);
        step_iv.setOnClickListener(this);

        brand_name = (TextView) findViewById(R.id.brand_name);
        device_name = (TextView) findViewById(R.id.device_name);
        step_index = (TextView) findViewById(R.id.step_index);
        step_notify = (TextView) findViewById(R.id.step_notify);

        linear_info = (LinearLayout) findViewById(R.id.linear_info);
        linear_step = (LinearLayout) findViewById(R.id.linear_step);
    }

    private void initData() {
        brand = getIntent().getStringExtra("brand");
        deviceType = getIntent().getStringExtra("deviceType");
        icon = getIntent().getIntExtra("deviceIcon", 0);

        device_icon.setImageResource(icon);
        device_name.setText(deviceType);

        if (deviceType.equals("BULB")) {
            step_icons = Const.Bulb_step;
            step_notifys = Const.Bulb_notify;
        }
        if (deviceType.equals("PLUG")) {
            step_icons = Const.switch_step;
            step_notifys = Const.switch_notify;
        }
        if (deviceType.equals("WALLMOTE")) {
            step_icons = Const.Wallmote_step;
            step_notifys = Const.Wallmote_notify;
        }
        if (deviceType.equals("EXTENDER")) {
            step_icons = Const.Extender_step;
            step_notifys = Const.Extender_notify;
        }

        steps = step_icons.length;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.step_iv:
                if (steps > 0) {
                    if (currentIndex < steps) {
                        linear_info.setVisibility(View.GONE);
                        linear_step.setVisibility(View.VISIBLE);
                        int step_icon = step_icons[currentIndex];
                        // Glide.with(this).load(step_icon).into(this.step_icon);
                        //         this.step_icon.setImageResource(step_icons[currentIndex]);
                        Bitmap bitmap = readBitMap(this, step_icon);
                        this.step_icon.setImageBitmap(bitmap);
                        step_notify.setText(step_notifys[currentIndex]);
                        step_index.setText("Step "+(currentIndex+1)+"");
                        currentIndex++;
                    } else {
                        Intent intent = new Intent(this, InstallDeviceActivity.class);
                        intent.putExtra("brand",brand);
                        intent.putExtra("deviceType",deviceType);
                        startActivity(intent);
                        finish();
                    }
                }
                break;
        }
    }
    /**
     * 以最省内存的方式读取本地资源的图片
     * @param context  上下文
     * @param resId 资源Id
     * @return 返回bitmap
     */
    public static Bitmap readBitMap(Context context, int resId){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        //压缩编码
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        //下面两个过时了，但没影响
        opt.inPurgeable = true;
        opt.inInputShareable = true;

        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is,null,opt);
    }
}
