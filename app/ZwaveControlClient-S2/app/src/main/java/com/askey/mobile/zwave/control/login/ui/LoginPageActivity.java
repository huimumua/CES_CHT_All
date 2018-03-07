package com.askey.mobile.zwave.control.login.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

public class LoginPageActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvLogIn,tvSignUp;
    private RelativeLayout layoutLogIn, layoutSignUp;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

//        tvLogIn = (TextView) findViewById(R.id.tv_log_in);
//        tvSignUp = (TextView) findViewById(R.id.tv_sign_up);
        layoutLogIn = (RelativeLayout) findViewById(R.id.layout_login);
        layoutSignUp = (RelativeLayout) findViewById(R.id.layout_signup);
        layoutLogIn.setOnClickListener(this);
        layoutSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_login:
                intent = new Intent(this,LogInActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
                break;
            case R.id.layout_signup:
                intent = new Intent(this,SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }
}
