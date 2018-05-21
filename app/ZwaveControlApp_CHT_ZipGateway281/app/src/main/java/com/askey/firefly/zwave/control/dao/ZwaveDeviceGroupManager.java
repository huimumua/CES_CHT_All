package com.askey.firefly.zwave.control.dao;

import android.content.Context;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edison_chang on 12/1/2017.
 */

public class ZwaveDeviceGroupManager extends GetReadWriteDatabase {

    private static ZwaveDeviceGroupManager instance;

    public ZwaveDeviceGroupManager(Context context) {
        super(context);
    }

    public static ZwaveDeviceGroupManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ZwaveDeviceGroupManager.class) {
                if (instance == null) {
                    instance = new ZwaveDeviceGroupManager(context);
                }
            }
        }
        return instance;
    }

    public void addDeviceGroup(ZwaveDeviceGroup zwaveDeviceGroup) {
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        zwaveDeviceGroupDao.insert(zwaveDeviceGroup);
    }

    public List<ZwaveDeviceGroup> getZwaveDeviceGroupList() {
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        QueryBuilder<ZwaveDeviceGroup> qb = zwaveDeviceGroupDao.queryBuilder();
        List<ZwaveDeviceGroup> list = qb.list();
        return list;
    }

    public List<Integer> getZwaveDeviceGroupListByNodeId(int nodeId) {
        List<Integer> groupIdList = new ArrayList<Integer>();
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        QueryBuilder<ZwaveDeviceGroup> qb = zwaveDeviceGroupDao.queryBuilder();
        qb.where(ZwaveDeviceGroupDao.Properties.NodeId.eq(nodeId));
        List<ZwaveDeviceGroup> list = qb.list();
        for (ZwaveDeviceGroup zwaveDeviceGroup : list) {
            if (-1 == groupIdList.indexOf(zwaveDeviceGroup.getGroupId())){
                groupIdList.add(zwaveDeviceGroup.getGroupId());
            }
        }
        return groupIdList;
    }

    public List<ZwaveDeviceGroup> getZwaveDeviceGroupListByNodeIdAndGroupId(int nodeId, int groupId) {
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        QueryBuilder<ZwaveDeviceGroup> qb = zwaveDeviceGroupDao.queryBuilder();
        qb.where(ZwaveDeviceGroupDao.Properties.NodeId.eq(nodeId), ZwaveDeviceGroupDao.Properties.GroupId.eq(groupId));
        List<ZwaveDeviceGroup> list = qb.list();
        return list;
    }

    public void deleteZwaveDeviceGroup(ZwaveDeviceGroup zwaveDeviceGroup) {
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        zwaveDeviceGroupDao.delete(zwaveDeviceGroup);
    }

    public void deleteZwaveDeviceGroupByNodeId(int nodeId) {
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        QueryBuilder<ZwaveDeviceGroup> qb = zwaveDeviceGroupDao.queryBuilder();
        qb.where(ZwaveDeviceGroupDao.Properties.NodeId.eq(nodeId));
        List<ZwaveDeviceGroup> list = qb.list();
        for (ZwaveDeviceGroup zwaveDeviceGroup : list) {
            zwaveDeviceGroupDao.delete(zwaveDeviceGroup);
        }
    }

    public void deleteZwaveDeviceGroupByInGropNodeId(int inGroupNodeId) {
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        QueryBuilder<ZwaveDeviceGroup> qb = zwaveDeviceGroupDao.queryBuilder();
        qb.where(ZwaveDeviceGroupDao.Properties.InGroupNodeId.eq(inGroupNodeId));
        List<ZwaveDeviceGroup> list = qb.list();
        for (ZwaveDeviceGroup zwaveDeviceGroup : list) {
            zwaveDeviceGroupDao.delete(zwaveDeviceGroup);
        }
    }

    public void deleteZwaveDeviceGroupByInGropNodeId(int inGroupNodeId, int groupId) {
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = getZwaveDeviceGroupDao();
        QueryBuilder<ZwaveDeviceGroup> qb = zwaveDeviceGroupDao.queryBuilder();
        qb.where(ZwaveDeviceGroupDao.Properties.InGroupNodeId.eq(inGroupNodeId),ZwaveDeviceGroupDao.Properties.GroupId.eq(groupId));
        List<ZwaveDeviceGroup> list = qb.list();
        for (ZwaveDeviceGroup zwaveDeviceGroup : list) {
            zwaveDeviceGroupDao.delete(zwaveDeviceGroup);
        }
    }

    public ZwaveDeviceGroupDao getZwaveDeviceGroupDao() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ZwaveDeviceGroupDao zwaveDeviceGroupDao = daoSession.getZwaveDeviceGroupDao();
        return zwaveDeviceGroupDao;
    }
}
