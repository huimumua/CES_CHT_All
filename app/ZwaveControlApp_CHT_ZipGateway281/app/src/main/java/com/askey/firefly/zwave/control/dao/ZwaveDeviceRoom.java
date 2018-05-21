package com.askey.firefly.zwave.control.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

/**
 * Created by edison_chang on 10/27/2017.
 */

@Entity(createInDb=false)
public class ZwaveDeviceRoom implements Parcelable {
    @Id(autoincrement = true)
    private Long roomId;
    @Property(nameInDb = "roomName")
    private String roomName;
    @Property(nameInDb = "condition")
    private String condition;
    @Property(nameInDb = "sensorNodeId")
    private Integer sensorNodeId;
    @ToMany(joinProperties = {@JoinProperty(name = "roomName", referencedName = "roomName")})
    private List<ZwaveDevice> zwaveDeviceList;

    @Generated(hash = 1859406819)
    public ZwaveDeviceRoom(Long roomId, String roomName, String condition, Integer sensorNodeId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.condition = condition;
        this.sensorNodeId = sensorNodeId;
    }

    protected ZwaveDeviceRoom(Parcel in) {
        roomId = in.readLong();
        roomName = in.readString();
        condition = in.readString();
        sensorNodeId = in.readInt();
    }

    @Generated(hash = 2094434963)
    public ZwaveDeviceRoom() {
    }

    public static final Creator<ZwaveDeviceRoom> CREATOR = new Creator<ZwaveDeviceRoom>() {
        @Override
        public ZwaveDeviceRoom createFromParcel(Parcel in) {
            return new ZwaveDeviceRoom(in);
        }

        @Override
        public ZwaveDeviceRoom[] newArray(int size) {
            return new ZwaveDeviceRoom[size];
        }
    };
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1262371526)
    private transient ZwaveDeviceRoomDao myDao;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(roomId);
        dest.writeString(roomName);
        dest.writeString(condition);
        dest.writeInt(sensorNodeId);
    }

    public Long getRoomId() {
        return this.roomId;
    }

    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomName() {
        return this.roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 236952369)
    public List<ZwaveDevice> getZwaveDeviceList() {
        if (zwaveDeviceList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ZwaveDeviceDao targetDao = daoSession.getZwaveDeviceDao();
            List<ZwaveDevice> zwaveDeviceListNew = targetDao._queryZwaveDeviceRoom_ZwaveDeviceList(roomName);
            synchronized (this) {
                if(zwaveDeviceList == null) {
                    zwaveDeviceList = zwaveDeviceListNew;
                }
            }
        }
        return zwaveDeviceList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 2101297768)
    public synchronized void resetZwaveDeviceList() {
        zwaveDeviceList = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1506060520)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getZwaveDeviceRoomDao() : null;
    }

    public Integer getSensorNodeId() {
        return this.sensorNodeId;
    }

    public void setSensorNodeId(Integer sensorNodeId) {
        this.sensorNodeId = sensorNodeId;
    }
}
