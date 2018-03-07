package com.askey.mobile.zwave.control.login.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.guideSetting.ui.SetupHomeActivity;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askey.mobile.zwave.control.util.ToastShow;
import com.askeycloud.sdk.auth.response.OAuthProvider;
import com.askeycloud.webservice.sdk.api.builder.auth.UserSignInBuilder;
import com.askeycloud.webservice.sdk.model.ServicePreference;
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
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initView();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resetView();
    }

    private void resetView() {
        emailText = (String) PreferencesUtils.get(mContext,"userName","");
        passwordText = (String) PreferencesUtils.get(mContext,"password","");
        if(!emailText.equals("")){
            email.setText(emailText);
        }
        if(!passwordText.equals("")){
            password.setText(passwordText);
        }

        String emailTemp = getIntent().getStringExtra("email");
        String passwordTemp = getIntent().getStringExtra("password");
        if (emailTemp != null && !"".equals(emailTemp)) {
            if (passwordTemp != null && !"".equals(passwordTemp)) {
                email.setText(emailTemp);
                password.setText(passwordTemp);
            }
        }

        if (password.length() > 0 && email.length() > 0) {
            ivLogIn.setVisibility(View.VISIBLE);
        } else {
            ivLogIn.setVisibility(View.INVISIBLE);
        }

        email.setCursorVisible(false);
        password.setCursorVisible(false);
        email.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    email.setCursorVisible(true);
                }
                return false;
            }
        });
        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    password.setCursorVisible(true);
                }
                return false;
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (password.length() > 0 && email.length() > 0) {
                    ivLogIn.setVisibility(View.VISIBLE);
                } else {
                    ivLogIn.setVisibility(View.INVISIBLE);
                }

                if (editable.length() > 0) {
                    email.setCursorVisible(true);
                } else {
                    email.setCursorVisible(false);
                }
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (password.length() > 0 && email.length() > 0) {
                    ivLogIn.setVisibility(View.VISIBLE);
                } else {
                    ivLogIn.setVisibility(View.INVISIBLE);
                }

                if (editable.length() > 0) {
                    email.setCursorVisible(true);
                }   else {
                    email.setCursorVisible(false);
                }
            }
        });
    }

    private void initView() {
        mContext = this;
        layout = (LinearLayout) findViewById(R.id.activity_login_page);
        email = (EditText) findViewById(R.id.et_log_in);
        password = (EditText) findViewById(R.id.et_password);
        ivLogIn = (ImageView) findViewById(R.id.iv_log_in);
        mLeft = (ImageView) findViewById(R.id.iv_left);
        forgotPassword = (TextView) findViewById(R.id.tv_forgot_password);
        ivLogIn.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        mLeft.setOnClickListener(this);
        layout.setOnClickListener(this);

        resetView();

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
                    String str = mContext.getResources().getString(R.string.log_in_wait_toast);
                    showWaitingDialog();
                    goLogIn();
                } else {
                    //账号或密码不能为空
                    ToastShow.showToast(mContext,getResources().getString(R.string.account_is_not_null));
                }
                break;
            case R.id.tv_forgot_password:
                intent = new Intent(mContext,LogInHomeActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_left:
                finish();
                break;
            case R.id.activity_login_page:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive()) {
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
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
                        stopWaitDialog();
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getAccessToken==" + oAuthResultModel.getAccessToken());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getRefreshToken==" + oAuthResultModel.getRefreshToken());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getTokenType==" + oAuthResultModel.getTokenType());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getUserid==" + oAuthResultModel.getUserSignInResponse().getUserid());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getEmail==" + oAuthResultModel.getUserSignInResponse().getEmail());
                        PreferencesUtils.put(mContext,"userid",oAuthResultModel.getUserSignInResponse().getUserid());
                        PreferencesUtils.put(mContext,"userName",emailText);
                        PreferencesUtils.put(mContext,"password",passwordText);
//                        Intent  intent = new Intent(LogInActivity.this, DeviceGuideActivity.class);
//                        startActivity(intent);
                        Intent  intent = new Intent(LogInActivity.this, SetupHomeActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onOAuthResultError(Type type, String s) {
                        Logg.i(LOG_TAG,"======onOAuthResultError===type===" + type);
                        Logg.i(LOG_TAG,"======onOAuthResultError===s===" + s);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopWaitDialog();
                                showVerificationPopu();
                            }
                        });
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
        PopupWindow popupWindow = new PopupWindow(popupView,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setFocusable(true);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView content = (TextView) popupView.findViewById(R.id.tv_content);
        content.setText(getResources().getString(R.string.invalid_eamil_password));
//        LinearLayout linearLayout = (LinearLayout) popupView.findViewById(R.id.ll_popu);
//        linearLayout.setBackground(ContextCompat.getDrawable(mContext,R.drawable.vector_drawable_ic_125));

        int[] location = new int[2];
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight =  popupView.getMeasuredHeight();
        email.getLocationOnScreen(location);
        popupWindow.showAtLocation(email, Gravity.NO_GRAVITY, ((location[0]+email.getWidth()/2)-popupWidth/2)/2,
                location[1]-popupHeight - email.getHeight() * 2);
    }

}
