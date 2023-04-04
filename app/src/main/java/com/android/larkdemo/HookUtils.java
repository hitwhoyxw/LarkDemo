package com.android.larkdemo;

import de.robv.android.xposed.XposedHelpers;

public class HookUtils {
    public static ClassLoader classLoader = null;
    public static String SQLITE_DATABASE = "android.database.sqlite.SQLiteDatabase";
    public static String SQLCIPER_DATABASE = "net.sqlcipher.database.SQLiteDatabase";
    public static String SQLCIPHER_OPENHELPER = "net.sqlcipher.database.SQLiteOpenHelper";
    public static String XPOSED_HOOK_PACKAGE = "com.ss.android.lark";
    public static byte[] bytes;
}
