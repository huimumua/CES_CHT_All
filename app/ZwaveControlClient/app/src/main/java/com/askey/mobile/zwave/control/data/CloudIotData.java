package com.askey.mobile.zwave.control.data;

import android.util.Log;

import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/11/22 13:47
 * 修改人：skysoft
 * 修改时间：2017/11/22 13:47
 * 修改备注：
 */
public class CloudIotData {

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
            ;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",id.toString());
        return  id.toString();
    }



    //    {
//        "desired": {
//        "function": "removeDevice"
//        "deviceId": "1234567"
//    },
//    }
    public static String removeDevice(String deviceId){
        JSONObject id = new JSONObject();
        try {
            id.put("function","removeDevice");
            id.put("deviceId",deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",id.toString());
        return id.toString();
    }


//    {
//        "desired": {
//        "function": "removeDeviceFromRoom"
//        "deviceId": "1234567"
//        "room": "bedroom"
//    },
//    }

    public static String removeDeviceFromRoom( String deviceId){
        JSONObject id = new JSONObject();
        try {
            id.put("function","removeDeviceFromRoom");
            id.put("deviceId",deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",id.toString());
        return id.toString();
    }


    //    {
//        "desired": {
//        "function": "getDeviceList"
//        "Room": "BedRoom",    //RoomName or ALL
//    },
//    }
    public static String getDeviceListCommand(String roomName){
//        JSONObject command = new JSONObject();
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getDeviceList");
            function.put("Room", roomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
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
        JSONObject id = new JSONObject();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("Room",Room);
            parameter.put("isFavorite",isFavorite);
            parameter.put("name",name);
            parameter.put("type",deviceType);
            id.put("function","editNodeInfo");
            id.put("parameter",parameter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json",id.toString());
        return id.toString();
    }

    public static String editNodeInfo(List<String> add, List<String> remove){
        JSONObject parameter = new JSONObject();
        JSONObject obj = null;
        JSONArray addList = new JSONArray();
        JSONArray removeList = new JSONArray();
        try {
            parameter.put("function", "editFavoriteList");
            if (add.size() > 0) {
                for (int i = 0; i < add.size(); i++) {
                    obj = new JSONObject();
                    obj.put("nodeId", add.get(i));
                    addList.put(obj);
                    obj = null;
                }
            }
            parameter.put("addFavorite", addList.toString());

            if (remove.size() > 0) {
                for (int i = 0; i < remove.size(); i++) {
                    obj = new JSONObject();
                    obj.put("nodeId", remove.get(i));
                    removeList.put(obj);
                    obj = null;
                }

            }
            parameter.put("removeFavorite", removeList.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json", parameter.toString());
        return parameter.toString();
    }



    //    {
//        "desired": {
//        "function": "getRecentDeviceList",
//                "number": "3",
//    },
//    }
    public static String getRecentDeviceList() {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getRecentDeviceList");
            function.put("number", "3");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "getRooms"
//    },
//    }

    public static String getRooms() {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getRooms");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "removeRoom"
//        "removeRoom": "BedRoom",
//         toRoom": "living Room",
//    },
//    }

    public static String removeRoom(String removeRoom, String toRoom) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeRoom");
            function.put("removeRoom", removeRoom);
            function.put("toRoom", toRoom);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "getSwitchStatus"
//        "deviceId": "1234567"
//    },
//    }

    public static String getSwitchStatus(String deviceId) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getSwitchStatus");
            function.put("deviceId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": " setSwitch "
//        "switchStatus": "on"    //on,off
//        "deviceId": "1234567"
//    },
//    }

    public static String setSwitch(String deviceId ,String switchState) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setSwitchStatus");
            function.put("deviceId", deviceId);
            function.put("switchStatus", switchState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": " getBrigtness "
//        "deviceId": "1234567"
//    },
//    }

    public static String getBrigtness(String deviceId ) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getBrigtness");
            function.put("deviceId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }


//    {
//        "desired": {
//        "function": " setBrigtness "
//        " value ": "50"  // 1~100
//        "deviceId": "1234567"
//    },
//    }

    public static String setBrigtness(String deviceId ,String value) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setBrigtness");
            function.put("deviceId", deviceId);
            function.put("value", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": " getLampColor "
//        "deviceId": "1234567"
//    },
//    }

    public static String getLampColor(String deviceId) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getLampColor");
            function.put("deviceId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }


    //    "function": " setLampColor "
//            "lampcolor": "RGB",    // warmWhite/coldWhite/RGB
//            "deviceId": "1234567"
//            "R":"255",
//            "G":"255",
//            "B":"255",
    public static String setLampcolor(String deviceId, String lampcolor, String colorId,String colorValue,String R,String G,String B) {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }


//    {
//        "desired": {
//        "function": "getConfigure"
//        "nodeId": "1234567"
//        "parameter": "7"
//    },
//    }

    public static String getConfigure(String deviceId,String lampcolor) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setLampcolor");
            function.put("deviceId", deviceId);
            function.put("lampcolor", lampcolor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }


//    {
//        "desired": {
//        "function": "getPower"
//        "deviceId": "1234567"
//    }

    public static String getPower(String deviceId ) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getPower");
            function.put("deviceId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }


    //    {
//        "desired": {
//        "function": "getGroupInfo",
//                "nodeId": "1234567",
//                "endpointId": "7"
//        "groupId": "0"
//    },
//    }

    public static String getGroupInfo(String deviceId,String endpointId , String groupId) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getGroupInfo");
            function.put("nodeId", deviceId);
            function.put("endpointId", endpointId);
            function.put("maxGroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "addEndpointsToGroup"
//        "nodeId": "8"
//        "groupId": "3"
//“arr”: [
//        {"controlNodeId": "3"},
//        {"controlNodeId": "5"}]
//        "endpointId": "0"
//    },
//    }

    public static String addEndpointsToGroup(String deviceId,String endpointId ,String groupId, ArrayList nodeInterFaceList ) {
        JSONObject function = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject tmpObj = null;
            int count = nodeInterFaceList.size();
            for(int i = 0; i < count; i++) {
                tmpObj = new JSONObject();
                tmpObj.put("controlNodeId" , nodeInterFaceList.get(i));
                jsonArray.put(tmpObj);
                tmpObj = null;
            }
            String personInfos = jsonArray.toString(); // 将JSONArray转换得到String
            function.put("function", "addEndpointsToGroup");
            function.put("nodeId", deviceId);
            function.put("endpointId", endpointId);
            function.put("groupId", groupId);
            function.put("arr" , personInfos);   // 获得JSONObject的String
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "removeEndpointsFromGroup"
//        "nodeId": "8"
//“arr”: [
//        "nodeInterface": "23"
//        "nodeInterface": "39"}]
//        "endpointId": "55"
//    },
//}

    public static String removeEndpointsFromGroup(String deviceId,String endpointId ,String groupId , ArrayList nodeInterFaceList ) {
        JSONObject function = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject tmpObj = null;
            int count = nodeInterFaceList.size();
            for(int i = 0; i < count; i++) {
                tmpObj = new JSONObject();
                tmpObj.put("controlNodeId" , nodeInterFaceList.get(i));
                jsonArray.put(tmpObj);
                tmpObj = null;
            }
            String personInfos = jsonArray.toString(); // 将JSONArray转换得到String
            function.put("function", "removeEndpointsFromGroup");
            function.put("nodeId", deviceId);
            function.put("endpointId", endpointId);
            function.put("groupId", groupId);
            function.put("arr" , personInfos);   // 获得JSONObject的String
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "getMaxSupperedGroups"
//        "nodeId": "8"
//        "endpointId": "55"
//    },
//    }

    public static String getMaxSupperedGroups(String deviceId,String endpointId) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getMaxSupperedGroups");
            function.put("nodeId", deviceId);
            function.put("endpointId", endpointId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "setScheduleActive",
//                "deviceType": "Zwave",
//                "nodeId": "23",
//                "active": “true",    //true or false
//    }

    public static String setScheduleActive(String deviceId,boolean active) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setScheduleActive");
            function.put("nodeId", deviceId);
            function.put("deviceType", "Zwave");
            function.put("active", active);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "getScheduleActive",
//                "nodeId": "23",
//    },
//    }


    public static String getScheduleActive(String deviceId ) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getScheduleActive");
            function.put("nodeId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "getScheduleList",
//                "nodeId": "23",
//    },
//    }

    public static String getScheduleList(String deviceId ) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getScheduleList");
            function.put("nodeId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "setSchedule",
//                "nodeId": "23",
//                "variableValue": "99",
//                "dayOfWeek": "Mon",
//                "StartTime": "01:00",
//                "EndTime": "19:00",
//
//    }
//    }

    public static String setSchedule(String deviceId ,String variableValue,String dayOfWeek,String StartTime,String EndTime) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setSchedule");
            function.put("nodeId", deviceId);
            function.put("variableValue", variableValue);
            function.put("dayOfWeek", dayOfWeek);
            function.put("StartTime", StartTime);
            function.put("EndTime", EndTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "removeSchedule",
//                "nodeId": "23",
//                "dayOfWeek": "Mon",
//    }
//    }

    public static String removeSchedule(String deviceId ,String dayOfWeek) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeSchedule");
            function.put("nodeId", deviceId);
            function.put("dayOfWeek", dayOfWeek);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }


    public static String getFavoriteList() {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getFavoriteList");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

    //    {
//        "desired": {
//        "function": "getScene",
//    }
    public static String getScene() {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getSceneList");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

    //    {
//        "desired": {
//        "function": "setScene",
//                "sceneName": "test12345",
//                "iconName": "test12345",
//                "condition":[{
//            "nodeId": "13",
//                    "category": "bulb",
//                    "targetSatus": 255",  // 255 = on, 0 = off
//            "currentStatus": "0",
//                    "targetColor": "RGB”,  // warmWhite/coldWhite/RGB
//            "currentColor": "warmWhite”,
//            "timer": "12:30:22” //HH:MM:SS
//        },{
//            "nodeId": "14",
//                    "category": "plug",
//                    "targetSatus": 255",  // 255 = on, 0 = off
//            "currentStatus": "0",
//                    "timer": "12:30:22” //HH:MM:SS
//        },
//    }
    public static String setSceneAction(String sceneName,String iconName , ArrayList<ScenesInfo> sceneInfoList ) {
        JSONObject function = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject tmpObj = null;
            int count = sceneInfoList.size();
            for(int i = 0; i < count; i++) {
                tmpObj = new JSONObject();
                ScenesInfo scenesInfo = sceneInfoList.get(i);
//                tmpObj.put("scenesId" ,scenesInfo.getScenesId());
//                tmpObj.put("scenesName" ,scenesInfo.getScenesName());
                tmpObj.put("nodeId" ,scenesInfo.getNodeId() );
                tmpObj.put("category" ,scenesInfo.getCategory() );
                tmpObj.put("targetSatus" ,scenesInfo.getTargetSatus() );
                tmpObj.put("currentStatus" ,scenesInfo.getCurrentStatus() );
                tmpObj.put("targetColor" ,scenesInfo.getTargetColor());
                tmpObj.put("currentColor" ,scenesInfo.getCurrentColor() );
                tmpObj.put("timer" ,scenesInfo.getTimer());
                jsonArray.put(tmpObj);
                tmpObj = null;
            }
            String conditions = jsonArray.toString(); // 将JSONArray转换得到String
            function.put("function", "setSceneAction");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
            function.put("condition", conditions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

    public static String removeSceneAction(String sceneName,String iconName , ArrayList<ScenesInfo> sceneInfoList ) {
        JSONObject function = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject tmpObj = null;
            int count = sceneInfoList.size();
            for(int i = 0; i < count; i++) {
                tmpObj = new JSONObject();
                ScenesInfo scenesInfo = sceneInfoList.get(i);
//                tmpObj.put("scenesId" ,scenesInfo.getScenesId());
//                tmpObj.put("scenesName" ,scenesInfo.getScenesName());
                tmpObj.put("nodeId" ,scenesInfo.getNodeId() );
                tmpObj.put("category" ,scenesInfo.getCategory() );
                tmpObj.put("targetSatus" ,scenesInfo.getTargetSatus() );
                tmpObj.put("currentStatus" ,scenesInfo.getCurrentStatus() );
                tmpObj.put("targetColor" ,scenesInfo.getTargetColor());
                tmpObj.put("currentColor" ,scenesInfo.getCurrentColor() );
                tmpObj.put("timer" ,scenesInfo.getTimer());
                jsonArray.put(tmpObj);
                tmpObj = null;
            }
            String conditions = jsonArray.toString(); // 将JSONArray转换得到String
            function.put("function", "removeSceneAction");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
            function.put("condition", conditions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

    //    {
//        "desired": {
//        "function": "removeSceneAction",
//                "sceneName": "test12345",
//                "iconName": "test12345",
//    }
    public static String removeSceneAction(String sceneName,String iconName) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeSceneAction");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }


//    {
//        "desired": {
//        "function": "removeScene",
//                "sceneName": "test12345",
//                "iconName": "test12345",
//    }

    public static String removeScene(String sceneName,String iconName) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeScene");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

//    {
//        "desired": {
//        "function": "editScene",
//                "sceneName": "test12345",
//                "iconName": "test12345",
//                "newName": "aaa",
//                "newName": "aaaa",
//
//    }

    public static String editScene(String sceneName,String iconName,String newName,String isFavorite) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeScene");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
            function.put("newName", newName);
            function.put("isFavorite", isFavorite);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }

    //    {
//        "desired": {
//        "function": "executeScene",
//                "action": "run",   // run or stop
//                "sceneName": "test12345",
//                "iconName": "test12345",
//
//    }
    public static String executeScene(String sceneName,String iconName,String action) {
        JSONObject function = new JSONObject();
        try {
            function.put("function", "executeScene");
            function.put("sceneName", sceneName);
            function.put("action", action);//run or stop
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return function.toString();
    }




}
