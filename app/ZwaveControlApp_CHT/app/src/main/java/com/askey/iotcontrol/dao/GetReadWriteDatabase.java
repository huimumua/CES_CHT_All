package com.askey.iotcontrol.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.askey.iotcontrol.utils.Const;

/**
 * Created by edison_chang on 12/1/2017.
 */

public class GetReadWriteDatabase {

    private Context context;
    private DaoMaster.DevOpenHelper openHelper;

    public GetReadWriteDatabase(Context context) {
        this.context = context;
        this.openHelper = new DaoMaster.DevOpenHelper(context, Const.DBPATH, null);
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
