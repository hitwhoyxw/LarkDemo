package com.android.larkdemo.Utils;

import android.content.SharedPreferences;

import de.robv.android.xposed.XSharedPreferences;

public class MyConfig {
    public static String finance_sdk_version = "6.8.8";
    public static boolean is_return_name_auth = true;

    public static String MODULE_PACKKAGE_NAME = "com.android.larkdemo";
    public static String MODULE_PREF_NAME = "Moduleconfig";
    public static XSharedPreferences xSharedPreferences=null;

    public static XSharedPreferences init() {
        return init(MODULE_PREF_NAME);
    }
    public static XSharedPreferences init(String path) {
        xSharedPreferences=new XSharedPreferences(MODULE_PACKKAGE_NAME, path);
        return xSharedPreferences;
    }


}
