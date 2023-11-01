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
import java.lang.reflect.Method;

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

                    findAndHookMethod("android.view.View", dexClassLoader, "performClick", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object mListenerInfo = XposedHelpers.getObjectField(param.thisObject, "mListenerInfo");
                            if (mListenerInfo != null) {
                                Object mOnClickListener = XposedHelpers.getObjectField(mListenerInfo, "mOnClickListener");
                                if (mOnClickListener != null) {
                                    String callbackClassName = mOnClickListener.getClass().getName();
                                    LogUtil.PrintLog("Callback class name: " + callbackClassName, "ImageViewClickHook");
                                    Log.i(TAG, "beforeHookedMethod: " + callbackClassName);
                                }
                            }
                        }
                    });
                 /*   findAndHookConstructor("com.ss.android.lark.money.redpacket.detail.RedpacketDetailView", dexClassLoader, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            LogUtil.PrintStackTrace(5);
                        }
                    });*/
                    try {
                        findAndHookConstructor("android.view.View", dexClassLoader, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                View view = (View) param.thisObject;
                                String className = view.getClass().getName();
                                if (!className.contains("RedpacketDetailView")) {
                                    return;
                                }
                                LogUtil.PrintLog("find RedpacketDetailView", "*********************************************");
                                strMessage = new Gson().toJson(param.args);
                                LogUtil.PrintLog(strMessage, "view的参数");
                                LogUtil.PrintStackTrace(5);
                            }
                        });
                    } catch (XposedHelpers.ClassNotFoundError error) {
                        LogUtil.PrintLog(error.toString(), "RedpacketDetailView");
                    } finally {

                    }
                    findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            String activityName = activity.getClass().getName();
                            LogUtil.PrintLog("onCreate hook success" + activityName, "Activity");
                            if (activityName.contains("RedpacketDetailActivity")) {
                                Bundle extras = (Bundle) param.args[0];
                                if (extras != null) {
                                    for (String key : extras.keySet()) {
                                        Object value = extras.get(key);
                                        LogUtil.PrintLog(value.toString(), "Bundle");
                                    }
                                }
                            }
                        }
                    });
                }

            });
        }
    }
}
