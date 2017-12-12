package com.askey.mobile.zwave.control.welcome.ui;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.login.ui.LoginPageActivity;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener{

//    private RadiusDialog mDialog;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        showDialog();
    }

    //点击按钮，弹出圆角对话框
    public void showDialog() {
        View contentView = this.getLayoutInflater().inflate(R.layout.dialog_notification, null);
//        mDialog = new RadiusDialog(this, 200, 100, contentView, R.style.notification_dialog);
        mDialog = new Dialog(this, R.style.notification_dialog);
        mDialog.setContentView(contentView);
        mDialog.setCancelable(false);
        mDialog.show();
        Button btnCancel = (Button) contentView.findViewById(R.id.btn_cancel);
        Button btnPositive = (Button) contentView.findViewById(R.id.btn_positive);
        btnCancel.setOnClickListener(this);
        btnPositive.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_cancel:

                mDialog.dismiss();
                break;
            case R.id.btn_positive:
                intent = new Intent(this,LoginPageActivity.class);
                startActivity(intent);
                mDialog.dismiss();
                finish();
                break;
        }
    }
}
