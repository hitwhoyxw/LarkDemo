package com.android.larkdemo;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtil {
    private static final String TAG="LogUtil";
    private static final String fileName="LarkDemo_Log.txt";
    private static final String dirName="LarkDemo";
    private static File file=null;
    private static FileWriter fileWriter=null;
    public static void Write(Context context,String log){
        try{
            File logDir=new File(Environment.getExternalStorageDirectory(),dirName);
            if (!logDir.exists()){
                logDir.mkdir();
            }
            if (file==null){
                file=new File(logDir,fileName);
            }
            if (fileWriter==null){
                fileWriter=new FileWriter(file,true);
            }
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            fileWriter.write(timeStamp+log+"\n");
            fileWriter.flush();
        } catch (IOException e) {

        }
    }
}
