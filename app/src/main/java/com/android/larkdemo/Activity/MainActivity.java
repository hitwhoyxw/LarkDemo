package com.android.larkdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.android.larkdemo.R;
import com.android.larkdemo.Utils.HookUtils;
import com.android.larkdemo.Utils.LogUtil;

public class MainActivity extends AppCompatActivity {

    boolean isMoudleEnable = false;
    boolean isDelayEnable = false;
    boolean isMuteEnable = false;
    float delayTimeMin = 0;
    float daleyTimeMax = 0;
    String muteKeyword = "";
    Switch moudleSwitch;
    Switch delaySwitch;
    Switch muteSwitch;
    EditText delayTimeMinEditText;
    EditText delayTimeMaxEditText;
    EditText muteKeywordEditText;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readAllConfig();
        initConfigSetting();
        findAllView();
        setListeners();
    }


    @Override
    protected void onDestroy() {
        saveAllConfig();
        super.onDestroy();

    }

    private void initConfigSetting() {
        try {
            sharedPreferences = getSharedPreferences("module_config", Context.MODE_PRIVATE);
        } catch (SecurityException e) {
            Log.i("initConfigSetting error:" + e.getMessage(), "initConfigSetting");
            sharedPreferences = null;
        }
    }

    public void findAllView() {
        moudleSwitch = findViewById(R.id.switch_module);
        delaySwitch = findViewById(R.id.switch_delay);
        muteSwitch = findViewById(R.id.switch_keyword);
        delayTimeMinEditText = findViewById(R.id.editText_starttime);
        delayTimeMaxEditText = findViewById(R.id.editText_endtime);
        muteKeywordEditText = findViewById(R.id.editText_keyword);
    }

    public void setListeners() {
        moudleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMoudleEnable = isChecked;
                saveAllConfig();
            }
        });
        delaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDelayEnable = isChecked;
                saveAllConfig();
            }
        });
        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMuteEnable = isChecked;
                saveAllConfig();
            }
        });
        delayTimeMinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }
                try {
                    delayTimeMin = Float.parseFloat(delayTimeMinEditText.getText().toString());
                    saveAllConfig();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
        delayTimeMaxEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }
                try {
                    daleyTimeMax = Float.parseFloat(delayTimeMaxEditText.getText().toString());
                    saveAllConfig();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
        muteKeywordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }
                try {
                    muteKeyword = muteKeywordEditText.getText().toString();
                    saveAllConfig();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void readAllConfig() {
        try {
            isMoudleEnable = sharedPreferences.getBoolean("isMoudleEnable", false);
            isDelayEnable = sharedPreferences.getBoolean("isDelayEnable", false);
            isMuteEnable = sharedPreferences.getBoolean("isMuteEnable", false);
            delayTimeMin = sharedPreferences.getFloat("delayTimeMin", 0);
            daleyTimeMax = sharedPreferences.getFloat("daleyTimeMax", 0);
            muteKeyword = sharedPreferences.getString("muteKeyword", "");


            moudleSwitch.setChecked(isMoudleEnable);
            delaySwitch.setChecked(isDelayEnable);
            muteSwitch.setChecked(isMuteEnable);
            delayTimeMinEditText.setText(String.valueOf(delayTimeMin));
            delayTimeMaxEditText.setText(String.valueOf(daleyTimeMax));
            muteKeywordEditText.setText(muteKeyword);
        } catch (NullPointerException e) {
            Log.i("readAllConfig error:" + e.getMessage(), "readAllConfig");
        }

    }

    public void saveAllConfig() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isMoudleEnable", isMoudleEnable);
            editor.putBoolean("isDelayEnable", isDelayEnable);
            editor.putBoolean("isMuteEnable", isMuteEnable);
            editor.putFloat("delayTimeMin", delayTimeMin);
            editor.putFloat("daleyTimeMax", daleyTimeMax);
            editor.putString("muteKeyword", muteKeyword);
        } catch (NullPointerException e) {
            Log.i("saveAllConfig error:" + e.getMessage(), "saveAllConfig");
        }

    }

}