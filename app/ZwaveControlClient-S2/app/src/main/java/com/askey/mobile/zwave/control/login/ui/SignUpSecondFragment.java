package com.askey.mobile.zwave.control.login.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import java.util.Map;

/**
 * Created by skysoft on 2017/10/10.
 */

public class SignUpSecondFragment extends Fragment implements View.OnClickListener{
    private FragmentManager fm;
    private Map<String,String> account;
    private EditText nickname;
    private Context mContext;
    private ImageView nicknameIcon,left;
    private SignUpActivity signUpActivity;
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up_second, null);

        initView();

        FragmentPage.getInstance().setPageCallback(new FragmentCallback() {
            @Override
            public void handlePage() {

                if((signUpActivity.isNextPage)) {
                    signUpActivity.goNextPage(null);
                }

            }
        });

        nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    nicknameIcon.setVisibility(View.VISIBLE);
                    nickname.setCursorVisible(true);
                } else {
                    nicknameIcon.setVisibility(View.INVISIBLE);
                    nickname.setCursorVisible(false);
                }

                if (nicknameIcon.getVisibility() == View.VISIBLE) {
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
        mContext = getActivity();
        fm  = getActivity().getSupportFragmentManager();
        String tag = 0 + "";
        SignUpOneFragment one = (SignUpOneFragment) fm.findFragmentByTag(tag);
        account = one.getAccount();
        nickname = (EditText) view.findViewById(R.id.et_nickname);
        nicknameIcon = (ImageView) view.findViewById(R.id.iv_nickname);
        left = (ImageView) view.findViewById(R.id.iv_left);
        left.setOnClickListener(this);
        signUpActivity =  ((SignUpActivity) getActivity());
        signUpActivity.right.setImageResource(R.drawable.vector_drawable_ic_66);

        nickname.setCursorVisible(false);
        nickname.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    nickname.setCursorVisible(true);
                }
                return false;
            }
        });
    }

    public Map<String,String> getAccount() {
        String nicknameText = nickname.getText().toString();
        account.put("nickname", nicknameText);
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
}
