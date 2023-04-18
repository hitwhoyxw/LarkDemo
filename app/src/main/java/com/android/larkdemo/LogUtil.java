package com.android.larkdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.XposedBridge;

public class LogUtil {
    private static final String TAG = "LogUtil";
    private static final String fileName = "Demo_Log.txt";
    private static final String dirName = "Demo";
    private static File file = null;
    private static FileWriter fileWriter = null;

    public static void Write(Context context, String log) {
        try {
            File logDir = new File(Environment.getExternalStorageDirectory(), dirName);
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            if (file == null) {
                file = new File(logDir, fileName);
            }
            if (fileWriter == null) {
                fileWriter = new FileWriter(file, true);
            }
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            fileWriter.write(timeStamp + log + "\n");
            fileWriter.flush();
        } catch (IOException e) {

        }
    }

    public static void PrintLog(String msg, String tag) {
        XposedBridge.log("");
        XposedBridge.log("-------------------------" + tag + "----------------------------------");
        XposedBridge.log(msg);
        XposedBridge.log("-------------------------" + tag + "----------------------------------");
    }

    public static void PrintInsert(String table, ContentValues contentValues, String tag) {
        XposedBridge.log("");
        XposedBridge.log("-------------------------" + tag + "----------------------------------");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("table:");
        stringBuilder.append(table);
        stringBuilder.append(contentValues.toString());
        XposedBridge.log(stringBuilder.toString());
        XposedBridge.log("-------------------------" + tag + "----------------------------------");
    }

    public static void PrintStackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int count = 0;
        XposedBridge.log(TAG + "-------------------------stackTrace----------------------------------");
        for (int i = 0; i < stackTraceElements.length && count < 5; i++) {
            String className = stackTraceElements[i].getClassName();
            if (!className.startsWith("de.robv.android.xposed") && !className.startsWith("com.android.larkdemo")) {
                XposedBridge.log(TAG + stackTraceElements[i].toString());
                count++;
            }
        }
        XposedBridge.log(TAG + "-------------------------stackTrace----------------------------------");
    }

    public static void PrintDatabaseQuery(Cursor cursor, String tag) {
        XposedBridge.log("");
        XposedBridge.log("-------------------------query " + tag + " data----------------------------------");
        PrintDatabaseQuery(cursor);
        XposedBridge.log("-------------------------query " + tag + " data----------------------------------");
    }

    public static void PrintDatabaseQuery(Cursor cursor) {
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getCount();
                for (int i = 0; i < count; i++) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 0; j < cursor.getColumnCount(); j++) {
                        stringBuilder.append(cursor.getColumnName(j)).append(": ");
                        boolean isBlob = cursor.getType(j) == Cursor.FIELD_TYPE_BLOB;
                        if (isBlob) {
                            stringBuilder.append(new String(cursor.getBlob(j))).append(", ");
                        }
                    }
                    XposedBridge.log("");
                    XposedBridge.log(TAG + stringBuilder.toString());
                    cursor.moveToNext();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
        }
    }
}
