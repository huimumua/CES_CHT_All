package com.askey.firefly.zwave.control.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.askey.firefly.zwave.control.utils.Const;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by edison_chang on 11/29/2017.
 */

public class ZwaveSceneManager {

    private static ZwaveSceneManager instance;
    private Context context;
    private DaoMaster.DevOpenHelper openHelper;

    public ZwaveSceneManager(Context context) {
        this.context = context;
        this.openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
    }

    public static ZwaveSceneManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ZwaveSceneManager.class) {
                if (instance == null) {
                    instance = new ZwaveSceneManager(context);
                }
            }
        }
        return instance;
    }

    public List<ZwaveScene> getZwaveScene() {
        ZwaveSceneDao zwaveSceneDao = getZwaveSceneDao();
        QueryBuilder<ZwaveScene> qb = zwaveSceneDao.queryBuilder();
        List<ZwaveScene> list = qb.list();
        return list;
    }

    public List<ZwaveScene> getZwaveSceneWithDevice(String sceneName) {
        ZwaveSceneDao zwaveSceneDao = getZwaveSceneDao();
        QueryBuilder<ZwaveScene> qb = zwaveSceneDao.queryBuilder();
        qb.where(ZwaveSceneDao.Properties.SceneName.eq(sceneName));
        List<ZwaveScene> list = qb.list();
        return list;
    }

    public void updateZwaveSceneName(String oldSceneName, String newSceneName) {
        ZwaveSceneDao zwaveSceneDao = getZwaveSceneDao();
        QueryBuilder<ZwaveScene> qb = zwaveSceneDao.queryBuilder();
        qb.where(ZwaveSceneDao.Properties.SceneName.eq(oldSceneName));
        List<ZwaveScene> list = qb.list();
        for (ZwaveScene zwaveScene : list) {
            zwaveScene.setSceneName(newSceneName);
            zwaveSceneDao.update(zwaveScene);
        }
    }

    public void updateZwaveSceneIcon(String sceneName, String sceneIconName) {
        ZwaveSceneDao zwaveSceneDao = getZwaveSceneDao();
        QueryBuilder<ZwaveScene> qb = zwaveSceneDao.queryBuilder();
        qb.where(ZwaveSceneDao.Properties.SceneName.eq(sceneName));
        List<ZwaveScene> list = qb.list();
        for (ZwaveScene zwaveScene : list) {
            zwaveScene.setSceneIcon(sceneIconName);
            zwaveSceneDao.update(zwaveScene);
        }
    }

    public void deleteZwaveScene(String sceneName) {
        ZwaveSceneDao zwaveSceneDao = getZwaveSceneDao();
        QueryBuilder<ZwaveScene> qb = zwaveSceneDao.queryBuilder();
        qb.where(ZwaveSceneDao.Properties.SceneName.eq(sceneName));
        List<ZwaveScene> list = qb.list();
        for (ZwaveScene zwaveScene : list) {
            zwaveSceneDao.delete(zwaveScene);
        }
    }

    public ZwaveSceneDao getZwaveSceneDao() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ZwaveSceneDao zwaveSceneDao = daoSession.getZwaveSceneDao();
        return zwaveSceneDao;
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
