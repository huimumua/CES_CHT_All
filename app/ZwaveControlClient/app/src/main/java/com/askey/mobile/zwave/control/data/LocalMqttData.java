package com.askey.mobile.zwave.control.data;

import android.util.Log;

import com.askey.mobile.zwave.control.deviceContr.rooms.ui.SetupKeyActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/11/16 14:27
 * 修改人：skysoft
 * 修改时间：2017/11/16 14:27
 * 修改备注：
 */
public class LocalMqttData {

//    {
//        "desired": {
//        "function": "addDevice"
//        "parameter":{
//            "deviceType": "Zwave"  //BT,Zwave,NBIoT
//            "deviceId": "1234567"  //for BT
//            "Room": "BedRoom",
//                    "isFavorite": "true",    //true,false,
//                    "category": "bulb",     //bulb/plug/sensor/switch/dimmer
//                    "name": "bulb_name"
//        },
//    }
    public static String addDevice(String deviceId,String Room,String isFavorite,String category,String name){
        //"state":{"desired":{"data":
//        JSONObject state = new JSONObject();
//        JSONObject desired = new JSONObject();
//        JSONObject data = new JSONObject();
        String result = "";
        JSONObject id = new JSONObject();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("deviceType","Zwave");
            parameter.put("deviceId",deviceId);
            parameter.put("Room",Room);
            parameter.put("isFavorite",isFavorite);
            parameter.put("category",category);
            parameter.put("name",name);
            id.put("function","addDevice");
            id.put("parameter",parameter);
//            data.put("data",id);
//            desired.put("desired",data);
//            state.put("state",desired);
            result = getPublicJson(id).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",result);
        return result;
    }

    public static JSONObject getPublicJson( JSONObject id ){
        JSONObject state = new JSONObject();
        JSONObject desired = new JSONObject();
        JSONObject data = new JSONObject();
        try {

            data.put("data",id);
            desired.put("desired",data);
            state.put("state",desired);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",state.toString());
        return state;
    }


//    {
//        "desired": {
//        "function": "removeDevice"
//        "deviceId": "1234567"
//    },
//    }
    public static String removeDevice(String deviceId){
        String result ="";
        JSONObject id = new JSONObject();
        try {
            id.put("function","removeDevice");
            id.put("deviceId",deviceId);
            result = getPublicJson(id).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",result);
        return result;
    }


//    {
//        "desired": {
//        "function": "removeDeviceFromRoom"
//        "deviceId": "1234567"
//        "room": "bedroom"
//    },
//    }

    public static String removeDeviceFromRoom( String deviceId){
    String result = "";
    JSONObject id = new JSONObject();
    try {
        id.put("function","removeDeviceFromRoom");
        id.put("deviceId",deviceId);
        result = getPublicJson(id).toString();
    } catch (JSONException e) {
        e.printStackTrace();
    }
    Log.d("json",result);
    return result;
}


//    {
//        "desired": {
//        "function": "getDeviceList"
//        "Room": "BedRoom",    //RoomName or ALL
//    },
//    }
    public static String getDeviceListCommand(String roomName){
//        JSONObject command = new JSONObject();
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getDeviceList");
            function.put("Room", roomName);
//            function.put("Room", "ALL");
            result = getPublicJson(function).toString();
//            command.put("desired", function);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "editNodeInfo"
//        "deviceId": "1234567"
//        "parameter":{
//            "Room": "BedRoom",
//                    "isFavorite": "true",    //true,false,
//                    "name": "bulb_name"
//        },
//    },
//    }

    public static String editNodeInfo(String deviceId,String Room,String isFavorite,String name,String deviceType){
//        JSONObject desired = new JSONObject();
        String result = "";
        JSONObject id = new JSONObject();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("Room",Room);
            parameter.put("isFavorite",isFavorite);
            parameter.put("name",name);
            parameter.put("type",deviceType);
            id.put("function","editNodeInfo");
            id.put("parameter",parameter);
            id.put("deviceId",deviceId);
//            desired.put("desired",id);
            result = getPublicJson(id).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",result);
        return result;
    }




//    {
//        "desired": {
//        "function": "getRecentDeviceList",
//                "number": "3",
//    },
//    }
    public static String getRecentDeviceList() {
        String result ="";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getRecentDeviceList");
            function.put("number", "3");
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "getRooms"
//    },
//    }

    public static String getRooms() {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getRooms");
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "removeRoom"
//        "removeRoom": "BedRoom",
//         toRoom": "living Room",
//    },
//    }

    public static String removeRoom(String removeRoom, String toRoom) {
       String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeRoom");
            function.put("removeRoom", removeRoom);
            function.put("toRoom", toRoom);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "getSwitchStatus"
//        "deviceId": "1234567"
//    },
//    }

    public static String getSwitchStatus(String deviceId) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getSwitchStatus");
            function.put("deviceId", deviceId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": " setSwitch "
//        "switchStatus": "on"    //on,off
//        "deviceId": "1234567"
//    },
//    }

    public static String setSwitch(String deviceId ,String switchState) {
        String result ="";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setSwitchStatus");
            function.put("deviceId", deviceId);
            function.put("switchStatus", switchState);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": " getBrigtness "
//        "deviceId": "1234567"
//    },
//    }

    public static String getBrigtness(String deviceId ) {
         String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getBrigtness");
            function.put("deviceId", deviceId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


//    {
//        "desired": {
//        "function": " setBrigtness "
//        " value ": "50"  // 1~100
//        "deviceId": "1234567"
//    },
//    }

    public static String setBrigtness(String deviceId ,String value) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setBrigtness");
            function.put("deviceId", deviceId);
            function.put("value", value);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": " getLampColor "
//        "deviceId": "1234567"
//    },
//    }

    public static String getLampColor(String deviceId) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getLampColor");
            function.put("deviceId", deviceId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


//    "function": " setLampColor "
//            "lampcolor": "RGB",    // warmWhite/coldWhite/RGB
//            "deviceId": "1234567"
//            "R":"255",
//            "G":"255",
//            "B":"255",
    public static String setLampcolor(String deviceId, String lampcolor, String colorId,String colorValue,String R,String G,String B) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setLampColor");
            function.put("deviceId", deviceId);
            function.put("lampcolor", lampcolor);
//            function.put("colorId", colorId);
//            function.put("colorValue", colorValue);
            function.put("R", R);
            function.put("G", G);
            function.put("B", B);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


//    {
//        "desired": {
//        "function": "getConfigure"
//        "nodeId": "1234567"
//        "parameter": "7"
//    },
//    }

    public static String getConfigure(String deviceId,String lampcolor) {
        String result ="";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setLampcolor");
            function.put("deviceId", deviceId);
            function.put("lampcolor", lampcolor);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


//    {
//        "desired": {
//        "function": "getPower"
//        "deviceId": "1234567"
//    }

    public static String getPower(String deviceId ) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getPower");
            function.put("deviceId", deviceId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }





}
