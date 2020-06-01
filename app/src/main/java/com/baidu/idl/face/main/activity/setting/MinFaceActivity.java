package com.baidu.idl.face.main.activity.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.facesdkdemo.R;


/**
 * author : shangrog
 * date : 2019/5/27 6:34 PM
 * description :最小人脸界面
 */

public class MinFaceActivity extends BaseActivity {
    private EditText mfEtAmount;
    private int initValue;
    private int thirty = 30;
    private int twoHundered = 200;
    private static final int ten = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minface);

        init();
    }


    public void init() {
        initValue = SingleBaseConfig.getBaseConfig().getMinimumFace();

        Button mfDecrease = findViewById(R.id.mf_Decrease);
        Button mfIncrease = findViewById(R.id.mf_Increase);
        mfEtAmount = findViewById(R.id.mf_etAmount);
        Button mfSave = findViewById(R.id.mf_save);
        mfEtAmount.setText(SingleBaseConfig.getBaseConfig().getMinimumFace() + "");
        mfDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initValue > thirty && initValue <= twoHundered) {
                    initValue = initValue - ten;
                    mfEtAmount.setText(initValue + "");
                }
            }
        });

        mfIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initValue >= thirty && initValue < twoHundered) {
                    initValue = initValue + ten;
                    mfEtAmount.setText(initValue + "");
                }
            }
        });

        mfSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleBaseConfig.getBaseConfig().setMinimumFace(Integer.valueOf(mfEtAmount.getText().toString()));
                ConfigUtils.modityJson();
                showAlertAndExit(getString(R.string.setting_notice));
            }
        });

    }


    private void showAlertAndExit(String message) {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }
}
