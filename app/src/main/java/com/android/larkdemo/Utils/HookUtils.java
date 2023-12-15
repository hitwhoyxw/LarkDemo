package com.android.larkdemo.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import de.robv.android.xposed.XposedHelpers;

public class HookUtils {
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

    public static boolean CopyFile(File originFile, Context context, String folder) {

        try {
            File destFolder = new File(context.getFilesDir(), folder);
            if (!destFolder.exists()) {
                boolean b = destFolder.mkdirs();
                if (!b) {
                    LogUtil.PrintLog("Directory create failed", "CopyFile");
                    return false;
                }
            }

            Path from = originFile.toPath();
            Path to = destFolder.toPath();
            LogUtil.PrintLog("PATH FROM " + from.toString() + " PATH TO " + to.toString(), "CopyFile");
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LogUtil.PrintLog("CopyFile fail " + e.getMessage().toString(), "CopyFile");
            return false;
        }
        return true;
    }

}
