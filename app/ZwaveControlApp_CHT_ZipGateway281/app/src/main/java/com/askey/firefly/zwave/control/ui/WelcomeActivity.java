package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDevice;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.service.MQTTBroker;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.thirdparty.usbserial.UsbSerial;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by chiapin on 2017/9/7.
 */

public class WelcomeActivity extends BaseActivity {
    MyTask mTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //process();

        mTask = new MyTask();
        mTask.execute();
    }

    class MyTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            showProgressDialog(mContext, "Initializing�AOpen Zwave Controller...");
        }


        // ��k2�GdoInBackground�]�^
        // �@�ΡG����?�J??�B?���?������?�ާ@�B��^ ?�{��??�檺?�G
        // ��?�q??��?�Ӽ�?���[??�ס�����?
        @Override
        protected String doInBackground(String... params) {

            Intent mqttService = new Intent(WelcomeActivity.this, MQTTBroker.class);
            startService(mqttService);

            Intent zwaveService = new Intent(WelcomeActivity.this, ZwaveControlService.class);
            startService(zwaveService);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        // ��k4�GonPostExecute�]�^
        // �@�ΡG����?�{��??��?�G�B??��?�G?�ܨ�UI?��
        @Override
        protected void onPostExecute(String result) {
            // ?�槹?�Z�A?��sUI
            //showInitZaveDialog( "�[?����");
            mHandler.sendEmptyMessage(0);

        }
    }

    private void process() {

        showProgressDialog(mContext, "Initializing�AOpen Zwave Controller...");
        new Thread(){
            @Override
            public void run(){

                Intent mqttService = new Intent(WelcomeActivity.this, MQTTBroker.class);
                startService(mqttService);

                Intent zwaveService = new Intent(WelcomeActivity.this, ZwaveControlService.class);
                startService(zwaveService);

                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Intent intent = new Intent();
                    intent.setClass(mContext, HomeActivity.class);
                    hideProgressDialog();
                    mContext.startActivity(intent);
                    //finish();
                    break;
            }
        }
    };
}
