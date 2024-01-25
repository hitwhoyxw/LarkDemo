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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.android.larkdemo.R;
import com.android.larkdemo.Utils.ConfigObject;
import com.android.larkdemo.Utils.ConfigUtils;
import com.android.larkdemo.Utils.HookUtils;
import com.android.larkdemo.Utils.MyCallback;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LarkDemo";
    static ConfigObject configObject;
    ConfigUtils configUtils;
    Switch moudleSwitch;
    Switch delaySwitch;
    Switch muteSwitch;
    EditText delayTimeMinEditText;
    EditText delayTimeMaxEditText;
    EditText muteKeywordEditText;
    RadioButton rBtb_direct;
    RadioButton rBtn_mock;
    RadioGroup rBtnGroup;

    Button btn_save;

    Button btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configUtils = ConfigUtils.getInstance();
        configUtils.init(true, this);
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
        rBtb_direct = findViewById(R.id.rBtn_direct);
        rBtn_mock = findViewById(R.id.rBtn_mock);
        rBtnGroup = findViewById(R.id.rBtnGroup);
        rBtnGroup.clearCheck();
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    public void setListeners() {
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAllConfig();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readAllConfig();
            }
        });
    }

    public void readAllConfig() {
        configObject = configUtils.getConfig(true);
        if (configObject == null) {
            Log.i(TAG, "readAllConfig: " + "configObject is null");
            return;
        }
        moudleSwitch.setChecked(configObject.isMoudleEnable);
        delaySwitch.setChecked(configObject.isDelayEnable);
        muteSwitch.setChecked(configObject.isMuteEnable);
        delayTimeMinEditText.setText(String.valueOf(configObject.delayTimeMin));
        delayTimeMaxEditText.setText(String.valueOf(configObject.daleyTimeMax));
        muteKeywordEditText.setText(configObject.muteKeyword);
        rBtnGroup.check(!configObject.fetchMode ? R.id.rBtn_mock : R.id.rBtn_direct);
    }

    public void saveAllConfig() {
        try {
            ConfigObject object = new ConfigObject(
                    moudleSwitch.isChecked(),
                    delaySwitch.isChecked(),
                    muteSwitch.isChecked(),
                    Float.parseFloat(delayTimeMinEditText.getText().toString()),
                    Float.parseFloat(delayTimeMaxEditText.getText().toString()),
                    muteKeywordEditText.getText().toString(),
                    rBtnGroup.getCheckedRadioButtonId() == R.id.rBtn_direct
            );
            configUtils.saveConfig(object);
        } catch (NullPointerException e) {
            Log.i(TAG, "saveAllConfig" + e.getMessage());
        }

    }

}