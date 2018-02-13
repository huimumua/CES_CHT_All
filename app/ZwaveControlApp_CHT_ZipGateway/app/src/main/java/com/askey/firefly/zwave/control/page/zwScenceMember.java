package com.askey.firefly.zwave.control.page;

/**
 * Created by chiapin on 2017/9/27.
 */

public class zwScenceMember {

    private int scenceId;
    private String roomName;
    private String member;
    private String condition;

    public zwScenceMember(int scenceId, String roomName, String member, String condition) {
        super();
        this.scenceId = scenceId;
        this.roomName = roomName;
        this.member = member;
        this.condition = condition;
    }

    public String getScenceName() {
        return roomName;
    }
    public void setScenceName(String roomName) {
        this.roomName = roomName;
    }

    public int getScenceId() {
        return scenceId;
    }
    public void setScenceId(int scenceId) {
        this.scenceId = scenceId;
    }

    public String getmember(){ return member; }
    public void setMember(String member) { this.member = member;}

    public String getCondition(){ return condition; }
    public void setCondition( String Condition) {this.condition = condition;}
}
