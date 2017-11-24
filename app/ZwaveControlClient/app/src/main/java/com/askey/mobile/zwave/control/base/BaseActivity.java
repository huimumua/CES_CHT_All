package com.askey.mobile.zwave.control.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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



}
