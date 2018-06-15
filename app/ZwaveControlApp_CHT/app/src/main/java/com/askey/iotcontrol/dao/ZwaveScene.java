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
 * Created by edison_chang on 11/29/2017.
 */

@Entity(createInDb = false)
public class ZwaveScene implements Parcelable {
    @Id(autoincrement = true)
    private Long sceneId;
    @Property(nameInDb = "sceneName")
    private String sceneName;
    @Property(nameInDb = "sceneIcon")
    private String sceneIcon;
    @ToMany(joinProperties = {@JoinProperty(name = "sceneName", referencedName = "sceneName")})
    private List<ZwaveDeviceScene> zwaveDeviceSceneList;

    @Generated(hash = 1362749363)
    public ZwaveScene(Long sceneId, String sceneName, String sceneIcon) {
        this.sceneId = sceneId;
        this.sceneName = sceneName;
        this.sceneIcon = sceneIcon;
    }

    protected ZwaveScene(Parcel in) {
        sceneId = in.readLong();
        sceneName = in.readString();
        sceneIcon = in.readString();
    }

    @Generated(hash = 781828586)
    public ZwaveScene() {
    }

    public static final Creator<ZwaveScene> CREATOR = new Creator<ZwaveScene>() {
        @Override
        public ZwaveScene createFromParcel(Parcel in) {
            return new ZwaveScene(in);
        }

        @Override
        public ZwaveScene[] newArray(int size) {
            return new ZwaveScene[size];
        }
    };
    /** Used for active entity operations. */
    @Generated(hash = 1097652909)
    private transient ZwaveSceneDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(sceneId);
        dest.writeString(sceneName);
        dest.writeString(sceneIcon);
    }

    public String getSceneIcon() {
        return this.sceneIcon;
    }

    public void setSceneIcon(String sceneIcon) {
        this.sceneIcon = sceneIcon;
    }

    public String getSceneName() {
        return this.sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public Long getSceneId() {
        return this.sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
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
    @Generated(hash = 753925229)
    public synchronized void resetZwaveDeviceSceneList() {
        zwaveDeviceSceneList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1764753058)
    public List<ZwaveDeviceScene> getZwaveDeviceSceneList() {
        if (zwaveDeviceSceneList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ZwaveDeviceSceneDao targetDao = daoSession.getZwaveDeviceSceneDao();
            List<ZwaveDeviceScene> zwaveDeviceSceneListNew = targetDao._queryZwaveScene_ZwaveDeviceSceneList(sceneName);
            synchronized (this) {
                if(zwaveDeviceSceneList == null) {
                    zwaveDeviceSceneList = zwaveDeviceSceneListNew;
                }
            }
        }
        return zwaveDeviceSceneList;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 625489503)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getZwaveSceneDao() : null;
    }
}
