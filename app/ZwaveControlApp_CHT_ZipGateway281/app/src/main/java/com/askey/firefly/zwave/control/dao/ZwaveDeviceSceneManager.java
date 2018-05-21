package com.askey.firefly.zwave.control.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.askey.firefly.zwave.control.utils.Const;

import java.util.List;

/**
 * Created by edison_chang on 11/30/2017.
 */

public class ZwaveDeviceSceneManager {

    private static ZwaveDeviceSceneManager instance;
    private Context context;
    private DaoMaster.DevOpenHelper openHelper;

    public ZwaveDeviceSceneManager(Context context) {
        this.context = context;
        this.openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
    }

    public static ZwaveDeviceSceneManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ZwaveDeviceSceneManager.class) {
                if (instance == null) {
                    instance = new ZwaveDeviceSceneManager(context);
                }
            }
        }
        return instance;
    }

    public void addZwaveDeviceScene(ZwaveDeviceScene zwaveDeviceScene) {
        ZwaveDeviceSceneDao zwaveDeviceSceneDao = getZwaveDeviceSceneDao();
        zwaveDeviceSceneDao.insert(zwaveDeviceScene);
        zwaveDeviceSceneDao.insertInTx(zwaveDeviceScene);
    }

    public void addZwaveDeviceSceneByList(List<ZwaveDeviceScene> zwaveDeviceSceneList) {
        ZwaveDeviceSceneDao zwaveDeviceSceneDao = getZwaveDeviceSceneDao();
        zwaveDeviceSceneDao.insertInTx(zwaveDeviceSceneList);
    }

    public ZwaveDeviceSceneDao getZwaveDeviceSceneDao() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ZwaveDeviceSceneDao zwaveDeviceSceneDao = daoSession.getZwaveDeviceSceneDao();
        return zwaveDeviceSceneDao;
    }

    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    public SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }
}
