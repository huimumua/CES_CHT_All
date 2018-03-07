package com.askey.mobile.zwave.control.home.fragment.dll;

import android.content.Context;
import android.os.AsyncTask;

import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.sdk.device.response.IoTDeviceInfoResponse;
import com.askeycloud.webservice.sdk.service.device.AskeyIoTDeviceService;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import java.util.List;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/12/26 15:21
 * 修改人：skysoft
 * 修改时间：2017/12/26 15:21
 * 修改备注：
 */
public class IotSubscribeTask extends AsyncTask<Void,Integer,Integer> {
    public static String TAG = "IotSubscribeTask";
    private Context context;
    private List<DeviceInfo> mDeviceInfoList;

    public IotSubscribeTask(Context context, List<DeviceInfo> deviceInfoList) {
        this.context = context;
        this.mDeviceInfoList = deviceInfoList;
    }

    /**
     * 运行在UI线程中，在调用doInBackground()之前执行
     */
    @Override
    protected void onPreExecute() {
        Logg.e(TAG, "==onPreExecute==");
    }
    /**
     * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法
     */
    @Override
    protected Integer doInBackground(Void... params) {
        for(DeviceInfo deviceInfo : mDeviceInfoList){
            String nodeId = deviceInfo.getDeviceId();
            if (Const.isRemote) {
                String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
                Context mContext = ZwaveClientApplication.getInstance();
                Logg.i(TAG, "=lookupIoTDevice =nodeTopic==" + nodeTopic);
                IoTDeviceInfoResponse ioTDeviceInfoResponse = AskeyIoTDeviceService.getInstance(mContext).lookupIoTDevice(Const.DEVICE_MODEL, nodeTopic);
                if (ioTDeviceInfoResponse != null) {
                    String shadowTopic = ioTDeviceInfoResponse.getShadowTopic();
                    if (shadowTopic != null && !shadowTopic.equals("")) {
                        AskeyIoTService.getInstance(mContext).subscribeMqtt(shadowTopic);
                        deviceInfo.setTopic(shadowTopic);
                    }
                }
            } else {
                String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
                // 订阅新设备的topic为 sn + nodeId
                MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
            }
        }

        return null;
    }

    /**
     * 运行在ui线程中，在doInBackground()执行完毕后执行
     */
    @Override
    protected void onPostExecute(Integer integer) {
        Logg.e(TAG, "==onPostExecute==");
    }

    /**
     * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        Logg.e(TAG, "==onProgressUpdate==");
    }

}