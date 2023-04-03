package com.android.larkdemo;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {
    public static String PACKAGENAME="com.ss.android.lark";
    public static String DATABASENAME="android.database.sqlite.SQLiteDatabase";
    public  static String TAG="LarkHookDemo";
    public static ClassLoader dexClassLoader=null;
    public static ClassLoader classLoader=null;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(PACKAGENAME)) {
            XposedBridge.log(TAG+" has Hooked!");
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class,new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context=(Context) param.args[0];
                    //拿到真正的类加载器
                    dexClassLoader=context.getClassLoader();
                    if (dexClassLoader==null){
                        XposedBridge.log("cannot get classloader return ");
                        return ;
                    }
                    /*
                    XposedHelpers.findAndHookMethod(DATABASENAME,
                            dexClassLoader, "insertWithOnConflict", String.class, String.class,
                            ContentValues.class,int.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    // 在插入数据之前进行操作
                                    //检测到收到消息后是插入eventv3表，tea_event_index=10352|10353,显示为native网络请求，怀疑消息不在这里插入
                                    String table = (String) param.args[0]; // 数据表名
                                    ContentValues values = (ContentValues) param.args[2]; // 数据内容
                                    XposedBridge.log(TAG+ "Inserting data into table " + table + ": " + values);
                                }
                            });

                     */
                    /*
                    XposedHelpers.findAndHookMethod(DATABASENAME, dexClassLoader, "query", String.class, String[].class, String.class, String[].class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            LogUtil.PrintStackTrace();
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Cursor cursor = (Cursor) param.getResult();
                            LogUtil.PrintDatabaseQuery(cursor);
                        }
                    });

                    //msgtype 6可疑
                    //type =traffic_packets
                    XposedHelpers.findAndHookMethod(DATABASENAME, dexClassLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            LogUtil.PrintStackTrace();
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Cursor cursor = (Cursor) param.getResult();
                            LogUtil.PrintDatabaseQuery(cursor);
                        }
                    });

                     */
                    /*
                    XposedHelpers.findAndHookMethod(DATABASENAME, dexClassLoader, "openOrCreateDatabase", String.class, byte[].class,SQLiteDatabase.CursorFactory.class, DatabaseErrorHandler.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String password=(String) param.args[3];
                            XposedBridge.log("password:"+password);
                        }
                    });
                    XposedHelpers.findAndHookMethod(DATABASENAME, dexClassLoader, "openOrCreateDatabase", File.class, SQLiteDatabase.CursorFactory.class, DatabaseErrorHandler.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String password=(String) param.args[3];
                            XposedBridge.log("password:"+password);
                        }
                    });

                    //net.sqlcipher.database.SQLiteOpenHelper里面getWritableDatabase带了参数 一个是char[],一个是byte[],应该是密码，然后调用的sqlite的openOrCreateDatabase
                     */
                    XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteOpenHelper", dexClassLoader, "getWritableDatabase", char[].class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            char[] password = (char[]) param.args[0];
                            XposedBridge.log("password char array:" + password);
                        }
                    });
                    XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteOpenHelper", dexClassLoader, "getWritableDatabase", byte[].class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            byte[] password = (byte[]) param.args[0];
                            XposedBridge.log("password byte array:" + password);
                        }
                    });
                }
            });

        }
    }
}
