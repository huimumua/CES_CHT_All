package com.askey.mobile.zwave.control.login.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;

public class LoginPageActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvLogIn,tvSignUp;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        tvLogIn = (TextView) findViewById(R.id.tv_log_in);
        tvSignUp = (TextView) findViewById(R.id.tv_sign_up);
        tvLogIn.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_log_in:
                intent = new Intent(this,LogInActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_sign_up:
                intent = new Intent(this,SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }
}
