package com.askey.mobile.zwave.control.login.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.interf.FragmentPage;
import com.askey.mobile.zwave.control.interf.FragmentCallback;
import com.askeycloud.sdk.auth.response.BasicUserOAuthResponse;
import com.askeycloud.webservice.sdk.api.ApiStatus;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by skysoft on 2017/10/11.
 */

public class LogInOneFragment extends Fragment implements View.OnClickListener{
    private final String LOG_TAG = LogInOneFragment.class.getSimpleName();
    private View view;
    private EditText email;
    private ImageView ivEmail,left;
    private LogInHomeActivity logInHomeActivity;
    private boolean isSended;
    public Map<String,String> account;
    private LinearLayout llEmail;
    private Context mContext;
    private int[] location = new int[2];
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_log_in_one, null);

        initView();

        FragmentPage.getInstance().setPageCallback(new FragmentCallback() {
            @Override
            public void handlePage() {

                Log.i(LOG_TAG, "=====handlePage=======");

                if (logInHomeActivity.isNextPage) {
                    logInHomeActivity.goNextPage(null);
                }

                if (!isSended) {
                    forgotPassword();
                } else {
                    //提示已经发到邮箱
                }
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
                if (isEmail(email.getText().toString())) {
                    ivEmail.setVisibility(View.VISIBLE);
                } else {
                    ivEmail.setVisibility(View.INVISIBLE);
                }
            }
        });
        return view;
    }

    private void forgotPassword() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final BasicUserOAuthResponse response = AskeyApiAuthService.getInstance(logInHomeActivity)
                        .forgotUserPassword(
                                email.getText().toString()
                        );

                logInHomeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getCode() == ApiStatus.API_SUCCESS) {
                            isSended = true;
                            logInHomeActivity.isNextPage = true;
                            logInHomeActivity.right.setImageResource(R.drawable.vector_drawable_ic_next_solid);
                            account.put("email", email.getText().toString());
                        } else {
                            //提示什么错误
                            isSended = false;
                            logInHomeActivity.isNextPage = false;
                            logInHomeActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
                        }
                        Log.i(LOG_TAG, "===forgotPassword========getCode====" + response.getCode());
                        Log.i(LOG_TAG, "===forgotPassword========getMessage====" + response.getMessage());
                        Log.i(LOG_TAG, "===forgotPassword========getStatus====" + response.getStatus());
                    }
                });
            }
        }).start();
    }

    private void initView() {
        mContext = getActivity();
        logInHomeActivity = (LogInHomeActivity) getActivity();
        llEmail = (LinearLayout) view.findViewById(R.id.ll_email);
        email = (EditText) view.findViewById(R.id.et_email_adress);
        ivEmail = (ImageView) view.findViewById(R.id.iv_email);
        left = (ImageView) view.findViewById(R.id.iv_left);
        left.setOnClickListener(this);
        logInHomeActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
        account = new HashMap<>();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                logInHomeActivity.onBackPressed();
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

    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private void showInvaildPopu() {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_verification_invaild, null);
        PopupWindow popupWindow = new PopupWindow(mContext);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setContentView(popupView);
        popupWindow.setFocusable(true);

        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight =  popupView.getMeasuredHeight();
        llEmail.getLocationOnScreen(location);
        popupWindow.showAtLocation(llEmail, Gravity.NO_GRAVITY, ((location[0]+llEmail.getWidth()/2)-popupWidth/2)/2,
                location[1]-popupHeight - llEmail.getHeight() * 2);
    }
}
