package com.android.larkdemo.Utils;

import android.annotation.SuppressLint;
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

import de.robv.android.xposed.XSharedPreferences;

public class HookUtils {
    public static String XPOSED_HOOK_PACKAGE = "com.ss.android.lark";
    public static String XPOSED_HOOK_PACKAGE1 = "com.alibaba.android.rimet";


    public static String readConfig(String key) {
//        try {
//            XSharedPreferences xSharedPreferences = new XSharedPreferences(XPOSED_HOOK_PACKAGE, ConfigName);
//            xSharedPreferences.makeWorldReadable();
//        }catch (Exception e){
//            LogUtil.PrintLog("readConfig error:"+e.getMessage(),"readConfig");
//        }
        return "";
    }

    public static void writeConfig(String key, String value) {

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
    public static int compareVersions(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");

        int len1 = v1.length;
        int len2 = v2.length;
        int min = Math.min(len1, len2);

        for (int i = 0; i < min; i++) {
            int part1 = 0;
            int part2 = 0;
            try {
                part1 = Integer.parseInt(v1[i]);
            } catch (NumberFormatException e) {
                part1 = 0;
            }
            try {
                part2 = Integer.parseInt(v2[i]);
            } catch (NumberFormatException e) {
                part2 = 0;
            }
            if (part1 < part2) {
                return -1;
            } else if (part1 > part2) {
                return 1;
            }
        }

        if (len1 < len2) {
            return -1;
        } else if (len1 > len2) {
            return 1;
        } else {
            return 0;
        }
    }

}
