package com.android.larkdemo;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;

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
                    XposedHelpers.findAndHookMethod(DATABASENAME,
                            dexClassLoader, "insert", String.class, String.class,
                            ContentValues.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    // 在插入数据之前进行操作
                                    String table = (String) param.args[0]; // 数据表名
                                    ContentValues values = (ContentValues) param.args[2]; // 数据内容
                                    XposedBridge.log(TAG+ "Inserting data into table " + table + ": " + values);
                                }
                            });
                    XposedHelpers.findAndHookMethod(DATABASENAME, dexClassLoader, "query", String.class, String[].class, String.class, String[].class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            String table = (String) param.args[0];
                            String[] columns = (String[]) param.args[1];
                            String selection = (String) param.args[2];
                            String[] selectionArgs = (String[]) param.args[3];
                            String groupBy = (String) param.args[4];
                            String having = (String) param.args[5];
                            String orderBy = (String) param.args[6];
                            String limit = (String) param.args[7];
                            XposedBridge.log(TAG+"Query data begin");
                            XposedBridge.log(TAG+"Query data from table"+table+"columns"+columns);
                            XposedBridge.log(TAG+"Query data from table"+table+"selection:"+selection);
                            XposedBridge.log(TAG+"Query data from table"+table+"selectionArgs:"+selectionArgs);
                            XposedBridge.log(TAG+"Query data end");
                        }
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                        }
                    });

                    XposedHelpers.findAndHookMethod(DATABASENAME,dexClassLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // 获取查询参数
                            String sql = (String) param.args[0];
                            String[] selectionArgs = (String[]) param.args[1];
                            // 输出查询参数
                            XposedBridge.log(TAG+"rawQuery data begin");
                            XposedBridge.log(TAG+"rawQuery sql: " + sql);
                            XposedBridge.log(TAG+ "rawQuery selectionArgs: " + selectionArgs);
                            XposedBridge.log(TAG+"rawQuery data end");
                        }
                    });
                }
            });

        }
    }
}
