package com.askey.firefly.zwave.control.dao;

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
 * Created by edison_chang on 10/27/2017.
 */

@Entity(createInDb=false)
public class ZwaveDeviceScene implements Parcelable {
    @Id(autoincrement = true)
    private Long sceneId;
    @Property(nameInDb = "scene")
    private String scene;
    @Property(nameInDb = "condition")
    private String condition;
    @Property(nameInDb = "sensorNodeId")
    private Integer sensorNodeId;
    @ToMany(joinProperties = {@JoinProperty(name = "scene", referencedName = "scene")})
    private List<ZwaveDevice> zwaveDeviceList;

    @Generated(hash = 1112052445)
    public ZwaveDeviceScene(Long sceneId, String scene, String condition, Integer sensorNodeId) {
        this.sceneId = sceneId;
        this.scene = scene;
        this.condition = condition;
        this.sensorNodeId = sensorNodeId;
    }

    protected ZwaveDeviceScene(Parcel in) {
        sceneId = in.readLong();
        scene = in.readString();
        condition = in.readString();
        sensorNodeId = in.readInt();
    }

    @Generated(hash = 1179282909)
    public ZwaveDeviceScene() {
    }

    public static final Creator<ZwaveDeviceScene> CREATOR = new Creator<ZwaveDeviceScene>() {
        @Override
        public ZwaveDeviceScene createFromParcel(Parcel in) {
            return new ZwaveDeviceScene(in);
        }

        @Override
        public ZwaveDeviceScene[] newArray(int size) {
            return new ZwaveDeviceScene[size];
        }
    };
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1943082237)
    private transient ZwaveDeviceSceneDao myDao;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(sceneId);
        dest.writeString(scene);
        dest.writeString(condition);
        dest.writeInt(sensorNodeId);
    }

    public Long getSceneId() {
        return this.sceneId;
    }

    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }

    public String getScene() {
        return this.scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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
    @Generated(hash = 324299710)
    public List<ZwaveDevice> getZwaveDeviceList() {
        if (zwaveDeviceList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ZwaveDeviceDao targetDao = daoSession.getZwaveDeviceDao();
            List<ZwaveDevice> zwaveDeviceListNew = targetDao
                    ._queryZwaveDeviceScene_ZwaveDeviceList(scene);
            synchronized (this) {
                if (zwaveDeviceList == null) {
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
    @Generated(hash = 1999946516)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getZwaveDeviceSceneDao() : null;
    }

    public Integer getSensorNodeId() {
        return this.sensorNodeId;
    }

    public void setSensorNodeId(Integer sensorNodeId) {
        this.sensorNodeId = sensorNodeId;
    }
}
