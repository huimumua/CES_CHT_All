package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceSceneManager;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class RemoveDeviceActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = RemoveDeviceActivity.class.getSimpleName();
    private ImageView ivBack;
    private Button btnCancel;
    private TextView tvStatus;
    private ProgressBar proBar;
    private Timer timer;
    private ZwaveDeviceManager zwDevManager;
    private ZwaveDeviceSceneManager zwDevSceneManager;
    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_device);

        zwDevManager = ZwaveDeviceManager.getInstance(this);
        zwDevSceneManager = ZwaveDeviceSceneManager.getInstance(this);

        initView();

        proBar.setIndeterminate(true);

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        timer = new Timer(true);
        timer.schedule(new RemoveDeviceActivity.mTimerTask(), 1000 * 60); //延时1000ms后执行，1000ms执行一次

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zwaveService.unregister(mCallback);

        try {
            this.unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    private Handler mHandler = new Handler() {
        // 重写handleMessage()方法，此方法在UI线程运行
        @Override
        public void handleMessage(Message msg) {
        switch (msg.what) {
            case 2001:
                hideProgressDialog();
                timerCancel();
                showFailedDialog("Remove Device Timeout");
                break;
        }
        }
    };

    public void showFailedDialog(final String titleStr) {
        final android.support.v7.app.AlertDialog.Builder removeDialog = new android.support.v7.app.AlertDialog.Builder(mContext);
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        removeDialog.setView(view);
        final android.support.v7.app.AlertDialog alertDialog = removeDialog.create();

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        message.setText(titleStr);
        Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);
        positiveButton.setText("retry");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //点击重试，返回添加设备界面，再次执行添加设备
            zwaveService.removeDevice(Const.zwaveType,1);
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.img_back);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        proBar = (ProgressBar) findViewById(R.id.proBar);
        ivBack.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(LOG_TAG,"onServiceConnected....");
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);

                removeDevice("",1);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    class mTimerTask extends TimerTask {
        public void run() {
            Log.d(LOG_TAG,"timer on schedule");
            Message message = new Message();
            message.what = 2001;
            mHandler.sendMessage(message);
            timerCancel();
        }
    }

    private void timerCancel() {
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    private void removeDevice(final String nodeId,final int type) {
        //删除成功则返回主页，否则提示删除失败，返回设备管理界面
        //点击取消，返回设备管理界面

        if(type == 0){
            Log.i(LOG_TAG,"replaceFailedDevice:" + nodeId);
            if (zwaveService != null) {
                zwaveService.removeFailedDevice(Integer.parseInt(nodeId));
            }

        }else if(type==1){
            Log.i(LOG_TAG, "removeDevice:");
            if (zwaveService != null) {
                zwaveService.removeDevice(Const.zwaveType,1);
            }else{
                Log.i(LOG_TAG, "zwaveService is Null");
            }
        }else if(type==2){
            if (zwaveService != null) {
                Log.i(LOG_TAG, "replaceFailedDevice:" + nodeId);
                zwaveService.replaceFailedDevice(Integer.parseInt(nodeId));
            }
        }
    }

    private void removeDeviceResult(String result) {
        Log.i(LOG_TAG,"removeDeviceResult,,,,,,,");
        if(result.contains("removeDevice:other")){
            return;
        }
        try {
            final JSONObject jsonObject = new JSONObject(result);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                String messageType = jsonObject.optString("MessageType");

                Log.i(LOG_TAG,"=====messageType=="+messageType);
                if(messageType.equals("Node Remove Status")) {

                    String status = jsonObject.optString("Status");

                    tvStatus.setText(status);
                    if ("Success".equals(status)) {

                        tvStatus.setText("Success");
                        proBar.setIndeterminate(false);
                        timerCancel();

                    } else if ("Failed".equals(status)) {
                        Toast.makeText(mContext, "Delete Faild", Toast.LENGTH_SHORT).show();
                        proBar.setIndeterminate(false);
                        timerCancel();
                        backToHomeActivity();

                    } else if ("Learn Ready".equals(status)) {
                        tvStatus.setText("Please press the trigger button of the device");

                    } else {
                        Log.i(LOG_TAG, "=====result==" + status);
                        tvStatus.setText(status);
                    }
                }
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                zwaveService.stopRemoveDevice(Const.zwaveType);
                timerCancel();
                finish();
                break;
            case R.id.btn_cancel:
                Log.i(LOG_TAG,"TAP CANCEL BUTTON");
                zwaveService.stopRemoveDevice(Const.zwaveType);
                timerCancel();
                backToHomeActivity();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

        if (className.equals("removeDevice")){
            if (result.contains("removeDevice:")){
                String[] tokens = result.split(":");
                final int nodeId = Integer.parseInt(tokens[2]);

                if (tokens.length<3){
                    Log.i(LOG_TAG,"removeDevice : wrong format "+result);
                } else {
                    Log.i(LOG_TAG,"remove nodeid = "+nodeId+" and backtoHome");
                    /*
                    Log.i(LOG_TAG,"call removeScene");
                    new Thread(new Runnable() {
                        public void run() {
                        List<String> tmpNodeList = zwDevManager.getSceneNameList();
                        if (tmpNodeList!=null) {

                            for (int idx = 0; idx < tmpNodeList.size(); idx++) {

                                ZwaveDeviceScene removeScene = new ZwaveDeviceScene();

                                ZwaveDeviceScene tmpScene = zwDevSceneManager.getScene(tmpNodeList.get(idx));
                                if (tmpScene != null && tmpScene.getSceneId() == nodeId) {
                                    removeScene.setSensorNodeId(null);
                                    removeScene.setCondition(null);
                                    zwDevSceneManager.updateScene(removeScene,tmpNodeList.get(idx));
                                }
                            }
                        }
                        }
                    }).start();
                    */

                    Log.i(LOG_TAG,"endof call removeScene");

                    // back to home activity
                    Intent intent = new Intent(mContext,HomeActivity.class);
                    startActivity(intent);
                    finish();

                }
            }else {
                removeDeviceResult(result);
            }
        }
        }
    };

}
