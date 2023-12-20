package com.android.larkdemo.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

public class HookUtils {
    public static String XPOSED_HOOK_PACKAGE = "com.ss.android.lark";
    public static String XPOSED_HOOK_PACKAGE1 = "com.alibaba.android.rimet";

    public static String ConfigPath = "/sdcard/MyModule/config.properties";

    public static String readConfig(String key) {
        File configFile = new File(ConfigPath);
        Properties properties = new Properties();
        try {
            FileInputStream fileInput = new FileInputStream(configFile);
            properties.load(fileInput);
            fileInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }

    public static void writeConfig(String key, String value) {
        File configFile = new File(ConfigPath);
        Properties properties = new Properties();
        try {
            FileInputStream fileInput = new FileInputStream(configFile);
            properties.load(fileInput);
            fileInput.close();

            FileOutputStream fileOutput = new FileOutputStream(configFile);
            properties.setProperty(key, value.toString());
            properties.store(fileOutput, null);
            fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean containMuteWord(String str, String muteWords) {
        String[] muteWordsArray = muteWords.split("\\|");
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (String muteWord : muteWordsArray) {
            if (str.contains(muteWord)) {
                return true;
            }
        }
        return false;
    }

    public static StackTraceElement[] filterStackTrace(StackTraceElement[] stackTraceElements) {
        StackTraceElement[] res = new StackTraceElement[stackTraceElements.length];
        int i = 0;
        for (StackTraceElement s : stackTraceElements) {
            String className = s.getClassName();
            if (!className.contains("xposed") && !className.contains("larkdemo") && !className.contains("lsposed")) {
                res[i] = s;
                i++;
            }
        }
        return res;
    }

    public static String getJsonValue(JsonObject jsonObject, String key) {
        JsonElement valueElement = jsonObject.get(key);
        if (valueElement != null && valueElement.isJsonPrimitive()) {
            JsonPrimitive valuePrimitive = valueElement.getAsJsonPrimitive();
            if (valuePrimitive.isString()) {
                return valuePrimitive.getAsString();
            }
        } else if (valueElement != null && valueElement.isJsonObject()) {
            return getJsonValue(valueElement.getAsJsonObject(), key);
        }
        return null;
    }

}
