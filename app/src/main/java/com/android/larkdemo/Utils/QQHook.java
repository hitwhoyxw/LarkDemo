package com.android.larkdemo.Utils;

import android.util.Log;

import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class QQHook {

    public static QQHook instance;

    private static class QQHookHolder {
        private static final QQHook instance = new QQHook();
    }

    public static QQHook getInstance() {
        return QQHookHolder.instance;
    }

    public void hookSendGrabHbRequest(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("com.tenpay.sdk.activity.NetBaseActivity", classLoader, "httpRequestInner", java.lang.String.class, Map.class, boolean.class, boolean.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String baseUrl = (String) param.args[0];
                Map<String, String> paramMap = (Map<String, String>) param.args[1];
                boolean isEncrypt = (boolean) param.args[2];
                boolean isShowLoading = (boolean) param.args[3];
                LogUtil.PrintLog("baseUrl:" + baseUrl, "hookSendGrabHbRequest");
            }
        });
        Class CgiBizDataCls = XposedHelpers.findClass("com.tenpay.sdk.net.http.request.CgiBizData", classLoader);
        Class CallbackThreadEnumClass = XposedHelpers.findClass("com.tenpay.sdk.net.callback.CallbackThreadEnum", classLoader);
        Class NetCallbackClass = XposedHelpers.findClass("com.tenpay.sdk.net.core.callback.NetCallback", classLoader);
        XposedHelpers.findAndHookMethod("com.tenpay.sdk.net.http.PayCgiService", classLoader, "sendRequest$default", android.content.Context.class, java.lang.String.class, Map.class, CgiBizDataCls, CallbackThreadEnumClass, NetCallbackClass, int.class, java.lang.Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                String url = (String) param.args[1];
                Map<String, String> paramMap = (Map<String, String>) param.args[2];
                Object cgiBizData = param.args[3];
                //null
                Object callbackThreadEnum = param.args[4];
                Object netCallback = param.args[5];
                //i2=16
                int i2 = (int) param.args[6];
                //obj = null
                Object obj = param.args[7];
                LogUtil.PrintLog("url:" + url + " paramMap:" + paramMap.toString() + " cgiBizData:" + cgiBizData + " i2:" + i2, "hookSendGrabHbRequest");
            }
        });
    }

}
