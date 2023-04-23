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
import android.util.Log;

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
                    XposedHelpers.findAndHookMethod(SQLiteDatabase.class, "loadLibs", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedHelpers.callStaticMethod(XposedHelpers.findClass("net.sqlcipher.database.SQLiteDatabase", dexClassLoader), "loadLibs", XposedHelpers.findClass("android.content.Context", dexClassLoader));
                        }
                    });

                    final Class<?> sqliteDatabase = XposedHelpers.findClass("net.sqlcipher.database.SQLiteDatabase", dexClassLoader);
                    final Method insertWithOnConflictMethod = XposedHelpers.findMethodExact(sqliteDatabase, "insertWithOnConflict", String.class, String.class, XposedHelpers.findClass("android.content.ContentValues", dexClassLoader), int.class);

                    XC_MethodHook methodHook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String table = (String) param.args[0];
                            ContentValues contentValues = (ContentValues) param.args[2];
                            if (table != null && table.toLowerCase().startsWith("insert into")) {
                                LogUtil.PrintInsert(table, contentValues, "sqlcipher insert");
                            }
                        }
                    };
                    // Hook insertWithOnConflict 方法
                    XposedBridge.hookMethod(insertWithOnConflictMethod, methodHook);

                    XC_MethodHook afterMethodHook = new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        }
                    };
                    // Hook其他执行SQL语句的方法
                 /*   XposedHelpers.findAndHookMethod(sqliteDatabase, "update", String.class, XposedHelpers.findClass("android.content.ContentValues", dexClassLoader), String.class, String[].class, afterMethodHook);
                    XposedHelpers.findAndHookMethod(sqliteDatabase, "delete", String.class, String.class, String[].class, afterMethodHook);*/

                }
            });
        }
    }
}
