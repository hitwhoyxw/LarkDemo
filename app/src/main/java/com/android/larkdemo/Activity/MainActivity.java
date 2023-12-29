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
import com.android.larkdemo.Utils.ConfigUtils;
import com.android.larkdemo.Utils.HookUtils;
import com.android.larkdemo.Utils.LogUtil;

public class MainActivity extends AppCompatActivity {

    ConfigUtils.ConfigObject configObject;
    ConfigUtils configUtils;
    Switch moudleSwitch;
    Switch delaySwitch;
    Switch muteSwitch;
    EditText delayTimeMinEditText;
    EditText delayTimeMaxEditText;
    EditText muteKeywordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configUtils = new ConfigUtils(this);

        readAllConfig();

        findAllView();
        setListeners();
    }


    @Override
    protected void onDestroy() {
        saveAllConfig();
        super.onDestroy();

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
                configObject.isMoudleEnable = isChecked;
                saveAllConfig();
            }
        });
        delaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                configObject.isDelayEnable = isChecked;
                saveAllConfig();
            }
        });
        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                configObject.isMuteEnable = isChecked;
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
                    configObject.delayTimeMin = Float.parseFloat(delayTimeMinEditText.getText().toString());
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
                    configObject.daleyTimeMax = Float.parseFloat(delayTimeMaxEditText.getText().toString());
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
                    configObject.muteKeyword = muteKeywordEditText.getText().toString();
                    saveAllConfig();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void readAllConfig() {
        try {
            configObject = configUtils.getConfig();

            moudleSwitch.setChecked(configObject.isMoudleEnable);
            delaySwitch.setChecked(configObject.isDelayEnable);
            muteSwitch.setChecked(configObject.isMuteEnable);
            delayTimeMinEditText.setText(String.valueOf(configObject.delayTimeMin));
            delayTimeMaxEditText.setText(String.valueOf(configObject.daleyTimeMax));
            muteKeywordEditText.setText(configObject.muteKeyword);
        } catch (Exception e) {
            Log.i("readAllConfig error:" + e.getMessage(), "readAllConfig");
        }

    }

    public void saveAllConfig() {
        try {
            configUtils.saveConfig(configObject);
        } catch (NullPointerException e) {
            Log.i("saveAllConfig error:" + e.getMessage(), "saveAllConfig");
        }

    }

}