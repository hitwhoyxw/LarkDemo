package com.android.larkdemo.Hook;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.larkdemo.Utils.ConfigObject;
import com.android.larkdemo.Utils.ConfigUtils;
import com.android.larkdemo.Utils.HookUtils;
import com.android.larkdemo.Utils.LogUtil;
import com.android.larkdemo.Utils.MyCallback;
import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Random;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.android.larkdemo.Utils.HookUtils.filterStackTrace;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class HookEntry implements IXposedHookLoadPackage {
    public static String TAG = "Demo";
    private ConfigUtils configUtils;

    private static ConfigObject configObject;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(HookUtils.XPOSED_HOOK_PACKAGE)) {
            //xSharedPreferences.reload();
            ClassLoader classLoader = loadPackageParam.classLoader;
            XposedBridge.log(TAG + " has Hooked!");
            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    ClassLoader dexClassLoader = context.getClassLoader();
                    if (dexClassLoader == null) {
                        XposedBridge.log("cannot get classloader return ");
                        return;
                    }
                    HookStackTrace(dexClassLoader);
                    //HookSDKSender(dexClassLoader, false);
                    HookMsg(dexClassLoader);
                    //HookNotification(dexClassLoader);
                }
            });
        }
    }

    public void getPermissionHook(ClassLoader dexClassLoader) {
        try {
            XposedHelpers.findAndHookMethod("com.ss.android.lark.main.app.MainActivity", dexClassLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.thisObject;
                    initConfigSetting(context);
                    configObject = ConfigObject.CreateDefaultConfigObject(true);
//                    HookUtils.requestPemission(context, new MyCallback() {
//                        @Override
//                        public void onCallback() {
//                            configObject = configUtils.getConfig();
//                        }
//                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public Method HookSDKSender(ClassLoader dexClassLoader, boolean getMethod) {
//        try {
//            //device_info{"finance_sdk_version":"6.8.8,"hongbao_type":"NORMAL","id":"xxx","is_return_name_auth":"true"}
//            //Command.GRAB_HONGBAO=2401
//            Class sdkSenderCls = findClass("com.ss.android.lark.sdk.SdkSender", dexClassLoader);
//            String methodName = "asynSendRequestNonWrap";
//            int paramCount = 4;
//            if (sdkSenderCls != null) {
//                Method[] methods = sdkSenderCls.getDeclaredMethods();
//                for (Method m : methods) {
//                    if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
//                        LogUtil.PrintLog("find sdkSender.asynSendRequestNonWrap", "sdkSenderHook");
//                        if (getMethod) {
//                            return m;
//                        }
//                        XposedBridge.hookMethod(m, new XC_MethodHook() {
//                            @Override
//                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                                Object command = param.args[0];
//                                Object msgBuilder = param.args[1];
//                                String strCmd = new Gson().toJson(command);
//                                String strMsg = new Gson().toJson(msgBuilder);
//                                if (strCmd.contains("GRAB_HONGBAO")) {
//                                    JsonParser parser = new JsonParser();
//                                    JsonObject jsonObject = parser.parse(strMsg).getAsJsonObject();
//                                    if (jsonObject.has("device_info")) {
//                                        JsonObject device_info = jsonObject.get("device_info").getAsJsonObject();
//                                        String version = device_info.get("finance_sdk_version").getAsString();
//
//                                        ConfigObject configObject = configUtils.getConfig();
//                                        if (configObject == null) {
//                                            return;
//                                        }
//                                        String configVersion = configObject.finance_sdk_version;
//                                        if (HookUtils.compareVersions(version, configVersion) >= 0) {
//                                            configVersion = version;
//                                            configObject.finance_sdk_version = version;
//                                            configUtils.saveConfig(configObject);
//                                            LogUtil.PrintLog("update version finance_sdk_version=" + version, "sdkSenderHook");
//                                        }
//                                    }
//                                }
//                                LogUtil.PrintLog("command = " + strCmd, methodName);
//                                LogUtil.PrintLog("msgBuilder = " + strMsg, methodName);
//                                if (strCmd.contains("HONGBAO")) {
//                                    //LogUtil.PrintStackTrace(0);
//                                }
//                            }
//                        });
//                        break;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            LogUtil.PrintLog(e.toString(), "asynSendRequestNonWrap");
//        }
//        return null;
//    }

    public void HookMsg(ClassLoader dexClassLoader) {
        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.ss.android.lark.chatbase.BasePageStore$1", dexClassLoader), "add", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws IOException {
                    Gson gson = new Gson();
                    String strMsg = gson.toJson(param.args[param.args.length - 1]);
                    LogUtil.PrintLog("addMsg = " + strMsg, "TAG");

                    ConfigObject configObject = configUtils.getConfig(false);
                    if (configObject == null) {
                        LogUtil.PrintLog("HookMsg configObject is null", TAG);
                        return;
                    }
                    LogUtil.PrintLog("configObject = " + new Gson().toJson(configObject), "configObject");
                    JsonObject jsonObject = gson.fromJson(strMsg, JsonObject.class);
                    String type = jsonObject.getAsJsonObject("mMessage").get("type").getAsString();
                    if (type != null && type.equals("RED_PACKET") && configObject.isMoudleEnable) {
                        LogUtil.PrintLog("find redpacket", "hookmsg");
                        String redPacketId = jsonObject.getAsJsonObject("mMessage").getAsJsonObject("messageContent").get("redPacketId").getAsString();
                        String subject = jsonObject.getAsJsonObject("mMessage").getAsJsonObject("messageContent").get("subject").getAsString();

                        if (configObject.isMuteEnable && HookUtils.containMuteWord(subject, configObject.muteKeyword)) {
                            LogUtil.PrintLog("mute redpacket subject = " + subject, TAG);
                            return;
                        }
                        if (configObject.isDelayEnable) {
                            LogUtil.PrintLog("delay grab redpacket subject = " + subject, TAG);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int delayTimeMin = (int) configObject.delayTimeMin * 1000;
                                        int daleyTimeMax = (int) configObject.daleyTimeMax * 1000;
                                        int mSec = new Random().nextInt(daleyTimeMax - delayTimeMin + 1) + delayTimeMin;
                                        LogUtil.PrintLog("delay grab time = " + mSec, TAG);
                                        Thread.sleep(mSec);
                                        CallRequestBuilder(dexClassLoader, redPacketId, ConfigUtils.finance_sdk_version, ConfigUtils.is_return_name_auth, 1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }

                        else {
                            CallRequestBuilder(dexClassLoader, redPacketId, ConfigUtils.finance_sdk_version, ConfigUtils.is_return_name_auth, 1);
                        }

                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void HookPluginLoader(ClassLoader dexClassLoader) {
        try {
            XposedHelpers.findAndHookMethod("com.bytedance.mira.plugin.b", dexClassLoader, "a", java.io.File.class, java.lang.String.class, int.class, java.lang.String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.args[0];
                    if (!file.getAbsolutePath().contains("money")) {
                        return;
                    }
                    String str = (String) param.args[1];
                    int i = (int) param.args[2];
                    String str2 = (String) param.args[3];
                    LogUtil.PrintLog("plugin file path " + file.getAbsolutePath() + " plugin name " + str + " plugin version" + i + " class name" + str2, "com.bytedance.mira.plugin.b.a");
                    Context context = (Context) XposedHelpers.callMethod(file, "getApplicationContext");
                    //HookUtils.CopyFile(file, context, "myplugin");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CallRequestBuilder(ClassLoader dexClassLoader, String id, String finance_sdk_version, boolean is_return_name_auth, int hongbao_type) {
        try {
            Class<?> builderClass = findClass("com.bytedance.lark.pb.im.v1.GrabHongbaoRequest$Builder", dexClassLoader);
            Class<?> redPacketTypeClass = findClass("com.bytedance.lark.pb.im.v1.HongbaoType", dexClassLoader);

            Object builderInstance = builderClass.newInstance();

            // 设置Builder对象的字段值
            builderClass.getMethod("id", String.class).invoke(builderInstance, id);
            builderClass.getMethod("is_return_name_auth", Boolean.class).invoke(builderInstance, is_return_name_auth);
            builderClass.getMethod("hongbao_type", findClass("com.bytedance.lark.pb.im.v1.HongbaoType", dexClassLoader)).invoke(builderInstance, callStaticMethod(redPacketTypeClass, "fromValue", hongbao_type));
            //builderClass.getMethod("chat_id",long.class).invoke(builderInstance, 0L);

            // 构建DeviceInfo对象
            Class<?> deviceInfoClass = findClass("com.bytedance.lark.pb.im.v1.GrabHongbaoRequest$DeviceInfo", dexClassLoader);
            Class<?> deviceInfoBuilderClass = findClass("com.bytedance.lark.pb.im.v1.GrabHongbaoRequest$DeviceInfo$Builder", dexClassLoader);

            Object deviceInfoBuilderInstance = deviceInfoBuilderClass.newInstance();
            deviceInfoBuilderClass.getMethod("finance_sdk_version", String.class).invoke(deviceInfoBuilderInstance, finance_sdk_version);
            Object deviceInfoInstance = deviceInfoBuilderClass.getMethod("build").invoke(deviceInfoBuilderInstance);


            builderClass.getMethod("device_info", deviceInfoClass).invoke(builderInstance, deviceInfoInstance);

            //Object grabHongbaoRequestInstance = builderClass.getMethod("build").invoke(builderInstance);

            Class commandCls = findClass("com.bytedance.lark.pb.basic.v1.Command", dexClassLoader);
            Object objCmd = callStaticMethod(commandCls, "fromValue", 2401);

            Class sdkSenderCls = findClass("com.ss.android.lark.sdk.SdkSender", dexClassLoader);
            Method asynSendRequestNonWrapMethod = null;
            for (Method m : sdkSenderCls.getDeclaredMethods()) {
                if (m.getName().equals("asynSendRequestNonWrap") && m.getParameterCount() == 4) {
                    LogUtil.PrintLog("find sdkSender.asynSendRequestNonWrap", "sdkSenderHook");
                    asynSendRequestNonWrapMethod = m;
                    break;
                }
            }
            if (asynSendRequestNonWrapMethod == null) {
                LogUtil.PrintLog("asynSendRequestNonWrapMethod is null", "sdkSenderHook");
                return;
            }
            asynSendRequestNonWrapMethod.invoke(null, objCmd, builderInstance, null, null);
        } catch (Exception e) {
            LogUtil.PrintLog(e.toString(), "RequestBuilderHook");
        }
    }

    public void HookPluginClassLoader(ClassLoader dexClassLoader) {
        try {
            findAndHookMethod("com.bytedance.mira.core.PluginClassLoader", dexClassLoader, "loadClass", String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Class clz = (Class) param.getResult();
                    String clsName = (String) param.args[0];
                    boolean b = (boolean) param.args[1];
                    if (clsName.equals("com.ss.android.lark.plugin.money") || clsName.equals("com.ss.android.lark.money.MoneyModule")) {
                        LogUtil.PrintLog("loadClass className=" + clsName + " boolen " + b, "PluginClassLoader");
                        LogUtil.PrintStackTrace(0);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void HookMira(ClassLoader dexClassLoader) {
//        Class MiraClass = dexClassLoader.loadClass("com.bytedance.mira.Mira");
//        findAndHookMethod(MiraClass,
//                "getPlugin", String.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        String pluginName = (String) param.args[0];
//                        Object plugin = param.getResult();
//                        if (plugin != null) {
//                            String packageDir = (String) XposedHelpers.getObjectField(plugin, "mPackageDir");
//                            // 在这里处理packageDir目录
//                            LogUtil.PrintLog("getPlugin", "param:" + pluginName + " packageDir:" + packageDir);
//                        }
//                    }
//                });
    }

    public void HookStackTrace(ClassLoader dexClassLoader) {
        try {
            XposedHelpers.findAndHookMethod(Throwable.class, "getStackTrace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // 获取原始的StackTraceElement[]
                    StackTraceElement[] stackTrace = (StackTraceElement[]) param.getResult();
                    StackTraceElement[] filteredStackTrace = filterStackTrace(stackTrace);
                    param.setResult(filteredStackTrace);
                }
            });
            XposedHelpers.findAndHookMethod(Thread.class, "getStackTrace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // 获取原始的StackTraceElement[]
                    StackTraceElement[] stackTrace = (StackTraceElement[]) param.getResult();
                    StackTraceElement[] filteredStackTrace = filterStackTrace(stackTrace);
                    param.setResult(filteredStackTrace);
                }
            });
        } catch (Exception e) {
            LogUtil.PrintLog("error occued in HookStackTrace", "HookStackTrace");
        }

    }

    private void initConfigSetting(Context context) {
        try {
            configUtils = ConfigUtils.getInstance();
            configUtils.init(false, null);
        } catch (Exception e) {
            LogUtil.PrintLog("initConfigSetting error:" + e.getMessage(), "initConfigSetting");
        }
    }

}
