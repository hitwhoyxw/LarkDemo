package com.android.larkdemo;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {
    public static String TAG = "LarkHookDemo";
    public static byte[] bytes;
    public static ClassLoader dexClassLoader = null;
    public static ClassLoader classLoader=null;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(HookUtils.XPOSED_HOOK_PACKAGE)) {
            XposedBridge.log(TAG + " has Hooked!");
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    //拿到真正的类加载器
                    dexClassLoader = context.getClassLoader();
                    if (dexClassLoader == null) {
                        XposedBridge.log("cannot get classloader return ");
                        return;
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

                    /*XposedHelpers.findAndHookMethod(DATABASENAME, dexClassLoader, "query", String.class, String[].class, String.class, String[].class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
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
*/                    //msgtype 6可疑
                    //type =traffic_packets
                    /*XposedHelpers.findAndHookMethod(DATABASENAME, dexClassLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                            try{

                            }
                            Cursor cursor=
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        }
                    });*/
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
                    //hook sqliteopenhelper的getwritabledatabase，获得参数中的数据库密码
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

                    //获得数据库 打印自定义查询
                    XposedHelpers.findAndHookMethod(HookUtils.SQLCIPHER_OPENHELPER, dexClassLoader, "getWritableDatabase", byte[].class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            Class<?> SqlCipherClass = Class.forName(HookUtils.SQLCIPER_DATABASE);
                            Object db = param.getResult();
                            if (db != null && db.getClass().getName().equals(HookUtils.SQLCIPER_DATABASE)) {

                                Object cipherDB = ((Class<?>) SqlCipherClass).cast(db);
                                Method query = SqlCipherClass.getMethod("rawQuery", String.class, String[].class);
                                String sql = "SELECT name FROM sqlite_master WHERE type='table';";
                                Cursor cursor = (Cursor) query.invoke(cipherDB, sql, null, null);
                                LogUtil.PrintDatabaseQuery(cursor, "TABLE");


                            }
                        }
                    });
                }
            });
        }
    }
}
