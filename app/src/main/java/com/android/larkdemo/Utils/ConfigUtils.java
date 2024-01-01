package com.android.larkdemo.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ConfigUtils {
    public static String finance_sdk_version = "6.8.8";
    public static boolean is_return_name_auth = true;
    private String configName="myConfig.config";
    private Gson gson;
    public static ConfigUtils instance;
    private ConfigUtils() {
        gson=new Gson();
    }
    private static class ConfigUtilsHolder{
        private static final ConfigUtils instance=new ConfigUtils();
    }
    public static ConfigUtils getInstance(){
        return ConfigUtilsHolder.instance;
    }

    public void saveConfig(ConfigObject configObject) {
        String json=new Gson().toJson(configObject);
        File configFile=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),configName);
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        } catch (IOException e) {
            Log.i("saveConfig", "saveConfig: "+e.getMessage());
        }
    }

    public ConfigObject getConfig() {
        ConfigObject configObject=null;
        File configFile=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),configName);
        Log.i("getConfig", "getConfig: "+configFile.exists()+configFile.getAbsolutePath());;
        if (!configFile.exists()){
            try{
                configFile.createNewFile();
            }catch (IOException e){
                Log.i("getConfig", "getConfig: "+e.getMessage());
            }
        }
        try(FileReader reader =new FileReader(configFile)){
            configObject=gson.fromJson(reader,ConfigObject.class);
            if (configObject==null){
                configObject=new ConfigObject("6.8.8",true,false,false,false,0f,0f,"");
                saveConfig(configObject);
            }
        }catch (IOException e) {
            Log.i("getConfig", "getConfig: "+e.getMessage());
        }
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
        public ConfigObject(){

        }
    }

}
