package com.askey.iotcontrol.page;

/**
 * Created by chiapin on 2017/9/27.
 */

public class RoomMember {

    private int roomId;
    private String roomName;
    private String member;
    private String condition;

    public RoomMember(int roomId, String roomName, String member, String condition) {
        super();
        this.roomId = roomId;
        this.roomName = roomName;
        this.member = member;
        this.condition = condition;
    }

    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomId() {
        return roomId;
    }
    public void setRoomId(int scenceId) {
        this.roomId = roomId;
    }

    public String getmember(){ return member; }
    public void setMember(String member) { this.member = member;}

    public String getCondition(){ return condition; }
    public void setCondition( String Condition) {this.condition = condition;}
}
