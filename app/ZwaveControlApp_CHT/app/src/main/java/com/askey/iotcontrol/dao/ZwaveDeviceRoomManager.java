package com.askey.iotcontrol.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.askey.iotcontrol.utils.Const;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by edison_chang on 10/27/2017.
 */

public class ZwaveDeviceRoomManager {

    private static ZwaveDeviceRoomManager instance;
    private Context context;
    private DaoMaster.DevOpenHelper openHelper;

    public ZwaveDeviceRoomManager(Context context) {
        this.context = context;
        this.openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
    }

    public static ZwaveDeviceRoomManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ZwaveDeviceRoomManager.class) {
                if (instance == null) {
                    instance = new ZwaveDeviceRoomManager(context);
                }
            }
        }
        return instance;
    }

    public void insertDeviceRoom(ZwaveDeviceRoom zwaveDeviceRoom) {
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();
        zwaveDeviceRoomDao.insert(zwaveDeviceRoom);
    }

    public void insertDeviceRoomList(List<ZwaveDeviceRoom> zwaveDeviceRoomList) {
        if (zwaveDeviceRoomList == null || zwaveDeviceRoomList.isEmpty()) return;
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();
        zwaveDeviceRoomDao.insertInTx(zwaveDeviceRoomList);
    }

    public void updateRoom(ZwaveDeviceRoom zwaveDeviceRoom, String roomName) {
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();
        ZwaveDeviceRoom updateZwaveDeviceRoom =
                zwaveDeviceRoomDao.queryBuilder().where(ZwaveDeviceRoomDao.Properties.RoomName.eq(roomName)).build().unique();
        if (updateZwaveDeviceRoom != null) {
            updateZwaveDeviceRoom.setRoomName(roomName);
            updateZwaveDeviceRoom.setCondition(zwaveDeviceRoom.getCondition());
            updateZwaveDeviceRoom.setSensorNodeId(zwaveDeviceRoom.getSensorNodeId());
            zwaveDeviceRoomDao.update(updateZwaveDeviceRoom);
        }
    }

    public void deleteRoom(ZwaveDeviceRoom zwaveDeviceRoom) {
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();
        zwaveDeviceRoomDao.deleteInTx(zwaveDeviceRoom);
    }

    public void deleteRoom(String roomName) {
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();
        ZwaveDeviceRoom zwaveDeviceRoom =
                zwaveDeviceRoomDao.queryBuilder().where(ZwaveDeviceRoomDao.Properties.RoomName.eq(roomName)).build().unique();
        if (zwaveDeviceRoom != null) {
            zwaveDeviceRoomDao.deleteInTx(zwaveDeviceRoom);
        }
    }

    public void changeRoomName(String oriName, String tarName) {
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();

        ZwaveDeviceRoom updateZwaveDeviceRoom =
                zwaveDeviceRoomDao.queryBuilder().where(ZwaveDeviceRoomDao.Properties.RoomName.eq(oriName)).build().unique();
        if (updateZwaveDeviceRoom != null) {
            updateZwaveDeviceRoom.setRoomName(tarName);
            zwaveDeviceRoomDao.update(updateZwaveDeviceRoom);
        }
    }

    public List<ZwaveDeviceRoom> getRoom() {
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();
        QueryBuilder<ZwaveDeviceRoom> qb = zwaveDeviceRoomDao.queryBuilder();
        List<ZwaveDeviceRoom> list = qb.list();
        return list;
    }

    public ZwaveDeviceRoom getRoom(String roomName) {
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = getZwaveDeviceRoomDao();
        QueryBuilder<ZwaveDeviceRoom> qb = zwaveDeviceRoomDao.queryBuilder();
        qb.where(ZwaveDeviceRoomDao.Properties.RoomName.eq(roomName));
        if (qb.list().isEmpty()) return null;
        ZwaveDeviceRoom zwaveDeviceRoom = qb.list().get(0);
        return zwaveDeviceRoom;
    }

    public ZwaveDeviceRoomDao getZwaveDeviceRoomDao() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ZwaveDeviceRoomDao zwaveDeviceRoomDao = daoSession.getZwaveDeviceRoomDao();
        return zwaveDeviceRoomDao;
    }

    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context,  Const.DBPATH, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    public SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context,  Const.DBPATH, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }
}
