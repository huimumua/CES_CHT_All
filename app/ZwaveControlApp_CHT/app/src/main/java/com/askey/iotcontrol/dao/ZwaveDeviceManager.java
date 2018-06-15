package com.askey.iotcontrol.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.askey.iotcontrol.utils.Const;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skysoft on 2017/1/11.
 */

public class ZwaveDeviceManager {

    private volatile static ZwaveDeviceManager mInstance;
    private DaoMaster.DevOpenHelper openHelper;
    private Context context;

    private ZwaveDeviceManager(){}
    public ZwaveDeviceManager(Context context) {
        this.context = context;
        this.openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
    }

    /**
     * 获取单例引用
     *
     * @param context
     * @return
     */
    public static ZwaveDeviceManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ZwaveDeviceManager.class) {
                if (mInstance == null) {
                    mInstance = new ZwaveDeviceManager(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     */
    public SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }

    /**
     * 插入一条记录
     *
     */
    public void insertZwaveDevice(ZwaveDevice zwaveDevice) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        zwaveDeviceDao.insert(zwaveDevice);
    }

    /**
     *
     */
    public void insertZwaveDeviceList(List<ZwaveDevice> zwaveDeviceList) {
        if (zwaveDeviceList == null || zwaveDeviceList.isEmpty()) {
            return;
        }
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        zwaveDeviceDao.insertInTx(zwaveDeviceList);
    }

    /**
     * 删除一条记录
     *
     * @param id
     */
    public void deleteZwaveDevice(long id) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.ZwaveId.eq(id));
        if (!qb.list().isEmpty()) {
            List<ZwaveDevice> zwaveDeviceList = getRoomDevicesList(qb.list().get(0).getRoomName());
            if (zwaveDeviceList.size() == 1) {
                ZwaveDeviceRoom zwaveDeviceRoom = ZwaveDeviceRoomManager.getInstance(context).getRoom(qb.list().get(0).getRoomName());
                //if (zwaveDeviceRoom != null) {
                //    ZwaveDeviceRoomManager.getInstance(context).deleteRoom(zwaveDeviceRoom);
                //}
            }
            zwaveDeviceDao.deleteByKey(id);
        }
    }

    /**
     * 删除多条记录
     *
     * @param ids
     */
    public void deleteZwaveDevice(Iterable<Long> ids) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        zwaveDeviceDao.deleteByKeyInTx(ids);

    }

    /**
     * 删除所有记录
     *
     */
    public void deleteAll() {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        zwaveDeviceDao.deleteAll();

    }

    /**
     * 更新一条记录
     *
     */
    public void updateZwaveDevice(ZwaveDevice zwaveDevice) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        ZwaveDevice upDateZwaveDevice = zwaveDeviceDao.queryBuilder()
                .where(ZwaveDeviceDao.Properties.ZwaveId.eq(zwaveDevice.getZwaveId())).build().unique();
        if (upDateZwaveDevice != null) {
            upDateZwaveDevice.setBrand(zwaveDevice.getBrand());
            upDateZwaveDevice.setNodeId(zwaveDevice.getNodeId());
            upDateZwaveDevice.setName(zwaveDevice.getName());
            upDateZwaveDevice.setDevType(zwaveDevice.getDevType());
            upDateZwaveDevice.setCategory(zwaveDevice.getCategory());
            upDateZwaveDevice.setRoomName(zwaveDevice.getRoomName());
            upDateZwaveDevice.setFavorite(zwaveDevice.getFavorite());
            upDateZwaveDevice.setNodeInfo(zwaveDevice.getNodeInfo());
            upDateZwaveDevice.setTimestamp(zwaveDevice.getTimestamp());
            upDateZwaveDevice.setInterfaceId(zwaveDevice.getInterfaceId());
            zwaveDeviceDao.update(upDateZwaveDevice);
        }
    }

    public void updateTimeStamp(int nodeId) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.NodeId.eq(nodeId));
        List<ZwaveDevice> list = qb.list();

        java.util.Date date = new java.util.Date();

        if (list != null && list.size() > 0) {
            list.get(0).setTimestamp(date.getTime());
        }
    }

    /**
     */
    public List<ZwaveDevice> queryZwaveDeviceList() {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        List<ZwaveDevice> list = qb.list();
        return list;
    }

    /**
     */
    public String getDeviceType(int nodeId) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.NodeId.eq(nodeId));
        List<ZwaveDevice> list = qb.list();
        if (list != null && list.size() > 0) {
            return list.get(0).getDevType();
        } else {
            return "null";
        }
    }
    /**
     */
    public String getDeviceCategory(int nodeId) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.NodeId.eq(nodeId));
        List<ZwaveDevice> list = qb.list();
        if (list != null && list.size() > 0) {
            return list.get(0).getCategory();
        } else {
            return "null";
        }
    }

    public List<ZwaveDevice> getDeviceIsFavorite(String isFavorite) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.IsFavorite.eq(isFavorite));
        List<ZwaveDevice> list = qb.list();
        return list;
    }

    /**
     */
    public ZwaveDevice queryZwaveDevices(long id) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.ZwaveId.eq(id));
        List<ZwaveDevice> list = qb.list();
        return list.get(0);
    }
    /**
     */
    public ZwaveDevice queryZwaveDevices(int nodeId) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.NodeId.eq(nodeId));
        List<ZwaveDevice> list = qb.list();
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }
    /**
     */
    public ZwaveDevice queryZwaveDevices(String brand,int nodeId) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.Brand.eq(brand),ZwaveDeviceDao.Properties.NodeId.eq(nodeId));
//        qb.and(ZwaveDeviceDao.Properties.HomeId.eq(homeId),ZwaveDeviceDao.Properties.NodeId.eq(nodeId));
        List<ZwaveDevice> list = qb.list();
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public List<String> getRoomNameList() {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(new WhereCondition.StringCondition("roomName != '' GROUP BY roomName"));
        List<ZwaveDevice> list = qb.list();
        List<String> roomName = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            roomName.add(i, list.get(i).getRoomName());
        }
        return roomName;
    }

    public List<ZwaveDevice> getRoomDevicesList(String roomName) {
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.RoomName.eq(roomName));
        List<ZwaveDevice> list = qb.list();
        return list;
    }

    public ZwaveDeviceDao getZwaveDeviceDao() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ZwaveDeviceDao zwaveDevice = daoSession.getZwaveDeviceDao();
        return  zwaveDevice;
    }

    public List<ZwaveDevice> getRecentDeviceList(int num){
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.limit(num).orderDesc(ZwaveDeviceDao.Properties.Timestamp);
        List<ZwaveDevice> list = qb.list();
        return list;
    }

    public void changeRoomName(String oriName, String tarName){
        ZwaveDeviceDao zwaveDeviceDao = getZwaveDeviceDao();
        QueryBuilder<ZwaveDevice> qb = zwaveDeviceDao.queryBuilder();
        qb.where(ZwaveDeviceDao.Properties.RoomName.eq(oriName));
        List<ZwaveDevice> list = qb.list();
        for (ZwaveDevice zwaveDevice : list) {
            zwaveDevice.setRoomName(tarName);
            zwaveDeviceDao.update(zwaveDevice);
        }
    }
}
