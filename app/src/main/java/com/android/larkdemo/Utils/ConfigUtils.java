package com.android.larkdemo.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.larkdemo.BuildConfig;
import com.android.larkdemo.Utils.ConfigObject;
import com.google.gson.Gson;

import de.robv.android.xposed.XSharedPreferences;

public class ConfigUtils {
    private static String TAG = "ConfigUtils";
    public static String finance_sdk_version = "6.8.8";
    public static boolean is_return_name_auth = true;
    private String configName = "myConfig";

    private SharedPreferences sharedPreferences;
    private XSharedPreferences xSharedPreferences;
    private XSharedPreferences.OnSharedPreferenceChangeListener listener;
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
            xSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, configName);
        }
    }


    public XSharedPreferences getxSharedPreferences() {
        return xSharedPreferences != null ? xSharedPreferences : null;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences != null ? sharedPreferences : null;
    }

    public void setOnSharedPreferenceChangeListener(XSharedPreferences.OnSharedPreferenceChangeListener listener) {
        if (sharedPreferences != null) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    public void setOnSharedPreferenceChangeListener() {
        if (null == listener) {
            listener = new XSharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    LogUtil.PrintLog("onSharedPreferenceChanged", TAG);
                    xSharedPreferences.reload();
                }
            };
        }
        if (sharedPreferences != null) {
            LogUtil.PrintLog("setOnSharedPreferenceChangeListener", TAG);
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    public void unSetOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        if (sharedPreferences != null && listener != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    public void unSetOnSharedPreferenceChangeListener() {
        if (sharedPreferences != null && listener != null) {
            LogUtil.PrintLog("unSetOnSharedPreferenceChangeListener", TAG);
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
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
            editor.putBoolean("fetchMode", configObject.fetchMode);
            editor.apply();
        } catch (Exception e) {
            Log.i(TAG, "saveConfig: " + e.getMessage());
        }
    }

    public ConfigObject getConfig(boolean isModule) {
        ConfigObject configObject = null;
        if (isModule) {
            return getConfigInModule();
        } else {
            return getConfigInApp();
        }
    }

    public ConfigObject getConfigInModule() {
        ConfigObject configObject = null;
        try {
            configObject = new ConfigObject(
                    sharedPreferences.getBoolean("isMoudleEnable", false),
                    sharedPreferences.getBoolean("isDelayEnable", false),
                    sharedPreferences.getBoolean("isMuteEnable", false),
                    sharedPreferences.getFloat("delayTimeMin", 0f),
                    sharedPreferences.getFloat("daleyTimeMax", 0f),
                    sharedPreferences.getString("muteKeyword", ""),
                    sharedPreferences.getBoolean("fetchMode", false)
            );
        } catch (Exception e) {
            Log.i(TAG, "getConfigInModule: " + e.getMessage());
        }
        return configObject;
    }

    public ConfigObject getConfigInApp() {
        ConfigObject configObject = null;
        try {
            configObject = new ConfigObject(
                    xSharedPreferences.getBoolean("isMoudleEnable", false),
                    xSharedPreferences.getBoolean("isDelayEnable", false),
                    xSharedPreferences.getBoolean("isMuteEnable", false),
                    xSharedPreferences.getFloat("delayTimeMin", 0f),
                    xSharedPreferences.getFloat("daleyTimeMax", 0f),
                    xSharedPreferences.getString("muteKeyword", ""),
                    xSharedPreferences.getBoolean("fetchMode", false)
            );
        } catch (Exception e) {
            Log.i(TAG, "getConfigInApp: " + e.getMessage());
        }
        return configObject;
    }

}
