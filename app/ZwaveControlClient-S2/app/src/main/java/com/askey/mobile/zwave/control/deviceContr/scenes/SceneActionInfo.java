package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by skysoft on 2017/12/11.
 */

public class SceneActionInfo implements Parcelable {

    private int actionId;
    private String type;
    private String name;
    private String action;
    private String nodeId;

//    protected SceneActionInfo(Parcel in) {
//        actionId = in.readInt();
//        type = in.readString();
//        name = in.readString();
//        action = in.readString();
//        nodeId = in.readString();
//        lightValue = in.readString();
//        timer = in.readString();
//    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(actionId);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(action);
        dest.writeString(nodeId);
        dest.writeString(lightValue);
        dest.writeString(timer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SceneActionInfo> CREATOR = new Creator<SceneActionInfo>() {
        @Override
        public SceneActionInfo createFromParcel(Parcel in) {
            SceneActionInfo sceneActionInfo = new SceneActionInfo();
            sceneActionInfo.setActionId(in.readInt());
            sceneActionInfo.setType(in.readString());
            sceneActionInfo.setName(in.readString());
            sceneActionInfo.setAction(in.readString());
            sceneActionInfo.setNodeId(in.readString());
            sceneActionInfo.setLightValue(in.readString());
            sceneActionInfo.setTimer(in.readString());
            return sceneActionInfo;
        }

        @Override
        public SceneActionInfo[] newArray(int size) {
            return new SceneActionInfo[size];
        }
    };

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }




    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    private String lightValue;
    private String timer;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLightValue() {
        return lightValue;
    }

    public void setLightValue(String lightValue) {
        this.lightValue = lightValue;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }


}
