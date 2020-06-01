package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.facesdkdemo.R;


/**
 * author : shangrong
 * date : 2019/5/27 6:52 PM
 * description :识别模型阀值
 */
public class RecognizeModleThresholdActivity extends BaseActivity {
    private EditText rmtEtThreshold;
    private int initValue;
    private int zero = 0;
    private static final int hundered = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizemodlethreshold);

        initValue = SingleBaseConfig.getBaseConfig().getThreshold();
        init();
    }

    public void init() {
        Button rmtDecrease = findViewById(R.id.rmt_Decrease);
        Button rmtIncrease = findViewById(R.id.rmt_Increase);
        rmtEtThreshold = findViewById(R.id.rmt_etthreshold);
        Button rmtSave = findViewById(R.id.rmt_save);

        rmtEtThreshold.setText(SingleBaseConfig.getBaseConfig().getThreshold() + "");

        rmtDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initValue > zero && initValue <= hundered) {
                    initValue = initValue - 1;
                    rmtEtThreshold.setText(initValue + "");
                }
            }
        });

        rmtIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initValue >= zero && initValue < hundered) {
                    initValue = initValue + 1;
                    rmtEtThreshold.setText(initValue + "");
                }
            }
        });

        rmtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleBaseConfig.getBaseConfig().setThreshold(Integer.valueOf(rmtEtThreshold.getText().toString()));
                ConfigUtils.modityJson();
                finish();
            }
        });
    }


}
