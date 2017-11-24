package com.askey.mobile.zwave.control.login.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.guideSetting.ui.DeviceGuideActivity;
import com.askey.mobile.zwave.control.guideSetting.ui.SetupHomeActivity;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.sdk.auth.response.OAuthProvider;
import com.askeycloud.webservice.sdk.api.builder.auth.UserSignInBuilder;
import com.askeycloud.webservice.sdk.model.auth.v3.OAuthResultModel;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;
import com.askeycloud.webservice.sdk.task.OAuthServiceCallback;

public class LogInActivity extends BaseActivity implements View.OnClickListener{
    private final String LOG_TAG = LogInActivity.class.getSimpleName();
    private EditText email,password;
    private ImageView ivLogIn;
    private String emailText,passwordText;
    private Context mContext;
    private TextView forgotPassword;
    private ImageView mLeft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initView();
    }

    private void initView() {
        mContext = this;
        email = (EditText) findViewById(R.id.et_log_in);
        password = (EditText) findViewById(R.id.et_password);
        ivLogIn = (ImageView) findViewById(R.id.iv_log_in);
        mLeft = (ImageView) findViewById(R.id.iv_left);
        forgotPassword = (TextView) findViewById(R.id.tv_forgot_password);
        ivLogIn.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        mLeft.setOnClickListener(this);

        String emailTemp = getIntent().getStringExtra("email");
        String passwordTemp = getIntent().getStringExtra("password");
        if (emailTemp != null && !"".equals(emailTemp)) {
            if (passwordTemp != null && !"".equals(passwordTemp)) {
                email.setText(emailTemp);
                password.setText(passwordTemp);
            }
        }

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.iv_log_in:
                emailText = email.getText().toString();
                passwordText = password.getText().toString();
                if (!"".equals(emailText) && !"".equals(passwordText)) {
                    //登录 并判断是否成功
                    goLogIn();

                } else {
                    //账号或密码不能为空
                }
                break;
            case R.id.tv_forgot_password:
                intent = new Intent(this,LogInHomeActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_left:
                finish();
                break;
        }
    }

    private void goLogIn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OAuthServiceCallback oAuthServiceCallback = new OAuthServiceCallback() {
                    @Override
                    public void onGetOAuthProviders(OAuthProvider[] oAuthProviders) {
                        Logg.i(LOG_TAG,"======OAuthProvider======" + oAuthProviders);
                    }

                    @Override
                    public void onOAuthResultSuccess(OAuthResultModel oAuthResultModel) {

                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getAccessToken==" + oAuthResultModel.getAccessToken());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getRefreshToken==" + oAuthResultModel.getRefreshToken());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getTokenType==" + oAuthResultModel.getTokenType());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getUserid==" + oAuthResultModel.getUserSignInResponse().getUserid());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getEmail==" + oAuthResultModel.getUserSignInResponse().getEmail());
                      Intent  intent = new Intent(LogInActivity.this, DeviceGuideActivity.class);
                        startActivity(intent);
//                        Intent  intent = new Intent(LogInActivity.this, SetupHomeActivity.class);
//                        startActivity(intent);
                    }

                    @Override
                    public void onOAuthResultError(Type type, String s) {
                        Logg.i(LOG_TAG,"======onOAuthResultError===type===" + type);
                        Logg.i(LOG_TAG,"======onOAuthResultError===s===" + s);
                        showVerificationPopu();
                    }
                };

                UserSignInBuilder builder = new UserSignInBuilder(
                        emailText,
                        passwordText
                );


                AskeyApiAuthService.getInstance(mContext).userSignIn(builder, oAuthServiceCallback);


            }
        }).start();
    }

    private void showVerificationPopu() {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_verification_view, null);
        PopupWindow popupWindow = new PopupWindow(mContext);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setContentView(popupView);
        popupWindow.setFocusable(true);

        TextView content = (TextView) popupView.findViewById(R.id.tv_content);
        content.setText(getResources().getString(R.string.invaild_eamil));
        LinearLayout linearLayout = (LinearLayout) popupView.findViewById(R.id.ll_popu);
        linearLayout.setBackgroundResource(R.drawable.vector_drawable_ic_125);

        int[] location = new int[2];
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight =  popupView.getMeasuredHeight();
        email.getLocationOnScreen(location);
        popupWindow.showAtLocation(email, Gravity.NO_GRAVITY, ((location[0]+email.getWidth()/2)-popupWidth/2)/2,
                location[1]-popupHeight - email.getHeight() * 2);
    }

}
