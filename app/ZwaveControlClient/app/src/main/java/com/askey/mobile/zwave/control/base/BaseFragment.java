package com.askey.mobile.zwave.control.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.util.CustomProgressDialog;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/11/27 18:05
 * 修改人：skysoft
 * 修改时间：2017/11/27 18:05
 * 修改备注：
 */
public class BaseFragment extends Fragment {

    private final  String  TAG = "BaseDeviceActivity";
    protected static Context mContext;
    /**
     * 进度条弹出框
     */
    protected static ProgressDialog progressDialog = null;
    /** The handler. */
    public static Handler _handler = new Handler();

    /** Progress dialog **/
    private static Dialog mDialog = null;

    /**
     * Show waiting dialog.
     */
    public static void showWaitingDialog(final String message) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    stopWaitDialog();
                }
                try {
                    //文字即为显示的内容
                    mDialog = CustomProgressDialog.createLoadingDialog(mContext, message);
                    mDialog.setCancelable(true);//允许返回
                    mDialog.show();//显示
                } catch (Exception ex) {
                    ex.getStackTrace();
                }
            }
        });
    }

    public static void showWaitingDialog() {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    stopWaitDialog();
                }
                try {
                    mDialog = CustomProgressDialog.createLoadingDialog(mContext);
                    mDialog.setCancelable(true);//允许返回
                    mDialog.show();//显示
                } catch (Exception ex) {
                    ex.getStackTrace();
                }
            }
        });
    }

    /**
     * Stop waiting dialog.
     */
    public static void stopWaitDialog() {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // activate Quickset services
                    if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    /**
     * 进度条
     * */
    public void showProgressDialog(Context context, String text) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(context, "Wait a moment...", text,
                    true);
            progressDialog.setContentView(R.layout.custom_progress);
            // progressDialog.setCancelable(true);
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int arg1,
                                     KeyEvent arg2) {
                    if (arg1 == KeyEvent.KEYCODE_BACK
                            && arg2.getRepeatCount() == 0
                            && arg2.getAction() == KeyEvent.ACTION_UP) {
                        new AlertDialog.Builder(mContext)
                                .setTitle("Warning")
                                .setMessage("The processing has not been completed yet")
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int whichButton) {
                                                hideProgressDialog();
                                            }
                                        })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int whichButton) {
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

    private void setProgressText(View v, String text) {

        WindowManager manager = getActivity().getWindowManager();
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

}
