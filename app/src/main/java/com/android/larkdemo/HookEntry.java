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
    private static boolean isDatabaseOpened = false;

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

                    /*Class<?> cls = XposedHelpers.findClass("android.database.sqlite.SQLiteDatabase",dexClassLoader);
                    XposedHelpers.findAndHookMethod(cls, "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            ContentValues contentValues=(ContentValues)param.args[2];
                            String tableName=(String) param.args[0];
                            LogUtil.PrintInsert(tableName,contentValues,"sqlite");
                        }
                    });*/
                }
            });
            XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", dexClassLoader, "insertWithOnConflict", java.lang.String.class, java.lang.String.class, android.content.ContentValues.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ContentValues contentValues = (ContentValues) param.args[2];
                    String tableName = (String) param.args[0];
                    LogUtil.PrintInsert(tableName, contentValues, "sqlcipher insertWithOnConflict");
                }
            });
            Class<?> sqlCipherClass = XposedHelpers.findClass("net.sqlcipher.database.SQLiteDatabase", dexClassLoader);
            XposedBridge.hookAllConstructors(sqlCipherClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // 获取数据库路径和密码
                    String path = (String) param.args[0];
                    String password = (String) param.args[1];
                    // 保存到全局变量中
                    mDatabasePath = path;
                    mDatabasePassword = password;
                    XposedBridge.log("sqlCipherClass hookAllConstructors");
                    XposedBridge.log("mDatabasePath:" + mDatabasePath + "mDatabasePassword:" + mDatabasePassword);
                }
            });
            XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", dexClassLoader, "isOpen", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    isDatabaseOpened = (boolean) param.getResult();
                    XposedBridge.log("isDatabaseOpened:" + isDatabaseOpened);
                }
            });
        }

    }
}
