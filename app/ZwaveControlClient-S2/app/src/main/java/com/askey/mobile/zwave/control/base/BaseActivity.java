package com.askey.mobile.zwave.control.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.login.ui.LoginPageActivity;
import com.askey.mobile.zwave.control.util.CustomProgressDialog;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/10 14:10
 * 修改人：skysoft
 * 修改时间：2017/7/10 14:10
 * 修改备注：
 */
public class BaseActivity extends AppCompatActivity {
    private final  String  TAG = "BaseDeviceActivity";
    protected static Context mContext;
    protected static Context appContext = ZwaveClientApplication.getInstance();
    /**
     * 进度条弹出框
     */
    protected static ProgressDialog progressDialog = null;
    protected PopupWindow mPopupWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mContext = this;

    }


    /**
     * 这是兼容的 AlertDialog
     */
    protected void showDialog(String title, String message, String negative, String positive) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                doNegativeButton();
            }
        });
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                doPositiveButton();
            }
        });
        builder.setCancelable(false);

        android.support.v7.app.AlertDialog dialog = builder.show();
        //“确”定按钮字体颜色
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_e2231a,null));
//”取消“按钮字体颜色
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_e2231a,null));
        } else {
            dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_e2231a));
//”取消“按钮字体颜色
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_e2231a));
        }

    }

    protected void doPositiveButton() {
    }

    protected void doNegativeButton() {
    }


    /**
     * 设置界面顶部导航栏
     * */
    public void setTopLayout(boolean isVis, String str, boolean isVisable) {
        if(((ImageView)findViewById(R.id.img_back))!=null && ((TextView)findViewById(R.id.tv_title))!=null && ((ImageView)findViewById(R.id.img_menu)) !=null){
            //返回按钮
            ImageView back = (ImageView)findViewById(R.id.img_back);
            if(isVis){
                back.setVisibility(View.VISIBLE);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }else{
                back.setVisibility(View.INVISIBLE);
            }
            //标题
            TextView text = (TextView)findViewById(R.id.tv_title);
            if(!"".equals(str) && str!=null){
                text.setText(str);
            }
            //菜单按钮
            ImageView menu = (ImageView)findViewById(R.id.img_menu);
            menu.setVisibility(isVisable ? View.VISIBLE : View.INVISIBLE);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMemu(mContext);
                }
            });
        }
    }

    /**
     * 子类继承实现相应方法
     * */
    public void openMemu(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.layout_popview_item, null, false);
        TextView tvRemove = (TextView) view.findViewById(R.id.tv_one);
        TextView tvReplace = (TextView) view.findViewById(R.id.tv_second);
        TextView tvRemoveFail = (TextView) view.findViewById(R.id.tv_third);
        TextView tvRename = (TextView) view.findViewById(R.id.tv_four);
        TextView tvBattery = (TextView) view.findViewById(R.id.tv_five);

        tvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDevice();
                mPopupWindow.dismiss();
            }
        });
        tvReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击替换异常进入添加页面
                replaceDevice();
                mPopupWindow.dismiss();

            }
        });
        tvRemoveFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击移除异常，进入删除页面
                removeFailDevice();
                mPopupWindow.dismiss();
            }
        });
        tvRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reNameDevice();     //预留的mqtt接口 修改文件名
                mPopupWindow.dismiss();
            }
        });
        tvBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBattery();
                mPopupWindow.dismiss();
                //预留的mqtt接口 获取电池电量
            }
        });

        mPopupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAsDropDown((ImageView)findViewById(R.id.img_menu));
    }

    protected void reNameDevice() {
    }

    protected void removeFailDevice() {
    }

    protected void replaceDevice() {
    }

    protected void removeDevice() {
    }
    protected void getBattery() {
    }

    /** The handler. */
    public static Handler _handler = new Handler();

    /** Progress dialog **/
    private static Dialog mDialog = null;

    /**
     * Show waiting dialog.
     */
    public void showWaitingDialog(final String message) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    stopWaitDialog();
                }
                try {
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
                                new AlertDialog.Builder(BaseActivity.this)
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
                }
            }
        });
    }

    public void showWaitingDialog() {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    stopWaitDialog();
                }
                try {
                    mDialog = CustomProgressDialog.createLoadingDialog(mContext);
                    mDialog.setCancelable(false);//允许返回
                    mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface arg0, int arg1,
                                             KeyEvent arg2) {
                            if (arg1 == KeyEvent.KEYCODE_BACK
                                    && arg2.getRepeatCount() == 0
                                    && arg2.getAction() == KeyEvent.ACTION_UP) {
                                new AlertDialog.Builder(BaseActivity.this)
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
    public void showProgressDialog(Context context,String text) {
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
                        new AlertDialog.Builder(BaseActivity.this)
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

    /*
        这个方法为耗时操作
     */
    private static final String BACK_IMG_SRC = "backgroundImg";
    private String back_img_src;
    protected void guide2HomeActivity(Context mContext) {
        back_img_src = (String) PreferencesUtils.get(mContext, BACK_IMG_SRC, "");
        if (!back_img_src.equals("")) {
            final File file = new File(back_img_src);
            if (file.exists()) {
                Bitmap myBitmap = null;
                try {
                    myBitmap = Glide.with(mContext)
                            .load(file)
                            .asBitmap() //必须
                            .centerCrop()
                            .into(1080, 1920)
                            .get();

                } catch (InterruptedException e) {
                    e.printStackTrace();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                final Bitmap finalMyBitmap = myBitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), finalMyBitmap);
                        ImageUtils.setBackgroundImg(drawable);
                    }
                });
            }
        }
        stopWaitDialog();
//        Intent intent = new Intent(mContext, HomeActivity.class);
//        startActivity(intent);
//        finish();
    }

}