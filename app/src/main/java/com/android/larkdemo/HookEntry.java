package com.android.larkdemo;


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

import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
                    XposedBridge.hookAllMethods(XposedHelpers.findClass("com.ss.android.lark.chatbase.BasePageStore$1", dexClassLoader), "add", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            strMessage = new Gson().toJson(param.args);
                            LogUtil.PrintLog(strMessage, "addMsg");
                        }
                    });
//                    findAndHookMethod("android.view.View", dexClassLoader, "performClick", new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            Object mListenerInfo = XposedHelpers.getObjectField(param.thisObject, "mListenerInfo");
//                            if (mListenerInfo != null) {
//                                Object mOnClickListener = XposedHelpers.getObjectField(mListenerInfo, "mOnClickListener");
//                                if (mOnClickListener != null) {
//                                    String callbackClassName = mOnClickListener.getClass().getName();
//                                    LogUtil.PrintLog("Callback class name: " + callbackClassName, "ImageViewClickHook");
//                                    Log.i(TAG, "beforeHookedMethod: " + callbackClassName);
//                                }
//                            }
//                        }
//                    });
                    /*findAndHookMethod(DexFile.class, "loadDex", String.class, String.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String dex = (String) param.args[0];
                            String odex = (String) param.args[1];
                            LogUtil.PrintLog("dex " + dex + "odex " + odex, "hook dexload");
                        }
                    });*/

                    try {
                        Class clzRedPacketMsgCell = XposedHelpers.findClass("com.ss.android.lark.chat.chatwindow.chat.message.cell.redpacket.RedPacketMessageCell", dexClassLoader);
                        if (clzRedPacketMsgCell != null) {
                            LogUtil.PrintLog("find RedPacketMessageCell!!!", "RedPacketMessageCell");
                            Method targetMethod = null;
                            for (Method m : clzRedPacketMsgCell.getDeclaredMethods()) {
                                Boolean choise = m.getName().equals("a") && m.getParameterCount() == 3 && m.getParameterTypes()[0] == View.class && m.getParameterTypes()[1] == int.class && Modifier.isPublic(m.getModifiers());
                                if (choise) {
                                    LogUtil.PrintLog("find RedPacketMessageCell.a(View view, int i, com.ss.android.lark.chat.export.vo.c<com.ss.android.lark.chat.chatwindow.chat.view.message.plugin.redpackage.entity.a> cVar)!!!", "RedPacketMessageCell");
                                    //通过限定找到了对应的a方法
                                    XposedBridge.hookMethod(m, new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                            View view = (View) param.args[0];
                                            int i = (int) param.args[1];
                                            LogUtil.PrintLog("args1 int = " + i, "RedPacketMessageCell");

                                            // 获取 ListenerInfo 对象
                                            Field listenerInfoField = View.class.getDeclaredField("mListenerInfo");
                                            listenerInfoField.setAccessible(true);
                                            Object listenerInfo = listenerInfoField.get(view);

                                            // 判断 ListenerInfo 对象是否为 null
                                            if (listenerInfo != null) {
                                                // 获取 OnClickListener 对象
                                                Class<?> listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
                                                Field onClickListenerField = listenerInfoClass.getDeclaredField("mOnClickListener");
                                                onClickListenerField.setAccessible(true);
                                                Object onClickListener = onClickListenerField.get(listenerInfo);

                                                if (onClickListener instanceof View.OnClickListener) {
                                                    // 调用 OnClickListener 的 onClick 方法
                                                    Method onClickMethod = View.OnClickListener.class.getDeclaredMethod("onClick", View.class);
                                                    onClickMethod.setAccessible(true);
                                                    //onClickMethod.invoke(onClickListener, view);
                                                    LogUtil.PrintLog("call onclick success", "RedPacketMessageCell");
                                                }
                                            }
                                        }
                                    });
                                }

                            }
                        }
                        try {
                            Class Bclz = findClass("com.ss.android.lark.b.class", dexClassLoader);
                            Class Sclz = findClass("com.ss.android.lark.dependency.s.class", dexClassLoader);
                            XposedHelpers.findAndHookMethod("com.ss.android.lark.d", dexClassLoader, "a", Bclz, Sclz, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    LogUtil.PrintStackTrace(0);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        LogUtil.PrintLog(e.toString(), "RedPacketMessageCell");
                    }


                }

            });
        }
    }
}
