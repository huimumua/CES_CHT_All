package com.askey.mobile.zwave.control.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.home.activity.addDevice.DeleteDeviceActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.RemoveFailActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.ReplaceFailActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.CustomProgressDialog;
import com.askey.mobile.zwave.control.util.Logg;

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
    private static final  String  TAG = "BaseFragment";
    protected static Context mContext;
    /**
     * 进度条弹出框
     */
    protected static ProgressDialog progressDialog = null;
    /** The handler. */
    public static Handler _handler = new Handler();

    /** Progress dialog **/
    private static Dialog mDialog = null;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
                    Thread.sleep(100);
                    Logg.e(TAG,"=====showWaitingDialog=======");
                    //文字即为显示的内容
                    mDialog = CustomProgressDialog.createLoadingDialog(mContext, message);
                    mDialog.setCancelable(false);//允许返回
                    mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

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
                                                        stopWaitDialog();
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
                    mDialog.show();//显示
                } catch (Exception ex) {
                    ex.getStackTrace();
                    Logg.e(TAG,"=====showWaitingDialog===Exception======="+ex.getMessage());
                }
            }
        });
    }

    public static void showWaitingDialog() {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                /*if (mDialog != null) {
                    stopWaitDialog();
                }*/
                try {
                    Thread.sleep(100);
                    Logg.e(TAG,"=====showWaitingDialog=======");
                    if(mDialog == null) {
                        mDialog = CustomProgressDialog.createLoadingDialog(mContext);
                        mDialog.setCancelable(false);//允许返回
                        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

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
                                                            stopWaitDialog();
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
                    }
                    mDialog.show();//显示
                } catch (Exception ex) {
                    ex.getStackTrace();
                    Logg.e(TAG,"=====showWaitingDialog===Exception======="+ex.getMessage());
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
                    Logg.e(TAG,"=====stopWaitDialog=======");
                    // activate Quickset services
                    /*if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                    }*/
                    if(mDialog.isShowing())
                    {
                        mDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logg.e(TAG,"=====stopWaitDialog===Exception======="+e.getMessage());
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

    protected void showDeleteDeviceDialog(final Context context, final String name, final String nodeId) {
        final AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(context);
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_device, null);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
        icon.setImageResource(R.drawable.vector_drawable_ic_92);
        alertDialog.setContentView(view);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button proceed = (Button) view.findViewById(R.id.btn_proceed);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DeleteDeviceActivity.class);
                intent.putExtra("deviceId", nodeId);
                intent.putExtra("roomName", name);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
    }

    protected void showFailDeleteDeviceDialog(final Context context, final String name, final String nodeId) {
        final AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(context);
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(true);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_device, null);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
        icon.setImageResource(R.drawable.vector_drawable_ic_92);
        alertDialog.setContentView(view);
        Button removeFail = (Button) view.findViewById(R.id.btn_cancel);
        Button replaceFail = (Button) view.findViewById(R.id.btn_proceed);
        removeFail.setText("removeFail");
        replaceFail.setText("replaceFail");
        removeFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RemoveFailActivity.class);
                intent.putExtra("nodeId", nodeId);
                intent.putExtra("roomName", name);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        replaceFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReplaceFailActivity.class);
                intent.putExtra("nodeId", nodeId);
                intent.putExtra("roomName", name);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
    }

    public void modifyRoomName(MqttMessageArrived mqttMessageArrived, String oldName, String newName){
//        MQTTManagement.getSingInstance().clearMessageArrived();
        MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
        Log.d("base",oldName+", "+newName);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.editRoom(oldName, newName));
    }

}
