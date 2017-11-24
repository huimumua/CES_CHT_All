package com.askey.mobile.zwave.control.login.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.util.Const;
import com.askeycloud.sdk.auth.response.BasicUserOAuthResponse;
import com.askeycloud.sdk.auth.response.OAuthProvider;
import com.askeycloud.webservice.sdk.api.ApiStatus;
import com.askeycloud.webservice.sdk.model.auth.v3.OAuthResultModel;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;
import com.askeycloud.webservice.sdk.service.web.AskeyWebService;
import com.askeycloud.webservice.sdk.task.OAuthServiceCallback;

import java.util.Map;

/**
 * Created by skysoft on 2017/10/11.
 */

public class LogInThirdFragment extends Fragment implements View.OnClickListener{
    private final String LOG_TAG = LogInThirdFragment.class.getSimpleName();
    private View view;
    private FragmentManager fm;
    private int secondFragmentTag = 1;
    private EditText newPassword;
    private Button btnLogIn;
    private ImageView passwordIcon;
    private LogInHomeActivity logInHomeActivity;
    private Map<String,String> account;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_log_in_third, null);

        initView();

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 6) {
                    passwordIcon.setVisibility(View.VISIBLE);
                } else {
                    passwordIcon.setVisibility(View.INVISIBLE);
                }
            }
        });
        return view;
    }

    private void initView() {
        logInHomeActivity = (LogInHomeActivity) getActivity();
        fm  = getActivity().getSupportFragmentManager();
        String tag = secondFragmentTag + "";
        LogInSecondFragment logInsecondFragment = (LogInSecondFragment) fm.findFragmentByTag(tag);
        account = logInsecondFragment.account;
        newPassword = (EditText) view.findViewById(R.id.et_email_adress);
        btnLogIn = (Button) view.findViewById(R.id.btn_log_in);
        passwordIcon = (ImageView) view.findViewById(R.id.iv_enter_email);
        btnLogIn.setOnClickListener(this);
        logInHomeActivity.right.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                logInHomeActivity.onBackPressed();
                break;
            case R.id.btn_log_in:
                if (logInHomeActivity.isNextPage) {
                    logInHomeActivity.goNextPage(account);
                } else {
                    goLogIn();
                }
                break;
        }
    }

    private void goLogIn() {
        if (!"".equals(newPassword.getText().toString())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final BasicUserOAuthResponse response = AskeyApiAuthService.getInstance(logInHomeActivity)
                            .resetUserPassword(
                                    account.get("recovery_code"),
                                    newPassword.getText().toString()
                            );

                    logInHomeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (response.getCode() == ApiStatus.API_SUCCESS) {
                                refreshToken();

                                logInHomeActivity.isNextPage = true;
                                account.put("password", newPassword.getText().toString());
                                btnLogIn.setBackgroundColor(getResources().getColor(R.color.red));
                            } else {
                                logInHomeActivity.isNextPage = false;
                                btnLogIn.setBackgroundColor(getResources().getColor(R.color.gray));
                            }
                        }
                    });
                    Log.i(LOG_TAG, "=======resetUserPassword========getCode======" + response.getCode());
                    Log.i(LOG_TAG, "=======resetUserPassword========getStatus======" + response.getStatus());
                    Log.i(LOG_TAG, "=======resetUserPassword========getMessage======" + response.getMessage());
                    Log.i(LOG_TAG, "=======resetUserPassword========getAdditionalProperties======" + response.getAdditionalProperties());
                    Log.i(LOG_TAG, "=======resetUserPassword========getAddtionMessage======" + response.getAddtionMessage());
                }
            }).start();

        } else {
            //password 不能为空
        }
    }

    private void refreshToken() {
        OAuthServiceCallback oAuthServiceCallback = new OAuthServiceCallback() {
            @Override
            public void onGetOAuthProviders(OAuthProvider[] oAuthProviders) {
                Log.i(LOG_TAG, "=========onGetOAuthProviders=======");
            }

            @Override
            public void onOAuthResultSuccess(OAuthResultModel oAuthResultModel) {
                Log.i(LOG_TAG, "=========onOAuthResultSuccess=======");
                Log.i(LOG_TAG, "=========onOAuthResultSuccess====getRefreshToken===" + oAuthResultModel.getRefreshToken());
            }

            @Override
            public void onOAuthResultError(Type type, String s) {
                Log.i(LOG_TAG, "=========onOAuthResultError=======");
            }
        };

        AskeyWebService.getInstance(logInHomeActivity)
                .refreshToken(
                        Const.AUTH_APP_ID,
                        oAuthServiceCallback
                );
    }
}
