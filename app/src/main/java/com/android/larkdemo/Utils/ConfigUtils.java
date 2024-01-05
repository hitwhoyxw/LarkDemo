package com.android.larkdemo.Utils;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.android.larkdemo.Utils.ConfigObject;

public class ConfigUtils {
    private static String TAG = "ConfigUtils";
    public static String finance_sdk_version = "6.8.8";
    public static boolean is_return_name_auth = true;
    private String configName = "myConfig.config";
    private Gson gson;
    public static ConfigUtils instance;

    private ConfigUtils() {
        gson = new Gson();
    }

    private static class ConfigUtilsHolder {
        private static final ConfigUtils instance = new ConfigUtils();
    }

    public static ConfigUtils getInstance() {
        return ConfigUtilsHolder.instance;
    }

    public void init() {
        ConfigObject configObject = new ConfigObject("6.8.8", true, false, false, false, 0f, 0f, "");
        File configFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), configName);
        Log.i(TAG, "getConfig: " + configFile.exists() + configFile.getAbsolutePath());
        ;
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                saveConfig(configObject);
            } catch (IOException e) {
                Log.i(TAG, "init: " + e.getMessage());
            }
        }
    }

    public void saveConfig(ConfigObject configObject) {
        String json = new Gson().toJson(configObject);
        File configFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), configName);
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        } catch (IOException e) {
            Log.i(TAG, "saveConfig: " + e.getMessage());
        }
    }

    public ConfigObject getConfig() {
        ConfigObject configObject=null;
        File configFile=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),configName);
        try(FileReader reader =new FileReader(configFile)){
            configObject=gson.fromJson(reader,ConfigObject.class);
        }catch (IOException e) {
            Log.i(TAG, "getConfig: " + e.getMessage());
        }
        return configObject;
    }
}
