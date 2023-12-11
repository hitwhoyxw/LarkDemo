package com.android.larkdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.larkdemo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}