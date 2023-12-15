package com.android.larkdemo.Utils;

import de.robv.android.xposed.XSharedPreferences;

public class XSharedPreferenUtil {
    public static XSharedPreferences xSharedPreferences;

    public static void initConfig(String packageName) {
        xSharedPreferences = new XSharedPreferences(packageName);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return xSharedPreferences.getBoolean(key, defaultValue);
    }

    public static String getString(String key, String defaultValue) {
        return xSharedPreferences.getString(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return xSharedPreferences.getInt(key, defaultValue);
    }

}
