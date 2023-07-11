package com.android.larkdemo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import de.robv.android.xposed.XposedHelpers;

public class HookUtils {
    public static ClassLoader classLoader = null;
    public static String SQLITE_DATABASE = "android.database.sqlite.SQLiteDatabase";
    public static String SQLCIPER_DATABASE = "net.sqlcipher.database.SQLiteDatabase";
    public static String SQLCIPHER_CURSOR = "net.sqlcipher.Cursor";
    public static String SQLCIPHER_OPENHELPER = "net.sqlcipher.database.SQLiteOpenHelper";
    public static String XPOSED_HOOK_PACKAGE = "com.ss.android.lark";
    public static String XPOSED_HOOK_PACKAGE1 = "com.alibaba.android.rimet";


    private static SharedPreferences sharedPreferences = null;

    public static void WriteConfig(String key, String value, Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("MymoduleConfig", context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> packageConfig = sharedPreferences.getStringSet("MymoduleConfig", null);
        packageConfig.add(value);
        editor.putStringSet("MymoduleConfig", packageConfig);
        editor.commit();
    }

    public static boolean IsAimPackage(String packageName) {
        Set<String> packageConfig = sharedPreferences.getStringSet("MymoduleConfig", null);
        for (String s : packageConfig) {
            if (s.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}
