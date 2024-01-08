package com.android.larkdemo.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.larkdemo.BuildConfig;
import com.android.larkdemo.Utils.ConfigObject;

import de.robv.android.xposed.XSharedPreferences;

public class ConfigUtils {
    private static String TAG = "ConfigUtils";
    public static String finance_sdk_version = "6.8.8";
    public static boolean is_return_name_auth = true;
    private String configName = "myConfig";

    public SharedPreferences sharedPreferences;
    public static ConfigUtils instance;

    private ConfigUtils() {

    }

    private static class ConfigUtilsHolder {
        private static final ConfigUtils instance = new ConfigUtils();
    }

    public static ConfigUtils getInstance() {
        return ConfigUtilsHolder.instance;
    }

    @SuppressLint("WorldReadableFiles")
    public void init(boolean isModule, Context context) {
        if (isModule) {
            sharedPreferences = context.getSharedPreferences(configName, Context.MODE_WORLD_READABLE);
        } else {
            sharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, configName);
        }
    }

    public void saveConfig(ConfigObject configObject) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isMoudleEnable", configObject.isMoudleEnable);
            editor.putBoolean("isDelayEnable", configObject.isDelayEnable);
            editor.putBoolean("isMuteEnable", configObject.isMuteEnable);
            editor.putFloat("delayTimeMin", configObject.delayTimeMin);
            editor.putFloat("daleyTimeMax", configObject.daleyTimeMax);
            editor.putString("muteKeyword", configObject.muteKeyword);
            editor.commit();
        } catch (Exception e) {
            Log.i(TAG, "saveConfig: " + e.getMessage());
        }
    }

    public ConfigObject getConfig() {
        ConfigObject configObject = null;
        try {
            configObject = new ConfigObject(
                    sharedPreferences.getBoolean("isMoudleEnable", false),
                    sharedPreferences.getBoolean("isDelayEnable", false),
                    sharedPreferences.getBoolean("isMuteEnable", false),
                    sharedPreferences.getFloat("delayTimeMin", 0f),
                    sharedPreferences.getFloat("daleyTimeMax", 0f),
                    sharedPreferences.getString("muteKeyword", "")
            );
        } catch (Exception e) {
            Log.i(TAG, "getConfig: " + e.getMessage());
        }
        return configObject;
    }
}
