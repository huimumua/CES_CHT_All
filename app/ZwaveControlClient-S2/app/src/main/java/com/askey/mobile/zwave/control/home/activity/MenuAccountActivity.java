package com.askey.mobile.zwave.control.home.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.login.ui.LogInActivity;
import com.askey.mobile.zwave.control.login.ui.LoginPageActivity;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askeycloud.sdk.auth.response.BasicUserOAuthResponse;
import com.askeycloud.webservice.sdk.api.ApiStatus;
import com.askeycloud.webservice.sdk.model.ServicePreference;
import com.askeycloud.webservice.sdk.model.auth.v3.OAuthResultModel;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;
import com.askeycloud.webservice.sdk.service.web.AskeyWebService;

import static com.askeycloud.webservice.sdk.model.ServicePreference.getAuthV3UserDataModel;

public class MenuAccountActivity extends BaseActivity implements View.OnClickListener{
    private final String LOG_TAG = MenuAccountActivity.class.getSimpleName();
    private TextView password;
    private ImageView passwordIcon;
    private Button logout;
    private RelativeLayout passwordLayout;
    private boolean passwordCansee = false;

    private RelativeLayout newPasswordLayout;
    private EditText newPassword;
    private ImageView newPasswordIcon;

    private TextView confirm;
    private RelativeLayout confirmLayout;
    private EditText confirmPassword;
    private ImageView confirmIcon;

    private TextView changePassword;

    private int currentStatus;

    private ImageView left;

    private String mEmail, mPassword;

