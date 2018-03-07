package com.askey.mobile.zwave.control.login.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.interf.FragmentCallback;
import com.askey.mobile.zwave.control.interf.FragmentPage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by skysoft on 2017/10/10.
 */

public class SignUpOneFragment extends Fragment implements View.OnClickListener{
    private EditText etEmailAdress,tvPassword;
    private Map<String,String> account;
    private ImageView emailIcon,passwordIcon,left;
    private SignUpActivity signUpActivity;
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up_one, null);

        initView();

        FragmentPage.getInstance().setPageCallback(new FragmentCallback() {
            @Override
            public void handlePage() {

                if((signUpActivity.isNextPage)) {
                    signUpActivity.goNextPage(null);
                }

            }
        });

        etEmailAdress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isEmail(etEmailAdress.getText().toString())) {
                    emailIcon.setVisibility(View.VISIBLE);
                } else {
                    emailIcon.setVisibility(View.INVISIBLE);;
                }

                if (emailIcon.getVisibility() == View.VISIBLE && passwordIcon.getVisibility() == View.VISIBLE) {
                    signUpActivity.isNextPage = true;
                    signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_next_solid);
                } else {
                    signUpActivity.isNextPage = false;
                    signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
                }

                if (editable.length() <= 0) {
                    etEmailAdress.setCursorVisible(false);
                } else {
                    etEmailAdress.setCursorVisible(true);
                }
            }
        });

        tvPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 8) {
                    passwordIcon.setVisibility(View.VISIBLE);
                } else {
                    passwordIcon.setVisibility(View.INVISIBLE);
                }

                if (editable.length() <= 0) {
                    tvPassword.setCursorVisible(false);
                } else {
                    tvPassword.setCursorVisible(true);
                }

                if (emailIcon.getVisibility() == View.VISIBLE && passwordIcon.getVisibility() == View.VISIBLE) {
                    signUpActivity.isNextPage = true;
                    signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_next_solid);
                } else {
                    signUpActivity.isNextPage = false;
                    signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);
                }

            }
        });

        return view;
    }

    private void initView() {
        signUpActivity =  ((SignUpActivity) getActivity());
        etEmailAdress = (EditText) view.findViewById(R.id.et_email_adress);
        tvPassword = (EditText) view.findViewById(R.id.et_password);
        emailIcon = (ImageView) view.findViewById(R.id.iv_enter_email);
        passwordIcon = (ImageView) view.findViewById(R.id.iv_enter_password);
        left = (ImageView) view.findViewById(R.id.iv_left);
        left.setOnClickListener(this);
        account = new HashMap<>();
        etEmailAdress.setCursorVisible(false);
        tvPassword.setCursorVisible(false);

        etEmailAdress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    etEmailAdress.setCursorVisible(true);// 再次点击显示光标
                }
                return false;
            }
        });
        tvPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    tvPassword.setCursorVisible(true);// 再次点击显示光标
                }
                return false;
            }
        });
    }

    public Map<String,String> getAccount() {
       String email = etEmailAdress.getText().toString();
       String password = tvPassword.getText().toString();
        account.put("email",email);
        account.put("password",password);
        return account;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                signUpActivity.onBackPressed();
                break;
        }
    }

    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }
}
