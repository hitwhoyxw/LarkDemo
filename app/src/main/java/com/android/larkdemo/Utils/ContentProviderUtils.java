package com.android.larkdemo.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;
import com.crossbowffs.remotepreferences.RemotePreferences;


///在非创建的进程中使用此sharedPreferences
public class ContentProviderUtils extends RemotePreferenceProvider {

    public static String authority = "com.android.larkdemo.Utils.ContentProviderUtils";
    public static String[] prefFileNames = {"module_config", "my_config"};
    public static String password = "123456";
    public static int MODULE_CONFIG = 0;
    public static int MY_CONFIG = 1;

    public static SharedPreferences module_sharedPreferences;
    public static SharedPreferences my_sharedPreferences;

    public static SharedPreferences getSharedPreferences(Context context, int index) {
        if (index == MODULE_CONFIG) {
            if (module_sharedPreferences == null) {
                module_sharedPreferences = new RemotePreferences(context, authority, prefFileNames[index]);
            }
            return module_sharedPreferences;
        } else if (index == MY_CONFIG) {
            if (my_sharedPreferences == null) {
                my_sharedPreferences = new RemotePreferences(context, authority, prefFileNames[index]);
            }
            return my_sharedPreferences;
        }
        return null;
    }

    public ContentProviderUtils() {
        super("com.android.larkdemo.Utils.ContentProviderUtils", new String[]{"module_config", "my_config"});
    }


    @Override
    protected boolean checkAccess(String prefFileName, String prefKey, boolean write) {
        String callingPackage = getCallingPackage();
        boolean b1 = "com.android.larkdemo".equals(callingPackage);
        boolean b2 = "com.android.ss.lark".equals(callingPackage);
        if (!b1 && !b2) {
            return false;
        }
        return true;
    }


}
