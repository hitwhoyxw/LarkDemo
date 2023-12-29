package com.android.larkdemo.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String DB_NAME = "myModule.db";
    public static String TABLE_NAME = "module_config";

    public DatabaseHelper(Context context) {
        super(context, getDBPath(context), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 创建表格
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "finance_sdk_version TEXT, " +
                "is_return_name_auth INTEGER, " +
                "isMoudleEnable INTEGER, " +
                "isDelayEnable INTEGER, " +
                "isMuteEnable INTEGER, " +
                "delayTimeMin REAL, " +
                "daleyTimeMax REAL, " +
                "muteKeyword TEXT" +
                ")";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public static String getDBPath(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + DB_NAME;
    }
}
