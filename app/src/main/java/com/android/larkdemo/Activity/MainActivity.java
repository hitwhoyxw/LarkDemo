package com.android.larkdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.android.larkdemo.R;
import com.android.larkdemo.Utils.HookUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllView();
        readAllConfig();
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
                isMoudleEnable = isChecked;
                HookUtils.writeConfig("isMoudleEnable", String.valueOf(isMoudleEnable));
            }
        });
        delaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDelayEnable = isChecked;
                HookUtils.writeConfig("isDelayEnable", String.valueOf(isDelayEnable));
            }
        });
        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMuteEnable = isChecked;
                HookUtils.writeConfig("isMuteEnable", String.valueOf(isMuteEnable));
            }
        });
        delayTimeMinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }
                try {
                    delayTimeMin = Integer.parseInt(delayTimeMinEditText.getText().toString());
                    HookUtils.writeConfig("delayTimeMin", String.valueOf(delayTimeMin));
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
                    daleyTimeMax = Integer.parseInt(delayTimeMaxEditText.getText().toString());
                    HookUtils.writeConfig("daleyTimeMax", String.valueOf(daleyTimeMax));
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
                    HookUtils.writeConfig("muteKeyword", muteKeyword);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void readAllConfig() {
        String moduleConfig = HookUtils.readConfig("isMoudleEnable");
        String delayConfig = HookUtils.readConfig("isDelayEnable");
        String muteConfig = HookUtils.readConfig("isMuteEnable");
        String delayTimeMinConfig = HookUtils.readConfig("delayTimeMin");
        String daleyTimeMaxConfig = HookUtils.readConfig("daleyTimeMax");
        String muteKeywordConfig = HookUtils.readConfig("muteKeyword");

        if (moduleConfig != null) {
            isMoudleEnable = Boolean.parseBoolean(moduleConfig);
        } else {
            isMoudleEnable = false;
        }
        if (delayConfig != null) {
            isDelayEnable = Boolean.parseBoolean(delayConfig);
        } else {
            isDelayEnable = false;
        }
        if (muteConfig != null) {
            isMuteEnable = Boolean.parseBoolean(muteConfig);
        } else {
            isMuteEnable = false;
        }
        if (delayTimeMinConfig != null) {
            delayTimeMin = Float.parseFloat(delayTimeMinConfig);
        } else {
            delayTimeMin = 0;
        }
        if (daleyTimeMaxConfig != null) {
            daleyTimeMax = Float.parseFloat(daleyTimeMaxConfig);
        } else {
            daleyTimeMax = 0;
        }
        if (muteKeywordConfig != null) {
            muteKeyword = muteKeywordConfig;
        } else {
            muteKeyword = "";
        }
        moudleSwitch.setChecked(isMoudleEnable);
        delaySwitch.setChecked(isDelayEnable);
        muteSwitch.setChecked(isMuteEnable);
        delayTimeMinEditText.setText(String.valueOf(delayTimeMin));
        delayTimeMaxEditText.setText(String.valueOf(daleyTimeMax));
        muteKeywordEditText.setText(muteKeyword);
    }

    public void saveAllConfig() {
        HookUtils.writeConfig("isMoudleEnable", String.valueOf(isMoudleEnable));
        HookUtils.writeConfig("isDelayEnable", String.valueOf(isDelayEnable));
        HookUtils.writeConfig("isMuteEnable", String.valueOf(isMuteEnable));
        HookUtils.writeConfig("delayTimeMin", String.valueOf(delayTimeMin));
        HookUtils.writeConfig("daleyTimeMax", String.valueOf(daleyTimeMax));
        HookUtils.writeConfig("muteKeyword", muteKeyword);
    }

}