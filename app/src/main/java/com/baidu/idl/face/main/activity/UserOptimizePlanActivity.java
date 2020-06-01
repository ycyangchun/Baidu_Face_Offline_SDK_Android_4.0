package com.baidu.idl.face.main.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;

/**
 * author : shangrong
 * date : 2019/10/29 3:56 PM
 * description :
 */
public class UserOptimizePlanActivity extends Activity {
    private RadioGroup uopIsAllow;
    private RadioButton uopAllow;
    private RadioButton uopRefused;
    private boolean isAllow;
    private Button buttonImportBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useroptimizeplan);

        init();
    }

    public void init() {
        PreferencesUtil.initPrefs(this);
        isAllow = PreferencesUtil.getBoolean("UserOptimizePlan", false);
        uopIsAllow = findViewById(R.id.uop_isallow);
        uopAllow = findViewById(R.id.uop_allow);
        uopRefused = findViewById(R.id.uop_refused);
        buttonImportBack = findViewById(R.id.button_import_back);

        buttonImportBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (isAllow) {
            uopAllow.setChecked(true);
        } else {
            uopRefused.setChecked(true);
        }
        uopIsAllow.setOnCheckedChangeListener(onCheckedChangeListener);
    }


    public RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.uop_allow:
                    uopAllow.setChecked(true);
                    PreferencesUtil.putBoolean("UserOptimizePlan", true);
                    break;
                case R.id.uop_refused:
                    uopRefused.setChecked(true);
                    PreferencesUtil.putBoolean("UserOptimizePlan", false);
                    break;
            }
        }
    };
}
