package com.askey.mobile.zwave.control.login.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.interf.FragmentCallback;
import com.askey.mobile.zwave.control.interf.FragmentPage;
import com.askeycloud.sdk.auth.response.BasicUserOAuthResponse;
import com.askeycloud.webservice.sdk.api.ApiStatus;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;

import java.util.Map;

/**
 * Created by skysoft on 2017/10/11.
 */

public class LogInSecondFragment extends BaseFragment implements View.OnClickListener{
    private final String LOG_TAG = LogInSecondFragment.class.getSimpleName();
    private View view;
    private EditText recoveryCode;
    private ImageView recoveryIcon,left;
    private LogInHomeActivity logInHomeActivity;
    public Map<String,String> account;
    private TextView email,requestNewCode;
    private FragmentManager fm;
    private int oneFragmentTag = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_log_in_second, null);

        initView();

        FragmentPage.getInstance().setPageCallback(new FragmentCallback() {
            @Override
            public void handlePage() {
                if (logInHomeActivity.isNextPage) {
                    logInHomeActivity.goNextPage(null);
                    account.put("recovery_code", recoveryCode.getText().toString());
                } else {
                    if (!"".equals(recoveryCode.getText().toString())) {
                        showWaitingDialog();
                        //
                        checkResetCode();
                    }
                }
            }
        });

        recoveryCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    recoveryCode.setCursorVisible(true);
                } else {

                    recoveryCode.setCursorVisible(false);
                }
                if (recoveryCode.getVisibility() == View.VISIBLE) {
                    logInHomeActivity.right.setImageResource(R.drawable.vector_drawable_ic_next_solid);
                } else {

                    logInHomeActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
                }
            }
        });
        return view;
    }

    private void checkResetCode() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                final BasicUserOAuthResponse response = AskeyApiAuthService.getInstance(logInHomeActivity)
                        .checkPwdResetCode(recoveryCode.getText().toString());
                Log.i(LOG_TAG, "=====checkResetCode==========getCode========" + response.getCode());
                Log.i(LOG_TAG, "=====checkResetCode==========getMessage========" + response.getMessage());
                Log.i(LOG_TAG, "=====checkResetCode==========getAdditionalProperties========" + response.getAdditionalProperties());

                logInHomeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ApiStatus.API_SUCCESS == response.getCode()) {
                            logInHomeActivity.isNextPage = true; recoveryIcon.setVisibility(View.VISIBLE);

                        } else {
                            logInHomeActivity.isNextPage = false;
                            recoveryIcon.setVisibility(View.INVISIBLE);
                        }
                        stopWaitDialog();
                    }
                });

            }
        }).start();

    }


    private void initView() {
        logInHomeActivity = (LogInHomeActivity) getActivity();
        fm  = getActivity().getSupportFragmentManager();
        String tag = oneFragmentTag + "";
        LogInOneFragment logInOneFragment = (LogInOneFragment) fm.findFragmentByTag(tag);
        account = logInOneFragment.account;

        recoveryCode = (EditText) view.findViewById(R.id.et_recovery_code);
        email = (TextView) view.findViewById(R.id.tv_email);
        requestNewCode = (TextView) view.findViewById(R.id.tv_request_new_code);
        requestNewCode.setOnClickListener(this);
        recoveryIcon = (ImageView) view.findViewById(R.id.iv_recovery_icon);
        left = (ImageView) view.findViewById(R.id.iv_left);
        left.setOnClickListener(this);
        logInHomeActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
        email.setText(account.get("email"));

        recoveryCode.setCursorVisible(false);
        recoveryCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    recoveryCode.setCursorVisible(true);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                logInHomeActivity.onBackPressed();
                break;
            case R.id.tv_request_new_code:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BasicUserOAuthResponse response = AskeyApiAuthService.getInstance(logInHomeActivity)
                                .forgotUserPassword(
                                        account.get("email")
                                );
                        Log.i(LOG_TAG, "========forgotUserPassword=====" + response.getCode());
                    }
                }).start();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(logInHomeActivity.right.getVisibility() == View.GONE) {
            logInHomeActivity.right.setVisibility(View.VISIBLE);
        }
    }
}
