package com.android.larkdemo;
/*import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.Cursor;
import net.sqlcipher.CursorWrapper;*/

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
                                }
                            }
                        }
                    });
                    //通过反射注册的实现，第二个参数是对应的实现类名，通过堆栈区分是不是要找的。
                    findAndHookConstructor("com.ss.android.lark.mira.e$a$d", dexClassLoader, java.lang.Class.class, java.lang.String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String className = (String) param.args[1];
                            LogUtil.PrintLog(className, "aim class");
                            LogUtil.PrintStackTrace(10);
                        }
                    });
                }

            });
        }
    }
}
