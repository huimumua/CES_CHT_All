package com.askey.iotcontrol.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.askey.iotcontrol.utils.Const;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by edison_chang on 11/23/2017.
 */

public class ZwaveScheduleManager extends GetReadWriteDatabase {

    private static ZwaveScheduleManager instance;

    public ZwaveScheduleManager(Context context) {
        super(context);
    }

    public static ZwaveScheduleManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ZwaveScheduleManager.class) {
                if (instance == null) {
                    instance = new ZwaveScheduleManager(context);
                }
            }
        }
        return instance;
    }

    public List<ZwaveSchedule> getZwaveScheduleList(int nodeId) {
        ZwaveScheduleDao zwaveScheduleDao = getZwaveDeviceScheduleDao();
        QueryBuilder<ZwaveSchedule> qb = zwaveScheduleDao.queryBuilder();
        qb.where(ZwaveScheduleDao.Properties.NodeId.eq(nodeId));
        List<ZwaveSchedule> list = qb.list();
        return list;
    }

    public ZwaveSchedule getZwaveSchedule(int jobId) {
        ZwaveScheduleDao zwaveScheduleDao = getZwaveDeviceScheduleDao();
        QueryBuilder<ZwaveSchedule> qb = zwaveScheduleDao.queryBuilder();
        qb.where(ZwaveScheduleDao.Properties.JobId.eq(jobId));
        List<ZwaveSchedule> list = qb.list();
        return list.get(0);
    }

    public List<ZwaveSchedule> getZwaveScheduleList() {
        ZwaveScheduleDao zwaveScheduleDao = getZwaveDeviceScheduleDao();
        QueryBuilder<ZwaveSchedule> qb = zwaveScheduleDao.queryBuilder();
        List<ZwaveSchedule> list = qb.list();
        return list;
    }

    public void addZwaveSchedule(ZwaveSchedule zwaveSchedule) {
        ZwaveScheduleDao zwaveScheduleDao = getZwaveDeviceScheduleDao();
        zwaveScheduleDao.insert(zwaveSchedule);
    }

    public void updateZwaveScheduleActive(int nodeId, String active) {
        ZwaveScheduleDao zwaveScheduleDao = getZwaveDeviceScheduleDao();
        QueryBuilder<ZwaveSchedule> qb = zwaveScheduleDao.queryBuilder();
        qb.where(ZwaveScheduleDao.Properties.NodeId.eq(nodeId));
        List<ZwaveSchedule> list = qb.list();
        for (ZwaveSchedule zwaveSchedule : list) {
            zwaveSchedule.setActive(active);
            zwaveScheduleDao.update(zwaveSchedule);
        }
    }

    public void deleteZwaveSchedule(ZwaveSchedule zwaveSchedule) {
        ZwaveScheduleDao zwaveScheduleDao = getZwaveDeviceScheduleDao();
        zwaveScheduleDao.delete(zwaveSchedule);
    }

    public void deleteZwaveSchedule(int jobId) {
        ZwaveScheduleDao zwaveScheduleDao = getZwaveDeviceScheduleDao();
        DeleteQuery<ZwaveSchedule> dq = zwaveScheduleDao.queryBuilder()
                .where(ZwaveScheduleDao.Properties.JobId.eq(jobId)).buildDelete();
        dq.executeDeleteWithoutDetachingEntities();
    }

    public ZwaveScheduleDao getZwaveDeviceScheduleDao() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ZwaveScheduleDao zwaveScheduleDao = daoSession.getZwaveScheduleDao();
        return zwaveScheduleDao;
    }
}
