package com.askey.mobile.zwave.control.login.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.guideSetting.ui.SetupHomeActivity;
import com.askey.mobile.zwave.control.interf.FragmentCallback;
import com.askey.mobile.zwave.control.interf.FragmentPage;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askeycloud.sdk.auth.response.BasicUserOAuthResponse;
import com.askeycloud.sdk.auth.response.OAuthProvider;
import com.askeycloud.sdk.auth.response.UserSignUpResponse;
import com.askeycloud.webservice.sdk.api.ApiStatus;
import com.askeycloud.webservice.sdk.api.builder.auth.UserSignInBuilder;
import com.askeycloud.webservice.sdk.api.builder.auth.UserSignUpBuilder;
import com.askeycloud.webservice.sdk.model.auth.v3.OAuthResultModel;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;
import com.askeycloud.webservice.sdk.task.OAuthServiceCallback;

import java.util.Map;

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
    private RelativeLayout llVerification;
    private PopupWindow popupWindowOne;
    private PopupWindow popupWindowTwo;
    private ProgressDialog mProgressDialog;
    private ProgressBar pb;
    private LinearLayout llThird;
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

                if((signUpActivity.isNextPage)) {
                    signUpActivity.goNextPage(account);
                    return;
                }
                if (!verification.getText().toString().equals("")) {
                    if (pb == null) {
                        triggerUserConfirm();
                    }
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
                    verification.setCursorVisible(true);
                    verificationIcon.setImageResource(R.drawable.vector_drawable_ic_76);
                    verificationIcon.setVisibility(View.VISIBLE);
                } else {
                    verificationIcon.setVisibility(View.INVISIBLE);
                    verification.setCursorVisible(false);
                }

            }
        });
        return view;
    }

    private void showVerificationPopu() {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_verification_view, null);
        popupWindowOne = new PopupWindow(popupView,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popupWindowOne.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindowOne.setFocusable(true);
        popupWindowOne.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindowOne.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView tvContent = (TextView) popupView.findViewById(R.id.tv_content);
        tvContent.setText(getResources().getString(R.string.verification_sent));
        LinearLayout linearLayout = (LinearLayout) popupView.findViewById(R.id.ll_popu);
        linearLayout.setBackground(ContextCompat.getDrawable(mContext,R.drawable.verification_background));

        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight =  popupView.getMeasuredHeight();
        signUpActivity.right.getLocationOnScreen(location);
        popupWindowOne.showAtLocation(signUpActivity.right, Gravity.NO_GRAVITY, ((location[0]+signUpActivity.right.getWidth())-popupWidth)/2,
                location[1]-popupHeight - signUpActivity.right.getHeight() * 2);
    }

    private void initView() {
        mContext = getActivity();
        fm  = getActivity().getSupportFragmentManager();
        String tag = 1 + "";
        SignUpSecondFragment second = (SignUpSecondFragment) fm.findFragmentByTag(tag);
        account = second.getAccount();

        llThird = (LinearLayout) view.findViewById(R.id.activity_sign_up_third);
        llVerification = (RelativeLayout) view.findViewById(R.id.ll_verification);
        verification = (EditText) view.findViewById(R.id.et_verification);
        email = (TextView) view.findViewById(R.id.tv_email);
        requestNewCode = (TextView) view.findViewById(R.id.tv_request_new_code);
        email.setText(account.get("email"));
        verificationIcon = (ImageView) view.findViewById(R.id.iv_verification_icon);
        left = (ImageView) view.findViewById(R.id.iv_left);
        left.setOnClickListener(this);
        requestNewCode.setOnClickListener(this);

        verification.setCursorVisible(false);
        signUpActivity =  ((SignUpActivity) getActivity());
//        signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
        signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_next_solid);

        verification.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    verification.setCursorVisible(true);
                }
                return false;
            }
        });
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
        if (signUpResponse.getCode() == ApiStatus.API_SUCCESS) {
            signUpActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showVerificationPopu();
                }
            });

        }
    }

    private void triggerUserConfirm() {
        //弹框
        showProgressBar();
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
                        //弹窗消失
//                        mProgressDialog.dismiss();
                        if (ApiStatus.API_SUCCESS == verifySuccessCode) {
                            signUpActivity.isNextPage = true;
                            goLogIn();
                            //还要改变右边图标
                        } else {
                            if (pb != null) {
                                pb.setVisibility(View.GONE);
                                pb = null;
                            }
                            verificationIcon.setImageResource(R.drawable.ic_close);//换成差的图标
                            showInvaildPopu();
                            signUpActivity.isNextPage = false;
//                            signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
                        }

                    }
                });

            }
        }).start();
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pb != null) {
                                    pb.setVisibility(View.GONE);
                                    pb = null;
                                }
                            }
                        });

                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getAccessToken==" + oAuthResultModel.getAccessToken());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getRefreshToken==" + oAuthResultModel.getRefreshToken());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getTokenType==" + oAuthResultModel.getTokenType());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getUserid==" + oAuthResultModel.getUserSignInResponse().getUserid());
                        Logg.i(LOG_TAG,"======onOAuthResultSuccess===getEmail==" + oAuthResultModel.getUserSignInResponse().getEmail());
                        PreferencesUtils.put(mContext,"userName", account.get("email"));
                        PreferencesUtils.put(mContext,"password",account.get("password"));
