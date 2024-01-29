package com.android.larkdemo.Hook;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.larkdemo.Utils.ConfigObject;
import com.android.larkdemo.Utils.ConfigUtils;
import com.android.larkdemo.Utils.HookUtils;
import com.android.larkdemo.Utils.LogUtil;
import com.google.gson.Gson;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.android.larkdemo.Utils.HookUtils.filterStackTrace;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static java.lang.Thread.sleep;

public class HookEntry implements IXposedHookLoadPackage {
    public static String TAG = "Demo";
    private ConfigUtils configUtils;
    private static String finance_sdk_version = null;
    private static DexClassLoader pluginDexClassLoader = null;

    private AtomicBoolean isOpenByModule = new AtomicBoolean(false);

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(HookUtils.XPOSED_HOOK_PACKAGE)) {
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
                    initConfigSetting(null);
                    ConfigObject configObject = configUtils.getConfig(false);
                    if (configObject == null || !configObject.isMoudleEnable) {
                        LogUtil.PrintLog("configObject is null or moudle is disable", TAG);
                        return;
                    }
                    if (configObject.fetchMode) {
                        hookConfigSetting(dexClassLoader);
                        RedpacketMsgHook(dexClassLoader);
                    } else {
                        HookOpenRedpacket(dexClassLoader);
                    }
                    LogUtil.PrintLog("HookEntry handleLoadPackage currentMode " + (configObject.fetchMode ? "直接请求" : "模拟点击"), TAG);
                }
            });

        }
    }

    public void hookConfigSetting(ClassLoader dexClassLoader) {
        Class ttcjPayUtilsCls = findClass("com.android.ttcjpaysdk.ttcjpayapi.TTCJPayUtils", dexClassLoader);
        Object ttcjPayUtilsInstantce = callStaticMethod(ttcjPayUtilsCls, "getInstance");
        finance_sdk_version = (String) callMethod(ttcjPayUtilsInstantce, "getSDKVersion");
        LogUtil.PrintLog("finance_sdk_version = " + finance_sdk_version, TAG);
    }


    public void RedpacketMsgHook(ClassLoader dexClassLoader) {

        Class entityClass = findClass("com.bytedance.lark.pb.basic.v1.Entity", dexClassLoader);
        Class contentClass = findClass("com.bytedance.lark.pb.basic.v1.Content", dexClassLoader);
        XposedHelpers.findAndHookMethod("com.ss.android.lark.parser.internal.p", dexClassLoader, "a", entityClass, contentClass, java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String chatId = (String) param.args[2];
                Object redPacketContent = param.getResult();
                LogUtil.PrintLog("redPacketContent:" + HookUtils.getObjectString(redPacketContent), TAG);
                fetchRedpacketByConfig(redPacketContent, configUtils.getConfig(false), dexClassLoader, chatId);
            }
        });
    }


    public void HookOpenRedpacket(ClassLoader dexClassLoader) {
        Class viewHolderClass = findClass("com.ss.android.lark.chat.chatwindow.chat.message.cell.redpacket.RedPacketMessageCell$RedPacketMessageViewHolder", dexClassLoader);
        Class voClass = findClass("com.ss.android.lark.chat.export.vo.c", dexClassLoader);
        XposedHelpers.findAndHookMethod("com.ss.android.lark.chat.chatwindow.chat.message.cell.redpacket.RedPacketMessageCell", dexClassLoader, "a", viewHolderClass, voClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    Object viewHolder = param.args[0];
                    Object cVar = param.args[1];
                    Class cVarClass = cVar.getClass();
                    Object al = cVarClass.getMethod("al").invoke(cVar);
                    Object redpacketContent = al.getClass().getMethod("E").invoke(al);
                    if (!HookUtils.isAvailableRedPacket(redpacketContent)) {
                        LogUtil.PrintLog("HookOpenRedpacket not available redPacketContent", TAG);
                        return;
                    }
                    View view = (View) viewHolder.getClass().getMethod("a").invoke(viewHolder);
                    if (view == null) {
                        LogUtil.PrintLog("HookOpenRedpacket view is null", TAG);
                        return;
                    }
                    int mSec = 0;
                    ConfigObject configObject = configUtils.getConfig(false);
                    if (configObject.isDelayEnable) {
                        mSec = HookUtils.getRandDelayTime(configObject);
                        LogUtil.PrintLog("delay grab time = " + mSec, TAG);
                    }
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isOpenByModule.get()) {
                                    LogUtil.PrintLog("already running a task,return ", TAG);
                                    return;
                                }
                                view.performClick();
                                isOpenByModule.set(true);
                            } catch (Exception e) {
                                LogUtil.PrintLog("error occued in HookOpenRedpacket" + e.getMessage(), TAG);
                            }
                        }
                    }, mSec);

                } catch (Exception e) {
                    LogUtil.PrintLog("error occued in HookOpenRedpacket" + e.getMessage(), TAG);
                }
            }
        });
        Class redPacketInfoCls = findClass("com.ss.android.lark.money.redpacket.dto.RedPacketInfo", dexClassLoader);
        XposedHelpers.findAndHookMethod("com.ss.android.lark.money.redpacket.detail.RedPacketDetailView", dexClassLoader, "x32_c", redPacketInfoCls, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Object view = param.thisObject;
                    Field mDialogOpenIV = view.getClass().getDeclaredField("mDialogOpenIV");
                    Field mCloseTitleIV = view.getClass().getDeclaredField("mCloseTitleIV");
                    if (mDialogOpenIV == null) {
                        LogUtil.PrintLog("mDialogOpenIV is null", TAG);
                        return;
                    }
                    mCloseTitleIV.setAccessible(true);
                    mDialogOpenIV.setAccessible(true);
                    ImageView imageView = (ImageView) mDialogOpenIV.get(view);
                    ImageView closeTitleIV = (ImageView) mCloseTitleIV.get(view);
                    if (imageView == null) {
                        LogUtil.PrintLog("imageView is null", TAG);
                        return;
                    }
                    imageView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (imageView.getVisibility() != View.VISIBLE) {
                                LogUtil.PrintLog("imageView is not visible", TAG);
                                return;
                            }
                            imageView.performClick();

                            LogUtil.PrintLog("finish a task success", TAG);
                            if (isOpenByModule.get()) {
                                LogUtil.PrintLog("finish a task success", TAG);
                                isOpenByModule.set(false);
                            }
                        }
                    }, 50);
                } catch (NoSuchFieldError noSuchFieldError) {
                    LogUtil.PrintLog("NoSuchFieldError", TAG);
                } catch (ClassCastException classCastException) {
                    LogUtil.PrintLog("ClassCastException", TAG);
                } finally {
                    if (isOpenByModule.get()) {
                        LogUtil.PrintLog("finish a task although failed", TAG);
                        isOpenByModule.set(false);
                    }
                }

            }
        });

    }

    public Method HookSDKSender(ClassLoader dexClassLoader, boolean getMethod) {
        try {
            Class sdkSenderCls = findClass("com.ss.android.lark.sdk.SdkSender", dexClassLoader);
            String methodName = "asynSendRequestNonWrap";
            int paramCount = 4;
            if (sdkSenderCls != null) {
                Method[] methods = sdkSenderCls.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
                        LogUtil.PrintLog("find sdkSender.asynSendRequestNonWrap", "sdkSenderHook");
                        if (getMethod) {
                            return m;
                        }
                        XposedBridge.hookMethod(m, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                Object command = param.args[0];
                                Object msgBuilder = param.args[1];
                                String strCmd = new Gson().toJson(command);
                                String strMsg = new Gson().toJson(msgBuilder);
                                if (strCmd.contains("HONGBAO")) {
                                    LogUtil.PrintLog(methodName + new Gson().toJson(param.args), TAG);
                                    LogUtil.PrintStackTrace(0);
                                }
                            }
                        });
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.PrintLog(e.toString(), "asynSendRequestNonWrap");
        }
        return null;
    }

    public void HookMsg(ClassLoader dexClassLoader) {
        try {
            Class basepageStoreCls = findClass("com.ss.android.lark.chatbase.BasePageStore$1", dexClassLoader);
            Method method = findMethodBestMatch(basepageStoreCls, "add", Object.class);
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws IOException {
                    Gson gson = new Gson();
                    String strMsg = gson.toJson(param.args[0]);
                    LogUtil.PrintLog("addMsg msgCount= " + param.args.length + "msgType = " + param.args[0].getClass().getName() + strMsg, TAG);

                    ConfigObject configObject = configUtils.getConfig(false);
                    if (configObject == null) {
                        LogUtil.PrintLog("HookMsg configObject is null", TAG);
                        return;
                    }
                    LogUtil.PrintLog("configObject = " + new Gson().toJson(configObject), TAG);
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
                                        int delayTimeMin = Math.round(configObject.delayTimeMin * 1000);
                                        int daleyTimeMax = Math.round(configObject.daleyTimeMax * 1000);
                                        int mSec = new Random().nextInt(daleyTimeMax - delayTimeMin + 1) + delayTimeMin;
                                        LogUtil.PrintLog("delay grab time = " + mSec, TAG);
                                        sleep(mSec);
                                        CallGrabRequestBuilder(dexClassLoader, redPacketId, finance_sdk_version, true, 1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        } else {
                            CallGrabRequestBuilder(dexClassLoader, redPacketId, ConfigUtils.finance_sdk_version, ConfigUtils.is_return_name_auth, 1);
                        }

                    }
                }
            });
        } catch (
                Exception e) {
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

    public void CallGrabRequestBuilder(ClassLoader dexClassLoader, String id, String
            finance_sdk_version, boolean is_return_name_auth, int hongbao_type) {
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

            Method asynSendRequestNonWrapMethod = FindSdkSenderOfArgs(dexClassLoader, 4);
            asynSendRequestNonWrapMethod.invoke(null, objCmd, builderInstance, null, null);
        } catch (Exception e) {
            LogUtil.PrintLog("error occued in CallGrabRequestBuilder" + e.getMessage(), TAG);
        }
    }

    public void CallUpdateRequest(ClassLoader dexClassLoader, String chatId, boolean type,
                                  boolean grabbed, boolean grabbed_finish, Boolean is_expired) {
        try {
            Class builderClass = findClass("com.bytedance.lark.pb.im.v1.UpdateHongbaoRequest$Builder", dexClassLoader);
            Class<?> redPacketTypeClass = findClass("com.bytedance.lark.pb.im.v1.HongbaoType", dexClassLoader);
            Class commandCls = findClass("com.bytedance.lark.pb.basic.v1.Command", dexClassLoader);
            Object builderInstance = builderClass.newInstance();
            builderClass.getMethod("id", String.class).invoke(builderInstance, chatId);
            builderClass.getMethod("clicked", boolean.class).invoke(builderInstance, true);
            builderClass.getMethod("grabbed", boolean.class).invoke(builderInstance, grabbed);
            builderClass.getMethod("grabbed_finish", boolean.class).invoke(builderInstance, grabbed_finish);
            builderClass.getMethod("is_expired", boolean.class).invoke(builderInstance, is_expired);
            builderClass.getMethod("hongbao_type", redPacketTypeClass).invoke(builderInstance, callStaticMethod(redPacketTypeClass, "fromValue", 1));

            Object objCmd = callStaticMethod(commandCls, "fromValue", 2402);
            Method asynSendRequestNonWrapMethod = FindSdkSenderOfArgs(dexClassLoader, 4);
            asynSendRequestNonWrapMethod.invoke(null, objCmd, builderInstance, null, null);
        } catch (Exception e) {
            LogUtil.PrintLog("error occued in CallUpdateRequest" + e.getMessage(), TAG);
        }

    }

    public Method FindSdkSenderOfArgs(ClassLoader dexClassLoader, int paramCount) {
        Class sdkSenderCls = findClass("com.ss.android.lark.sdk.SdkSender", dexClassLoader);
        Method asynSendRequestNonWrapMethod = null;
        for (Method m : sdkSenderCls.getDeclaredMethods()) {
            if (m.getName().equals("asynSendRequestNonWrap") && m.getParameterCount() == paramCount) {
                LogUtil.PrintLog("find sdkSender.asynSendRequestNonWrap", "sdkSenderHook");
                asynSendRequestNonWrapMethod = m;
                break;
            }
        }
        return asynSendRequestNonWrapMethod;
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
        try {
            //加载插件的classloader,
            XposedHelpers.findAndHookConstructor("com.bytedance.mira.core.PluginClassLoader", dexClassLoader, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.ClassLoader.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String str = (String) param.args[0];
                    String str2 = (String) param.args[1];
                    String str3 = (String) param.args[2];
                    ClassLoader classLoader = (ClassLoader) param.args[3];
                    LogUtil.PrintLog("PluginClassLoader str=" + str + " str2=" + str2 + " str3=" + str3 + " classLoader=" + classLoader, TAG);

                    pluginDexClassLoader = (DexClassLoader) param.thisObject;
                    HookOpenRedpacketView(pluginDexClassLoader);
                }
            });
        } catch (Exception e) {
            LogUtil.PrintLog("error occued in HookPluginClassLoader" + e.getMessage(), TAG);
        }
    }

    public void HookMira(ClassLoader dexClassLoader) {
        try {
            Class MiraClass = dexClassLoader.loadClass("com.bytedance.mira.Mira");
            findAndHookMethod(MiraClass,
                    "getPlugin", String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String pluginName = (String) param.args[0];
                            Object plugin = param.getResult();
                            if (plugin != null) {
                                String packageDir = (String) XposedHelpers.getObjectField(plugin, "mPackageDir");
                                // 在这里处理packageDir目录
                                LogUtil.PrintLog("getPlugin", "param:" + pluginName + " packageDir:" + packageDir);
                            }
                        }
                    });
        } catch (Exception e) {
            LogUtil.PrintLog("error occued in HookMira" + e.getMessage(), TAG);
        }

    }

    public void HookOpenRedpacketView(ClassLoader dexClassLoader) {
        try {
            Class RedPacketDetailActivityClass = findClass("com.ss.android.lark.money.redpacket.detail.RedPacketDetailActivity", dexClassLoader);
            Method method = null;
            for (Method m : RedPacketDetailActivityClass.getDeclaredMethods()) {
                if (m.getName().equals("x32_a") && m.getReturnType() == Intent.class && m.getModifiers() == Modifier.PUBLIC + Modifier.STATIC) {
                    method = m;
                    LogUtil.PrintLog("find RedPacketDetailActivity.x32_a", "sdkSenderHook");
                    break;
                }
            }
            if (method == null) {
                LogUtil.PrintLog("can not find RedPacketDetailActivity.public static Intent x32_a(Context context, String str, RedPacketInfo redPacketInfo) ", TAG);
                return;
            }
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Intent intent = (Intent) param.getResult();
                    Context context = (Context) param.args[0];
                    String str = (String) param.args[1];
                    Object obj = param.args[2];
                    LogUtil.PrintLog("x32_a intent:" + " context:" + context.toString() + " str:" + str + " obj:" + new Gson().toJson(obj), TAG);
                }
            });
        } catch (Exception e) {
            LogUtil.PrintLog("error occued in HookOpenRedpacketView" + e.getMessage(), TAG);
        }
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

    public boolean fetchRedpacketByConfig(Object redPacketContent, ConfigObject
            configObject, ClassLoader dexClassLoader, String chatId) {
        try {

            Field redPacketId = redPacketContent.getClass().getDeclaredField("redPacketId");
            redPacketId.setAccessible(true);

            Field type = redPacketContent.getClass().getDeclaredField("type");
            type.setAccessible(true);

            if (!HookUtils.canFetch(redPacketContent, configObject)) {
                LogUtil.PrintLog("can not fetch redPacketContent", TAG);
                return false;
            }
            String redPacketIdStr = (String) redPacketId.get(redPacketContent);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!configObject.isDelayEnable) {
                            CallGrabRequestBuilder(dexClassLoader, redPacketIdStr, finance_sdk_version, true, 1);
                            sleep(500);
                            CallUpdateRequest(dexClassLoader, chatId, true, true, true, false);
                            return;
                        }
                        int mSec = HookUtils.getRandDelayTime(configObject);
                        LogUtil.PrintLog("delay grab time = " + mSec, TAG);
                        sleep(mSec);
                        CallGrabRequestBuilder(dexClassLoader, redPacketIdStr, finance_sdk_version, true, 1);
                        sleep(500);
                        CallUpdateRequest(dexClassLoader, chatId, true, true, true, false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            return true;

        } catch (IllegalAccessException e) {
            LogUtil.PrintLog("IllegalAccessException", TAG);
        } catch (NoSuchFieldException e) {
            LogUtil.PrintLog("NoSuchFieldException", TAG);
        }
        return true;
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
