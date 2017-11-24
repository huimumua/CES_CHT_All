package com.askey.mobile.zwave.control.login.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.interf.FragmentCallback;
import com.askey.mobile.zwave.control.interf.FragmentPage;
import com.askeycloud.sdk.auth.response.BasicUserOAuthResponse;
import com.askeycloud.sdk.auth.response.UserSignUpResponse;
import com.askeycloud.webservice.sdk.api.ApiStatus;
import com.askeycloud.webservice.sdk.api.builder.auth.UserSignUpBuilder;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;

import java.util.Map;

import lecho.lib.hellocharts.model.Line;

/**
 * Created by skysoft on 2017/10/10.
 */

public class SignUpThirdFragment extends Fragment implements View.OnClickListener{
    private final String LOG_TAG = SignUpThirdFragment.class.getSimpleName();
    private FragmentManager fm;
    private Context mContext;
    public Map<String, String> account;
    private EditText verification;
    private ImageView verificationIcon,left;
    private SignUpActivity signUpActivity;
    private int verifySuccessCode;
    private TextView email,requestNewCode;
    private View view;
    private int[] location = new int[2];
    private LinearLayout llVerification;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up_third, null);

        initView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                goSignUp();
            }
        }).start();

        FragmentPage.getInstance().setPageCallback(new FragmentCallback() {
            @Override
            public void handlePage() {
                Log.i(LOG_TAG, "=====handlePage=======");
                if (!verification.getText().toString().equals("")) {
                    triggerUserConfirm();
                } else {
                    //提示验证码已经发到邮箱
                    showVerificationPopu();
                }

                if((signUpActivity.isNextPage)) {
                    signUpActivity.goNextPage(account);
                }

            }
        });

        verification.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    verificationIcon.setImageResource(R.drawable.vector_drawable_ic_76);
                    verificationIcon.setVisibility(View.VISIBLE);
                } else {
                    verificationIcon.setVisibility(View.INVISIBLE);
                }

            }
        });
        return view;
    }

    private void showVerificationPopu() {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_verification_view, null);
        PopupWindow popupWindow = new PopupWindow(mContext);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setContentView(popupView);
        popupWindow.setFocusable(true);
        TextView tvContent = (TextView) popupView.findViewById(R.id.tv_content);
        tvContent.setText(getResources().getString(R.string.verification_sent));
        LinearLayout linearLayout = (LinearLayout) popupView.findViewById(R.id.ll_popu);
        linearLayout.setBackgroundResource(R.drawable.verification_background);

        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight =  popupView.getMeasuredHeight();
        signUpActivity.right.getLocationOnScreen(location);
        popupWindow.showAtLocation(signUpActivity.right, Gravity.NO_GRAVITY, ((location[0]+signUpActivity.right.getWidth()/2)-popupWidth/2)/2,
                location[1]-popupHeight - signUpActivity.right.getHeight() * 2);
    }

    private void initView() {
        mContext = getActivity();
        fm  = getActivity().getSupportFragmentManager();
        String tag = 1 + "";
        SignUpSecondFragment second = (SignUpSecondFragment) fm.findFragmentByTag(tag);
        account = second.getAccount();

        llVerification = (LinearLayout) view.findViewById(R.id.ll_verification);
        verification = (EditText) view.findViewById(R.id.et_verification);
        email = (TextView) view.findViewById(R.id.tv_email);
        requestNewCode = (TextView) view.findViewById(R.id.tv_request_new_code);
        email.setText(account.get("email"));
        verificationIcon = (ImageView) view.findViewById(R.id.iv_verification_icon);
        left = (ImageView) view.findViewById(R.id.iv_left);
        left.setOnClickListener(this);

        signUpActivity =  ((SignUpActivity) getActivity());
        signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
    }

    private void goSignUp() {
        Log.i(LOG_TAG, "=====goSignUp===email====" + account.get("email"));
        Log.i(LOG_TAG, "=====goSignUp===password====" + account.get("password"));
        Log.i(LOG_TAG, "=====goSignUp===nickname====" + account.get("nickname"));
        UserSignUpBuilder builder = new UserSignUpBuilder(
                account.get("email"),
                account.get("password"),
                account.get("nickname")
        );
        UserSignUpResponse signUpResponse = AskeyApiAuthService.getInstance(mContext).userSignUp(builder);
        Log.i(LOG_TAG, "=====goSignUp=====getCode==" + signUpResponse.getCode());
        Log.i(LOG_TAG, "=====goSignUp===getUserid====" + signUpResponse.getUserid());
        Log.i(LOG_TAG, "=====goSignUp===getMessage====" + signUpResponse.getMessage());
    }

    private void triggerUserConfirm() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String confirmCode = verification.getText().toString();
                BasicUserOAuthResponse response = AskeyApiAuthService.getInstance(mContext).triggerUserConfirm(
                        account.get("email"),
                        confirmCode
                );
                Log.i(LOG_TAG, "=====triggerUserConfirm=====getCode==" + response.getCode());
                Log.i(LOG_TAG, "=====triggerUserConfirm=====getMessage==" + response.getMessage());
                verifySuccessCode = response.getCode();

                signUpActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ApiStatus.API_SUCCESS == verifySuccessCode) {
                            signUpActivity.isNextPage = true;
                            signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_next_solid);
                            //还要改变右边图标
                        } else {
                            verificationIcon.setImageResource(R.drawable.vector_drawable_ic_close);//换成差的图标
                            showInvaildPopu();
                            signUpActivity.isNextPage = false;
                            signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
                        }

                    }
                });

            }
        }).start();
    }

    private void showInvaildPopu() {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_verification_invaild, null);
        PopupWindow popupWindow = new PopupWindow(mContext);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setContentView(popupView);
        popupWindow.setFocusable(true);

        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight =  popupView.getMeasuredHeight();
        llVerification.getLocationOnScreen(location);
        popupWindow.showAtLocation(llVerification, Gravity.NO_GRAVITY, ((location[0]+llVerification.getWidth()/2)-popupWidth/2)/2,
                location[1]-popupHeight - llVerification.getHeight() * 2);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                signUpActivity.onBackPressed();
                break;
            case R.id.tv_request_new_code:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BasicUserOAuthResponse response = AskeyApiAuthService
                                .getInstance(mContext)
                                .resentConfirmCode(account.get("email"));
                    }
                }).start();
                break;
        }
    }
}