//                        Intent  intent = new Intent(LogInActivity.this, DeviceGuideActivity.class);
//                        startActivity(intent);
                        Intent intent = new Intent(mContext, SetupHomeActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onOAuthResultError(Type type, String s) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pb != null) {
                                    pb.setVisibility(View.GONE);
                                    pb = null;
                                }
                            }
                        });
                        Logg.i(LOG_TAG,"======onOAuthResultError===type===" + type);
                        Logg.i(LOG_TAG,"======onOAuthResultError===s===" + s);
                    }
                };

                UserSignInBuilder builder = new UserSignInBuilder(
                        account.get("email"),
                        account.get("password")
                );


                AskeyApiAuthService.getInstance(mContext).userSignIn(builder, oAuthServiceCallback);


            }
        }).start();
    }

    private void showProgressBar() {
//        mProgressDialog = new ProgressDialog(mContext);
//        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.show();
        pb = new  ProgressBar(mContext, null,android.R.attr.progressBarStyleLarge);
        pb.setIndeterminate(true);
        pb.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        llThird.addView(pb, params);
    }

    private void showInvaildPopu() {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_verification_view, null);
        popupWindowTwo = new PopupWindow(popupView,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popupWindowTwo.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindowTwo.setFocusable(true);
        popupWindowTwo.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindowTwo.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView content = (TextView) popupView.findViewById(R.id.tv_content);
        content.setText(getResources().getString(R.string.verification_invaild));
//        LinearLayout linearLayout = (LinearLayout) popupView.findViewById(R.id.ll_popu);
//        linearLayout.setBackground(ContextCompat.getDrawable(mContext,R.drawable.vector_drawable_ic_125));

        int[] location = new int[2];
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight =  popupView.getMeasuredHeight();
        llVerification.getLocationOnScreen(location);
        popupWindowTwo.showAtLocation(llVerification, Gravity.NO_GRAVITY, ((location[0]+llVerification.getWidth()/2)-popupWidth/2)/2,
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
                        Log.i(LOG_TAG, "=======resentConfirmCode====getCode=====" + response.getCode());
                        Log.i(LOG_TAG, "=======resentConfirmCode====getCode=====" + response.getCode());
                        Log.i(LOG_TAG, "=======resentConfirmCode====getAdditionalProperties=====" + response.getAdditionalProperties());
                        Log.i(LOG_TAG, "=======resentConfirmCode====getStatus=====" + response.getStatus());
                    }
                }).start();
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (popupWindowOne != null) {
            popupWindowOne.dismiss();
        }
        if (popupWindowTwo != null) {
            popupWindowTwo.dismiss();
        }
    }
}
