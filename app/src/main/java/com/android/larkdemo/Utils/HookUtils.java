package com.android.larkdemo.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class HookUtils {
    public static String XPOSED_HOOK_PACKAGE = "com.ss.android.lark";
    public static String XPOSED_HOOK_PACKAGE1 = "com.alibaba.android.rimet";
    public static Gson gson = new Gson();

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
        if (muteWords == null || muteWords.isEmpty()) {
            return false;
        }
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

    public static void requestPemission(Context context, MyCallback myCallback) {
        if (XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("权限请求");
        builder.setMessage("模块的配置文件读写需要授予外部存储读写权限，是否授予该应用权限？无权限无法工作");

        builder.setPositiveButton("授予", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在此处添加授予权限的逻辑
                XXPermissions.with(context)
                        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                        .request(new OnPermissionCallback() {
                            @Override
                            public void onGranted(List<String> permissions, boolean allGranted) {
                                if (allGranted) {
                                    if (myCallback != null) {
                                        myCallback.onCallback();
                                    }
                                    Toast.makeText(context, "已授予外部存储读写权限", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "授予部分权限成功，可能会影响部分功能的使用", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                                if (doNotAskAgain) {
                                    Toast.makeText(context, "已拒绝外部存储读写权限，请手动授予", Toast.LENGTH_SHORT).show();
                                    XXPermissions.startPermissionActivity(context, permissions);
                                } else {
                                    Toast.makeText(context, "已拒绝外部存储读写权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在此处添加拒绝权限的逻辑
                Toast.makeText(context, "已拒绝权限", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static boolean isAvailableRedPacket(Object redPacketContent) {
        try {
            if (redPacketContent == null) {
                LogUtil.PrintLog("redPacketContent is null", "larkDemo");
                return false;
            }
            Field canGrab = redPacketContent.getClass().getDeclaredField("canGrab");
            canGrab.setAccessible(true);

            Field isExpired = redPacketContent.getClass().getDeclaredField("isExpired");
            isExpired.setAccessible(true);

            Field isGrabbed = redPacketContent.getClass().getDeclaredField("isGrabbed");
            isGrabbed.setAccessible(true);

            Field redPacketId = redPacketContent.getClass().getDeclaredField("redPacketId");
            redPacketId.setAccessible(true);

            Field type = redPacketContent.getClass().getDeclaredField("type");
            type.setAccessible(true);

            if (!canGrab.getBoolean(redPacketContent) || isExpired.getBoolean(redPacketContent) || isGrabbed.getBoolean(redPacketContent)) {
                LogUtil.PrintLog("can not fetch redPacketContent", "larkDemo");
                return false;
            }
        } catch (Exception e) {
            LogUtil.PrintLog("isAvailableRedPacket error:" + e.getMessage(), "larkDemo");
            return false;
        }
        return true;
    }

    public static boolean canFetch(Object redPacketContent, ConfigObject configObject) {
        if (redPacketContent == null) {
            LogUtil.PrintLog("redPacketContent is null", "larkDemo");
            return false;
        }
        if (!isAvailableRedPacket(redPacketContent)) {
            return false;
        }
        try {
            if (!configObject.isMoudleEnable) {
                return false;
            }
            String muteWords = configObject.muteKeyword;
            Field subject = redPacketContent.getClass().getDeclaredField("subject");
            String subjectStr = subject.get(redPacketContent).toString();
            subject.setAccessible(true);
            if (configObject.isMuteEnable && (HookUtils.containMuteWord(subjectStr, muteWords) || HookUtils.containMuteWord(subjectStr, "挂|测|g"))) {
                return false;
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static int getRandDelayTime(ConfigObject configObject) {
        int delayTimeMin = Math.round(configObject.delayTimeMin * 1000);
        int daleyTimeMax = Math.round(configObject.daleyTimeMax * 1000);
        if (delayTimeMin > daleyTimeMax) {
            int temp = delayTimeMin;
            delayTimeMin = daleyTimeMax;
            daleyTimeMax = temp;
        }
        int mSec = new Random().nextInt(daleyTimeMax - delayTimeMin + 1) + delayTimeMin;
        return mSec;
    }

    public static String getObjectString(Object object) {
        return gson.toJson(object);
    }

}

