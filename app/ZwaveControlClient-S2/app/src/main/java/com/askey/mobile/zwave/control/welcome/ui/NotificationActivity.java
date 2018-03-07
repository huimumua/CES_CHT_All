package com.askey.mobile.zwave.control.welcome.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.guideSetting.ui.DeviceGuideActivity;
import com.askey.mobile.zwave.control.guideSetting.ui.DeviceGuideHomeActivity;
import com.askey.mobile.zwave.control.login.ui.LoginPageActivity;

public class NotificationActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        showDialog();
    }

    /**
     * 这是兼容的 AlertDialog
     */
    private void showDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.notifi_title));
        builder.setMessage(getResources().getString(R.string.notifi_content));
        builder.setNegativeButton(getResources().getString(R.string.dont_allow), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//               Intent intent = new Intent(NotificationActivity.this,LoginPageActivity.class);
//                startActivity(intent);
                //直接跳转到最后一个向导界面
                Intent intent = new Intent(NotificationActivity.this, DeviceGuideHomeActivity.class);
                startActivity(intent);
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.subscribe_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(NotificationActivity.this, DeviceGuideHomeActivity.class);
                startActivity(intent);
                dialogInterface.dismiss();
            }
        });
        builder.setCancelable(false);

      AlertDialog dialog = builder.show();
        //“确”定按钮字体颜色
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_e2231a));
//”取消“按钮字体颜色
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_e2231a));
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_e2231a));
//”取消“按钮字体颜色
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_e2231a));
        }

    }

}
