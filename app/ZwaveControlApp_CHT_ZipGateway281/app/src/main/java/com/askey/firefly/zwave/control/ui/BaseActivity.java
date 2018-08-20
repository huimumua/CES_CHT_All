package com.askey.firefly.zwave.control.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;

import java.util.Timer;

/**
 * Created by chiapin on 2017/9/22.
 */

public class BaseActivity extends AppCompatActivity{

    private static String LOG_TAG = BaseActivity.class.getSimpleName();
    protected static Context mContext;
    protected static ProgressDialog progressDialog = null;

    private Timer timer;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void showProgressDialog(Context context, String text) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(context, "Wait a moment...", text, true);
            progressDialog.setContentView(R.layout.custom_progress);

            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int arg1,
                                     KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_BACK
                        && arg2.getRepeatCount() == 0
                        && arg2.getAction() == KeyEvent.ACTION_UP) {
                    new AlertDialog.Builder(BaseActivity.this)
                        .setTitle("Warning")
                        .setMessage("The processing has not been completed yet")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick( DialogInterface dialog,int whichButton) {
                                hideProgressDialog();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton) {
                                return;
                            }
                        }).show();
                    }
                    return true;
                }

            });
            View v = progressDialog.getWindow().getDecorView();
            if (text == null) {
                text = "Wait a moment...";
            }
            setProgressText(v, text);
        }
    }

    public void showNodeProgressDialog(Context context, String text) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(context, "Wait a moment...", text, true);
            progressDialog.setContentView(R.layout.node_progress);

            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int arg1,
                                     KeyEvent arg2) {
                    if (arg1 == KeyEvent.KEYCODE_BACK
                            && arg2.getRepeatCount() == 0
                            && arg2.getAction() == KeyEvent.ACTION_UP) {
                        new AlertDialog.Builder(BaseActivity.this)
                                .setTitle("Warning")
                                .setMessage("The processing has not been completed yet")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick( DialogInterface dialog,int whichButton) {
                                        hideProgressDialog();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int whichButton) {
                                        return;
                                    }
                                }).show();
                    }
                    return true;
                }

            });
            View v = progressDialog.getWindow().getDecorView();
            if (text == null) {
                text = "Wait a moment...";
            }
            setProgressText(v, text);
        }
    }

    public void setTopLayout(boolean isVis, String str) {
        if((findViewById(R.id.img_back))!=null && (findViewById(R.id.tv_title))!=null){
            //Back button =(ImageView)findViewById(R.id.img_back);
            ImageView back = (ImageView)findViewById(R.id.img_back);
            if(isVis){
                back.setVisibility(View.VISIBLE);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        backToHomeActivity();
                    }
                });
            }else{
                back.setVisibility(View.INVISIBLE);
            }
            //Title
            TextView text = (TextView)findViewById(R.id.tv_title);
            if(!"".equals(str) && str!=null){
                text.setText(str);
            }
        }
    }

    private void setProgressText(View v, String text) {

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width2 = outMetrics.widthPixels;
        int height2 = outMetrics.heightPixels;

        if (v instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) v;
            int count = parent.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = parent.getChildAt(i);
                setProgressText(child, text);
            }
        } else if (v instanceof TextView) {
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.width = width2;
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                Log.i(LOG_TAG,"LANDSCAPE MODE");
                params.width = width2/2;
            }
            v.setLayoutParams(params);

            ((TextView) v).setWidth(width2-60);
            ((TextView) v).setTextSize(18);
            ((TextView) v).setText(text);
            ((TextView) v).setTextColor(Color.BLACK);
        }
    }

    protected static  void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    protected void backToHomeActivity(){
        Intent intent = new Intent();
        intent.setClass(mContext, HomeActivity.class);
        mContext.startActivity(intent);
        finish();
    }

}