    private TextView userName;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_account);
        mContext = this;
        currentStatus = 1;
        mEmail = (String) PreferencesUtils.get(this,"userName","");
        mPassword = (String) PreferencesUtils.get(this,"password","");

        userName = (TextView) findViewById(R.id.tv_username);

        password = (TextView) findViewById(R.id.tv_password);
        passwordIcon = (ImageView) findViewById(R.id.iv_passwordIcon);
        logout = (Button) findViewById(R.id.btn_logout);
        passwordLayout = (RelativeLayout) findViewById(R.id.rl_password);
        passwordIcon.setOnClickListener(this);
        logout.setOnClickListener(this);
        passwordLayout.setOnClickListener(this);

        newPasswordLayout = (RelativeLayout) findViewById(R.id.rl_newpassword);
        newPassword = (EditText) findViewById(R.id.et_newpassword);
        newPasswordIcon = (ImageView) findViewById(R.id.iv_newpasswordIcon);
        confirm = (TextView) findViewById(R.id.tv_confirm);
        confirmLayout = (RelativeLayout) findViewById(R.id.rl_confirmpassword);
        confirmPassword = (EditText) findViewById(R.id.et_confirmpassword);
        confirmIcon = (ImageView) findViewById(R.id.iv_confirmIcon);

        changePassword = (TextView) findViewById(R.id.tv_changepassword);

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 8) {
                    newPasswordIcon.setVisibility(View.VISIBLE);
                } else {
                    newPasswordIcon.setVisibility(View.INVISIBLE);
                }

                if (newPasswordIcon.getVisibility() == View.VISIBLE && confirmIcon.getVisibility() == View.VISIBLE) {
                    logout.setBackgroundResource(R.drawable.red_rectangle);
                    logout.setTextColor(getResources().getColor(R.color.white,null));
                    logout.setText(getResources().getString(R.string.activity_account_continue));
                    logout.setClickable(true);
                } else {
                    logout.setBackgroundResource(R.drawable.gray_rectangle);
                    logout.setTextColor(getResources().getColor(R.color.white,null));
                    logout.setText(getResources().getString(R.string.activity_account_logout));
                    logout.setClickable(false);
                }
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if ((newPassword.getText().toString().equals(confirmPassword.getText().toString()))) {
                    confirmIcon.setVisibility(View.VISIBLE);
                } else {
                    confirmIcon.setVisibility(View.INVISIBLE);
                }

                if (newPasswordIcon.getVisibility() == View.VISIBLE && confirmIcon.getVisibility() == View.VISIBLE) {
                    logout.setBackgroundResource(R.drawable.red_rectangle);
                    logout.setTextColor(getResources().getColor(R.color.white,null));
                    logout.setText(getResources().getString(R.string.activity_account_continue));
                    logout.setClickable(true);
                } else {
                    logout.setBackgroundResource(R.drawable.gray_rectangle);
                    logout.setTextColor(getResources().getColor(R.color.white,null));
                    logout.setText(getResources().getString(R.string.activity_account_logout));
                    logout.setClickable(false);
                }
            }
        });

        left = (ImageView) findViewById(R.id.iv_left);
        left.setOnClickListener(this);
        refreshPasswordView();

        userName.setText(mEmail);
        password.setText(mPassword);

    }

    private void refreshPasswordView() {

        passwordLayout.setVisibility(View.VISIBLE);

        newPasswordLayout.setVisibility(View.GONE);

        confirm.setVisibility(View.GONE);
        confirmLayout.setVisibility(View.GONE);

        changePassword.setVisibility(View.GONE);

        logout.setBackgroundResource(R.drawable.stroke_rectangle_red);
        logout.setTextColor(getResources().getColor(R.color.text_read,null));
        logout.setText(getResources().getString(R.string.activity_account_logout));
        logout.setClickable(true);
        currentStatus = 1;


    }
    private void refreshNewPasswordView() {

        passwordLayout.setVisibility(View.GONE);

        newPasswordLayout.setVisibility(View.VISIBLE);

        confirm.setVisibility(View.VISIBLE);
        confirmLayout.setVisibility(View.VISIBLE);

        changePassword.setVisibility(View.GONE);

        logout.setBackgroundResource(R.drawable.gray_rectangle);
        logout.setTextColor(getResources().getColor(R.color.white,null));
        logout.setClickable(false);
        currentStatus = 2;


    }
    private void refreshChangePasswordView() {
        password.setText((String) PreferencesUtils.get(this,"password",""));
        passwordLayout.setVisibility(View.VISIBLE);

        newPasswordLayout.setVisibility(View.GONE);

        confirm.setVisibility(View.GONE);
        confirmLayout.setVisibility(View.GONE);

        changePassword.setVisibility(View.VISIBLE);

        logout.setBackgroundResource(R.drawable.stroke_rectangle_red);
        logout.setTextColor(getResources().getColor(R.color.text_read,null));
        logout.setText(getResources().getString(R.string.activity_account_logout));
        logout.setClickable(true);
        currentStatus = 3;


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_passwordIcon:
                if (passwordCansee) {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordIcon.setImageResource(R.drawable.vector_drawable_ic_unlock);
                    passwordCansee=false;
                } else {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordCansee=true;
                    passwordIcon.setImageResource(R.drawable.vector_drawable_ic_unlock_red);
                }
                break;
            case R.id.btn_logout:
                if (currentStatus == 2) {
                    changeUserPassword();
                } else {
                    HomeActivity.shadowTopic = "";
                    ServicePreference.revokeToken(mContext);
                    AskeyApiAuthService.getInstance(mContext).revoke();
                    TcpClient.getInstance().disconnect();
                    MQTTManagement.getSingInstance().closeMqtt();
                    AskeyIoTService.getInstance(appContext).disconnectIoTMQTTManager();
                    Intent intent = new Intent(this, LoginPageActivity.class);
                    startActivity(intent);
                }

                break;
            case R.id.rl_password:
                refreshNewPasswordView();
                break;
            case R.id.iv_left:
                if (currentStatus == 2) {
                    refreshPasswordView();
                } else {
                    finish();
                }
                break;
        }
    }

    private void changeUserPassword() {
        showWaitingDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String password = (String) PreferencesUtils.get(mContext,"password","");
                OAuthResultModel oAuthResultModel = ServicePreference.getAuthV3UserDataModel(mContext);
                try {
                    Log.i(LOG_TAG, "=======changeUserPassword=====oAuthResultModel=====" + oAuthResultModel);
//                    String userId = oAuthResultModel.getUserSignInResponse().getUserid();
                    String userId = "";
                    if( userId.equals("") || userId == null ){
                        userId = (String) PreferencesUtils.get(mContext,"userid","");
                    }
                    final BasicUserOAuthResponse response = AskeyApiAuthService.getInstance(mContext)
                            .changeUserPassword(
                                    userId,
                                    password,
                                    newPassword.getText().toString()
                            );

                    Log.i(LOG_TAG, "=======changeUserPassword=====getUserid=====" + userId);
                    Log.i(LOG_TAG, "=======changeUserPassword=====password=====" + password);
                    Log.i(LOG_TAG, "=======changeUserPassword=====getCode=====" + response.getCode());
                    Log.i(LOG_TAG, "=======changeUserPassword=====getMessage=====" + response.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ApiStatus.API_SUCCESS == response.getCode()) {
                                PreferencesUtils.put(mContext, "password", newPassword.getText().toString());
                                refreshChangePasswordView();
                            }
                            stopWaitDialog();
                        }
                    });
                } catch (Exception e) {
                    Log.i(LOG_TAG, "=======changeUserPassword=====Exception=====" + e.getMessage());
                }


            }
        }).start();
    }
}
