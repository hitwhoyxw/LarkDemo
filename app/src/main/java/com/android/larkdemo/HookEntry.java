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

public class HookEntry implements IXposedHookLoadPackage {
    public static String TAG = "Demo";
    public static ClassLoader dexClassLoader = null;
    public static ClassLoader classLoader = null;
    private static String mDatabasePath = "";
    private static String mDatabasePassword = "";
    private static int isDatabaseOpened = -1;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(HookUtils.XPOSED_HOOK_PACKAGE)) {
            classLoader = loadPackageParam.classLoader;
            XposedBridge.log(TAG + " has Hooked!");
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    dexClassLoader = context.getClassLoader();
                    if (dexClassLoader == null) {
                        XposedBridge.log("cannot get classloader return ");
                        return;
                    }
                   /* XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", dexClassLoader, "insertWithOnConflict", java.lang.String.class, java.lang.String.class, android.content.ContentValues.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String table=(String)param.args[0];
                            ContentValues contentValues=(ContentValues)param.args[2];
                            LogUtil.PrintInsert(table,contentValues,"sqlcipher1");
                        }
                    });
                    XposedHelpers.findAndHookConstructor("RedPacketMessageCell$a$a", dexClassLoader, boolean.class, java.lang.String.class,new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("RedPacketMessageCell$a$a,useable="+(boolean)param.args[0]+",status="+(String) param.args[1]);
                        }
                    });*/
                    /*XposedHelpers.findAndHookMethod("com.ss.android.lark.integrator.im.chat.core.dependency.v", classLoader, "a", android.app.Activity.class, java.lang.String.class, java.lang.String.class, boolean.class, boolean.class, boolean.class, java.lang.String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                        }
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                        }
                    });*/
                    XposedHelpers.findAndHookMethod("com.ss.android.lark.chatwindow.ChatWindowActivity", dexClassLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("ChatWindowActivity has been hooked");
                        }
                    });
                    /*XposedBridge.hookAllMethods(XposedHelpers.findClass("com.ss.android.lark.chatbase.BasePageStore$1", dexClassLoader), "add", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            LogUtil.PrintLog(new Gson().toJson(param.args), "addMsg");
                        }
                    });*/
                    XposedHelpers.findAndHookMethod("com.ss.android.lark.chatbase.BasePageStore$1", dexClassLoader, "add", Object.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    super.beforeHookedMethod(param);
                                    LogUtil.PrintLog(new Gson().toJson(param.args), "addMsg");
                                    Log.e("addMsg", new Gson().toJson(param.args));
                                }
                            }
                    );

                    //钉钉调用的这个插入，数据结构都出来了

                    /*XposedHelpers.findAndHookMethod("com.alibaba.bee.DatabaseUtils", classLoader, "getInsertWithOnConflict", java.lang.Class<? extends com.alibaba.bee.impl.table.TableEntry>.class, java.lang.String.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                        }
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                        }
                    });*/
                }
            });
        }
    }
}
