package com.android.larkdemo.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static com.android.larkdemo.Utils.DatabaseHelper.TABLE_NAME;

public class ConfigUtils {
    public static String finance_sdk_version = "6.8.8";
    public static boolean is_return_name_auth = true;

    private DatabaseHelper databaseHelper;

    public ConfigUtils(Context context) {
        databaseHelper = new DatabaseHelper(context);
        checkAndCreateDefaultConfig();
    }

    public void checkAndCreateDefaultConfig() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            // 没有配置数据，需要插入默认值
            ConfigObject defaultConfig = new ConfigObject(finance_sdk_version, is_return_name_auth, false, false, false, 0, 0, "");
            ContentValues values = new ContentValues();
            values.put("finance_sdk_version", defaultConfig.finance_sdk_version);
            values.put("is_return_name_auth", defaultConfig.is_return_name_auth ? 1 : 0);
            values.put("isMoudleEnable", defaultConfig.isMoudleEnable ? 1 : 0);
            values.put("isDelayEnable", defaultConfig.isDelayEnable ? 1 : 0);
            values.put("isMuteEnable", defaultConfig.isMuteEnable ? 1 : 0);
            values.put("delayTimeMin", defaultConfig.delayTimeMin);
            values.put("daleyTimeMax", defaultConfig.daleyTimeMax);
            values.put("muteKeyword", defaultConfig.muteKeyword);

            db.insert(TABLE_NAME, null, values);
        }
        cursor.close();
        db.close();
    }

    public void saveConfig(ConfigObject configObject) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("finance_sdk_version", configObject.finance_sdk_version);
        values.put("is_return_name_auth", configObject.is_return_name_auth ? 1 : 0);

        values.put("isMoudleEnable", configObject.isMoudleEnable ? 1 : 0);
        values.put("isDelayEnable", configObject.isDelayEnable ? 1 : 0);
        values.put("isMuteEnable", configObject.isMuteEnable ? 1 : 0);
        values.put("delayTimeMin", configObject.delayTimeMin);
        values.put("daleyTimeMax", configObject.daleyTimeMax);
        values.put("muteKeyword", configObject.muteKeyword);
        db.update(TABLE_NAME, values, null, null);
        db.close();
    }

    public ConfigObject getConfig() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        ConfigObject configObject = null;
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            @SuppressLint("Range") String finance_sdk_version = cursor.getString(cursor.getColumnIndex("finance_sdk_version"));
            @SuppressLint("Range") boolean is_return_name_auth = cursor.getInt(cursor.getColumnIndex("is_return_name_auth")) == 1;
            @SuppressLint("Range") boolean isMoudleEnable = cursor.getInt(cursor.getColumnIndex("isMoudleEnable")) == 1;
            @SuppressLint("Range") boolean isDelayEnable = cursor.getInt(cursor.getColumnIndex("isDelayEnable")) == 1;
            @SuppressLint("Range") boolean isMuteEnable = cursor.getInt(cursor.getColumnIndex("isMuteEnable")) == 1;
            @SuppressLint("Range") float delayTimeMin = cursor.getFloat(cursor.getColumnIndex("delayTimeMin"));
            @SuppressLint("Range") float daleyTimeMax = cursor.getFloat(cursor.getColumnIndex("daleyTimeMax"));
            @SuppressLint("Range") String muteKeyword = cursor.getString(cursor.getColumnIndex("muteKeyword"));

            configObject = new ConfigObject(finance_sdk_version, is_return_name_auth, isMoudleEnable, isDelayEnable, isMuteEnable, delayTimeMin, daleyTimeMax, muteKeyword);
        } else {
            configObject = new ConfigObject(true, true, true, 0, 0, "");
        }
        cursor.close();
        db.close();
        return configObject;
    }

    public class ConfigObject {
        public String finance_sdk_version;
        public boolean is_return_name_auth;
        public boolean isMoudleEnable;
        public boolean isDelayEnable;
        public boolean isMuteEnable;
        public float delayTimeMin;
        public float daleyTimeMax;
        public String muteKeyword;

        public ConfigObject(String finance_sdk_version, boolean is_return_name_auth, boolean isMoudleEnable, boolean isDelayEnable, boolean isMuteEnable, float delayTimeMin, float daleyTimeMax, String muteKeyword) {
            this.finance_sdk_version = finance_sdk_version;
            this.is_return_name_auth = is_return_name_auth;
            this.isMoudleEnable = isMoudleEnable;
            this.isDelayEnable = isDelayEnable;
            this.isMuteEnable = isMuteEnable;
            this.delayTimeMin = delayTimeMin;
            this.daleyTimeMax = daleyTimeMax;
            this.muteKeyword = muteKeyword;
        }

        public ConfigObject(boolean isMoudleEnable, boolean isDelayEnable, boolean isMuteEnable, float delayTimeMin, float daleyTimeMax, String muteKeyword) {
            this.finance_sdk_version = "0.0.0";
            this.is_return_name_auth = true;
            this.isMoudleEnable = isMoudleEnable;
            this.isDelayEnable = isDelayEnable;
            this.isMuteEnable = isMuteEnable;
            this.delayTimeMin = delayTimeMin;
            this.daleyTimeMax = daleyTimeMax;
            this.muteKeyword = muteKeyword;
        }
    }

}
