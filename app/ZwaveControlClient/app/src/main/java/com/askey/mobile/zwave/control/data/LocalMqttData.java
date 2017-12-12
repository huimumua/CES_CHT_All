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
//    public static String editNodeInfo(List<String> add, List<String> remove){
//        String result = "";
//        String addStr = "[{}]";
//        String removeStr = "[{}]";
//        JSONObject parameter = new JSONObject();
//        JSONObject obj = null;
//        JSONArray addList = new JSONArray();
//        JSONArray removeList = new JSONArray();
//        try {
//            parameter.put("function", "editFavoriteList");
//            if (add.size() > 0) {
//                for (int i = 0; i < add.size(); i++) {
//                    obj = new JSONObject();
//                    obj.put("nodeId", add.get(i));
//                    addList.put(obj);
//                    obj = null;
//                }
//                addStr = addList.toString();
//            }
//            parameter.put("addFavorite", addStr);
//
//            if (remove.size() > 0) {
//                for (int i = 0; i < remove.size(); i++) {
//                    obj = new JSONObject();
//                    obj.put("nodeId", remove.get(i));
//                    removeList.put(obj);
//                    obj = null;
//                }
//                removeStr = removeList.toString();
//            }
//            parameter.put("removeFavorite", removeStr);
//
//            result = getPublicJson(parameter).toString();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Log.d("json", result);
//        return result;
//    }
    public static String editNodeInfo(List<String> add, List<String> remove){
        String result = "";
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

            result = getPublicJson(parameter).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("json", result);
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

//    {
//        "desired": {
//        "function": "getGroupInfo",
//                "nodeId": "1234567",
//                "endpointId": "7"
//        "groupId": "0"
//    },
//    }

    public static String getGroupInfo(String deviceId,String endpointId , String groupId) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getGroupInfo");
            function.put("nodeId", deviceId);
            function.put("endpointId", endpointId);
            function.put("maxGroupId", groupId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "addEndpointsToGroup"
//        "nodeId": "8"
//       “arr”: [{
//            "nodeInterFace": "23"},
//        {"nodeInterFace": "39"}]
//        "endpointId": "55"
//    },
//    }
public static String addEndpointsToGroup(String deviceId,String endpointId ,String groupId , ArrayList nodeInterFaceList ) {
    String result = "";
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
        result = getPublicJson(function).toString();
    } catch (JSONException e) {
        e.printStackTrace();
    }
    return result;
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

    public static String removeEndpointsFromGroup(String deviceId,String endpointId ,String groupId, ArrayList nodeInterFaceList ) {
        String result = "";
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
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "getMaxSupperedGroups"
//        "nodeId": "8"
//        "endpointId": "55"
//    },
//    }
    public static String getMaxSupperedGroups(String deviceId,String endpointId) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getMaxSupperedGroups");
            function.put("nodeId", deviceId);
            function.put("endpointId", endpointId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "setScheduleActive",
//                "deviceType": "Zwave",
//                "nodeId": "23",
//                "active": “true",    //true or false
//    }

    public static String setScheduleActive(String deviceId,boolean active) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setScheduleActive");
            function.put("nodeId", deviceId);
            function.put("deviceType", "Zwave");
            function.put("active", active);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "getScheduleActive",
//                "nodeId": "23",
//    },
//    }


    public static String getScheduleActive(String deviceId ) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getScheduleActive");
            function.put("nodeId", deviceId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "getScheduleList",
//                "nodeId": "23",
//    },
//    }

    public static String getScheduleList(String deviceId ) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getScheduleList");
            function.put("nodeId", deviceId);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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

    public static String setSchedule(String deviceId ,String variableValue,String dayOfWeek,String StartTime,String EndTime,String active) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "setSchedule");
            function.put("nodeId", deviceId);
            function.put("variableValue", variableValue);
            function.put("dayOfWeek", dayOfWeek);
            function.put("StartTime", StartTime);
            function.put("EndTime", EndTime);
            function.put("active", active);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "removeSchedule",
//                "nodeId": "23",
//                "dayOfWeek": "Mon",
//    }
//    }

    public static String removeSchedule(String deviceId ,String dayOfWeek) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeSchedule");
            function.put("nodeId", deviceId);
            function.put("dayOfWeek", dayOfWeek);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getFavoriteList() {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getFavoriteList");
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    //    {
//        "desired": {
//        "function": "getScene",
//    }
    public static String getScene() {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "getSceneList");
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
        String result = "";
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
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String removeSceneAction(String sceneName,String iconName , ArrayList<ScenesInfo> sceneInfoList ) {
        String result = "";
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
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    {
//        "desired": {
//        "function": "removeSceneAction",
//                "sceneName": "test12345",
//                "iconName": "test12345",
//    }
    public static String removeSceneAction(String sceneName,String iconName) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeSceneAction");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


//    {
//        "desired": {
//        "function": "removeScene",
//                "sceneName": "test12345",
//                "iconName": "test12345",
//    }

    public static String removeScene(String sceneName,String iconName) {
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeScene");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
        String result = "";
        JSONObject function = new JSONObject();
        try {
            function.put("function", "removeScene");
            function.put("sceneName", sceneName);
            function.put("iconName", iconName);
            function.put("newName", newName);
            function.put("isFavorite", isFavorite);
            result = getPublicJson(function).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
    String result = "";
    JSONObject function = new JSONObject();
    try {
        function.put("function", "executeScene");
        function.put("sceneName", sceneName);
        function.put("action", action);//run or stop
        result = getPublicJson(function).toString();
    } catch (JSONException e) {
        e.printStackTrace();
    }
    return result;
}



}
