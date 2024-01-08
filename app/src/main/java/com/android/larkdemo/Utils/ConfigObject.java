package com.android.larkdemo.Utils;

public class ConfigObject {
    public boolean isMoudleEnable;
    public boolean isDelayEnable;
    public boolean isMuteEnable;
    public float delayTimeMin;
    public float daleyTimeMax;
    public String muteKeyword;

    public ConfigObject(String finance_sdk_version, boolean is_return_name_auth, boolean isMoudleEnable, boolean isDelayEnable, boolean isMuteEnable, float delayTimeMin, float daleyTimeMax, String muteKeyword) {
        this.isMoudleEnable = isMoudleEnable;
        this.isDelayEnable = isDelayEnable;
        this.isMuteEnable = isMuteEnable;
        this.delayTimeMin = delayTimeMin;
        this.daleyTimeMax = daleyTimeMax;
        this.muteKeyword = muteKeyword;
    }

    public ConfigObject(boolean isMoudleEnable, boolean isDelayEnable, boolean isMuteEnable, float delayTimeMin, float daleyTimeMax, String muteKeyword) {
        this.isMoudleEnable = isMoudleEnable;
        this.isDelayEnable = isDelayEnable;
        this.isMuteEnable = isMuteEnable;
        this.delayTimeMin = delayTimeMin;
        this.daleyTimeMax = daleyTimeMax;
        this.muteKeyword = muteKeyword;
    }

    public static ConfigObject CreateDefaultConfigObject(boolean defaultOpen) {
        if (defaultOpen) {
            return new ConfigObject("6.8.8", true, true, true, true, 0f, 0f, "");
        } else {
            return new ConfigObject("6.8.8", true, false, false, false, 0f, 0f, "");
        }
    }

    public ConfigObject() {

    }
}
