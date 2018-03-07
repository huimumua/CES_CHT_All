package com.askey.mobile.zwave.control.data;

import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/11/29 8:59
 * 修改人：skysoft
 * 修改时间：2017/11/29 8:59
 * 修改备注：
 */
public class LocalJsonParsing {
    public static String LOG_TAG = "LocalJsonParsing";

    public static ArrayList getDeviceList(String mqttResult) {
        ArrayList deviceInfoList = new ArrayList<>();
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String DeviceList = reportedObject.optString("deviceList");
                JSONArray columnInfo = new JSONArray(DeviceList);
                int size = columnInfo.length();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        JSONObject info = columnInfo.getJSONObject(i);
                        String nodeId = info.getString("nodeId");
                        String brand = info.getString("brand");
                        String devType = info.getString("deviceType");
                        String category = info.getString("category");
                        String Room = info.getString("Room");
                        String isFavorite = info.getString("isFavorite");
                        String name = info.getString("name");
                        Logg.i(LOG_TAG, "==getDeviceResult=JSONArray===devName==" + name);
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDeviceId(nodeId);
                        deviceInfo.setDisplayName(name);
                        deviceInfo.setDeviceType(category);
                        deviceInfo.setRooms(Room);
                        deviceInfo.setIsFavorite(isFavorite);

                        deviceInfoList.add(deviceInfo);

                        String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
                        // 订阅新设备的topic为 sn + nodeId
                        MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return deviceInfoList;
    }


}
