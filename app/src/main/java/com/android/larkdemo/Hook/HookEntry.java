package com.android.larkdemo.Hook;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.larkdemo.Utils.HookUtils;
import com.android.larkdemo.Utils.LogUtil;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class HookEntry implements IXposedHookLoadPackage {
    public static String TAG = "Demo";
    public static ClassLoader dexClassLoader = null;
    public static ClassLoader classLoader = null;
    private static String mDatabasePath = "";
    private static String mDatabasePassword = "";
    private static int isDatabaseOpened = -1;
    private String strMessage;

    private Boolean isFirst = true;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(HookUtils.XPOSED_HOOK_PACKAGE)) {
            classLoader = loadPackageParam.classLoader;
            XposedBridge.log(TAG + " has Hooked!");
            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    dexClassLoader = context.getClassLoader();
                    if (dexClassLoader == null) {
                        XposedBridge.log("cannot get classloader return ");
                        return;
                    }
                    try {
                        Class storeCls = findClass("com.ss.android.lark.chatbase.BasePageStore$1", dexClassLoader);
                        Class aClass = findClass("com.ss.android.lark.chat.entity.message.a", dexClassLoader);
                        XposedHelpers.findAndHookMethod(storeCls, "add", aClass, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                Object objA = param.args[0];
                                strMessage = new Gson().toJson(param.args);
                                LogUtil.PrintLog(strMessage, "addMsg");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        XposedHelpers.findAndHookMethod("com.bytedance.mira.plugin.b", dexClassLoader, "a", java.io.File.class, java.lang.String.class, int.class, java.lang.String.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                File file = (File) param.args[0];
                                if (!file.getAbsolutePath().contains("money")) {
                                    return;
                                }
                                String str = (String) param.args[1];
                                int i = (int) param.args[2];
                                String str2 = (String) param.args[3];
                                LogUtil.PrintLog("plugin file path " + file.getAbsolutePath() + " plugin name " + str + " plugin version" + i + " class name" + str2, "com.bytedance.mira.plugin.b.a");
                                Context context = (Context) XposedHelpers.callMethod(file, "getApplicationContext");
                                HookUtils.CopyFile(file, context, "myplugin");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //hook sdksender
                    try {
                        //device_info{"finance_sdk_version":"6.8.8,"hongbao_type":"NORMAL","id":"xxx","is_return_name_auth":"true"}
                        //Command.GRAB_HONGBAO=2401
                        Class sdkSenderCls = findClass("com.ss.android.lark.sdk.SdkSender", dexClassLoader);
                        String methodName = "asynSendRequestNonWrap";
                        int paramCount = 4;
                        if (sdkSenderCls != null) {
                            Method[] methods = sdkSenderCls.getDeclaredMethods();
                            for (Method m : methods) {
                                if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
                                    LogUtil.PrintLog("find sdkSender.asynSendRequestNonWrap", "sdkSenderHook");

                                    XposedBridge.hookMethod(m, new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            Object command = param.args[0];
                                            Object msgBuilder = param.args[1];
                                            String strCmd = new Gson().toJson(command);
                                            LogUtil.PrintLog("command = " + strCmd, methodName);
                                            LogUtil.PrintLog("msgBuilder = " + new Gson().toJson(msgBuilder), methodName);
                                            if (strCmd.contains("HONGBAO")) {
                                                LogUtil.PrintStackTrace(0);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.PrintLog(e.toString(), "asynSendRequestNonWrap");
                    }
                    //Class MiraClass=dexClassLoader.loadClass("com.bytedance.mira.Mira");
//                        findAndHookMethod(MiraClass,
//                                "getPlugin", String.class, new XC_MethodHook() {
//                                    @Override
//                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                        String pluginName = (String) param.args[0];
//                                        Object plugin = param.getResult();
//                                        if (plugin != null) {
//                                            String packageDir = (String) XposedHelpers.getObjectField(plugin, "mPackageDir");
//                                            // 在这里处理packageDir目录
//                                            LogUtil.PrintLog("getPlugin","param:"+pluginName+" packageDir:"+packageDir);
//                                        }
//                                    }
//                                });
//                    try{
//                        findAndHookMethod("com.bytedance.mira.core.PluginClassLoader", dexClassLoader, "loadClass", String.class, boolean.class, new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                Class clz=(Class) param.getResult();
//                                String clsName=(String) param.args[0];
//                                boolean b=(boolean) param.args[1];
//                                if (clsName.equals("com.ss.android.lark.plugin.money")||clsName.equals("com.ss.android.lark.money.MoneyModule")){
//                                    LogUtil.PrintLog("loadClass className="+clsName+" boolen "+b,"PluginClassLoader");
//                                    LogUtil.PrintStackTrace(0);
//                                }
//                            }
//                        });
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }

//                    try {
//                        Class clzRedPacketMsgCell = XposedHelpers.findClass("com.ss.android.lark.chat.chatwindow.chat.message.cell.redpacket.RedPacketMessageCell", dexClassLoader);
//                        if (clzRedPacketMsgCell != null) {
//                            LogUtil.PrintLog("find RedPacketMessageCell!!!", "RedPacketMessageCell");
//                            Method targetMethod = null;
//                            for (Method m : clzRedPacketMsgCell.getDeclaredMethods()) {
//                                Boolean choise = m.getName().equals("a") && m.getParameterCount() == 3 && m.getParameterTypes()[0] == View.class && m.getParameterTypes()[1] == int.class && Modifier.isPublic(m.getModifiers());
//                                if (choise) {
//                                    LogUtil.PrintLog("find RedPacketMessageCell.a(View view, int i, com.ss.android.lark.chat.export.vo.c<com.ss.android.lark.chat.chatwindow.chat.view.message.plugin.redpackage.entity.a> cVar)!!!", "RedPacketMessageCell");
//                                    //通过限定找到了对应的a方法
//                                    XposedBridge.hookMethod(m, new XC_MethodHook() {
//                                        @Override
//                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                            View view = (View) param.args[0];
//                                            int i = (int) param.args[1];
//                                            LogUtil.PrintLog("args1 int = " + i, "RedPacketMessageCell");
//
//                                            // 获取 ListenerInfo 对象
//                                            Field listenerInfoField = View.class.getDeclaredField("mListenerInfo");
//                                            listenerInfoField.setAccessible(true);
//                                            Object listenerInfo = listenerInfoField.get(view);
//
//                                            // 判断 ListenerInfo 对象是否为 null
//                                            if (listenerInfo != null) {
//                                                // 获取 OnClickListener 对象
//                                                Class<?> listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
//                                                Field onClickListenerField = listenerInfoClass.getDeclaredField("mOnClickListener");
//                                                onClickListenerField.setAccessible(true);
//                                                Object onClickListener = onClickListenerField.get(listenerInfo);
//
//                                                if (onClickListener instanceof View.OnClickListener) {
//                                                    // 调用 OnClickListener 的 onClick 方法
//                                                    Method onClickMethod = View.OnClickListener.class.getDeclaredMethod("onClick", View.class);
//                                                    onClickMethod.setAccessible(true);
//                                                    if (isFirst){
//                                                        onClickMethod.invoke(onClickListener, view);
//                                                        LogUtil.PrintLog("call onclick success", "RedPacketMessageCell");
//                                                        isFirst=false;
//                                                    }
//
//                                                }
//                                            }
//                                        }
//                                    });
//                                }
//
//                            }
//                        }
////                        try {
////                            Class Bclz = findClass("com.ss.android.lark.b.class", dexClassLoader);
////                            Class Sclz = findClass("com.ss.android.lark.dependency.s.class", dexClassLoader);
////                            XposedHelpers.findAndHookMethod("com.ss.android.lark.d", dexClassLoader, "a", Bclz, Sclz, new XC_MethodHook() {
////                                @Override
////                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
////                                    LogUtil.PrintStackTrace(0);
////                                }
////                            });
////
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
//                    } catch (Exception e) {
//                        LogUtil.PrintLog(e.toString(), "RedPacketMessageCell");
//                    }


                }


            });
        }
    }
}
