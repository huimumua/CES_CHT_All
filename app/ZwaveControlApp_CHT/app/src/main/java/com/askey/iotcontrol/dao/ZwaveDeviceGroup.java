package com.askey.iotcontrol.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.DaoException;

/**
 * Created by edison_chang on 12/1/2017.
 */

@Entity(createInDb = false)
public class ZwaveDeviceGroup implements Parcelable {
    @Id(autoincrement = true)
    private Long deviceGroupId;
    @Property(nameInDb = "nodeId")
    private Integer nodeId;
    @Property(nameInDb = "groupId")
    private Integer groupId;
    @Property(nameInDb = "inGroupNodeId")
    private Integer inGroupNodeId;
    @Property(nameInDb = "interfaceId")
    private Integer interfaceId;
    @Property(nameInDb = "endpointId")
    private Integer endpointId;
    @ToMany(joinProperties = {@JoinProperty(name = "inGroupNodeId", referencedName = "nodeId")})
    private List<ZwaveDevice> zwaveDeviceList;

    @Generated(hash = 967581747)
    public ZwaveDeviceGroup(Long deviceGroupId, Integer nodeId, Integer groupId, Integer inGroupNodeId,
            Integer interfaceId, Integer endpointId) {
        this.deviceGroupId = deviceGroupId;
        this.nodeId = nodeId;
        this.groupId = groupId;
        this.inGroupNodeId = inGroupNodeId;
        this.interfaceId = interfaceId;
        this.endpointId = endpointId;
    }

    protected ZwaveDeviceGroup(Parcel in) {
        deviceGroupId = in.readLong();
        nodeId = in.readInt();
        groupId = in.readInt();
        inGroupNodeId = in.readInt();
        interfaceId = in.readInt();
        endpointId = in.readInt();
    }

    @Generated(hash = 1227530495)
    public ZwaveDeviceGroup() {
    }

    public static final Creator<ZwaveDeviceGroup> CREATOR = new Creator<ZwaveDeviceGroup>() {
        @Override
        public ZwaveDeviceGroup createFromParcel(Parcel in) {
            return new ZwaveDeviceGroup(in);
        }

        @Override
        public ZwaveDeviceGroup[] newArray(int size) {
            return new ZwaveDeviceGroup[size];
        }
    };
    /** Used for active entity operations. */
    @Generated(hash = 1861426710)
    private transient ZwaveDeviceGroupDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(deviceGroupId);
        dest.writeInt(nodeId);
        dest.writeInt(groupId);
        dest.writeInt(inGroupNodeId);
        dest.writeInt(interfaceId);
        dest.writeInt(endpointId);
    }

    public Integer getEndpointId() {
        return this.endpointId;
    }

    public void setEndpointId(Integer endpointId) {
        this.endpointId = endpointId;
    }

    public Integer getInterfaceId() {
        return this.interfaceId;
    }

    public void setInterfaceId(Integer interfaceId) {
        this.interfaceId = interfaceId;
    }

    public Integer getInGroupNodeId() {
        return this.inGroupNodeId;
    }

    public void setInGroupNodeId(Integer inGroupNodeId) {
        this.inGroupNodeId = inGroupNodeId;
    }

    public Integer getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Long getDeviceGroupId() {
        return this.deviceGroupId;
    }

    public void setDeviceGroupId(Long deviceGroupId) {
        this.deviceGroupId = deviceGroupId;
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

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 2101297768)
    public synchronized void resetZwaveDeviceList() {
        zwaveDeviceList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 886880497)
    public List<ZwaveDevice> getZwaveDeviceList() {
        if (zwaveDeviceList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ZwaveDeviceDao targetDao = daoSession.getZwaveDeviceDao();
            List<ZwaveDevice> zwaveDeviceListNew = targetDao._queryZwaveDeviceGroup_ZwaveDeviceList(inGroupNodeId);
            synchronized (this) {
                if(zwaveDeviceList == null) {
                    zwaveDeviceList = zwaveDeviceListNew;
                }
            }
        }
        return zwaveDeviceList;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1122757813)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getZwaveDeviceGroupDao() : null;
    }
}
