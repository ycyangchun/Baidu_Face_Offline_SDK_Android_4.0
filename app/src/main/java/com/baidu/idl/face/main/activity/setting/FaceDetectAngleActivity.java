package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.facesdkdemo.R;


/**
 * author : shangrog
 * date : 2019/5/27 6:37 PM
 * description :人脸检测角度设置
 */
public class FaceDetectAngleActivity extends BaseActivity {
    private RadioButton fdaPreviewZeroAngle;
    private RadioButton fdaPreviewNinetyAngle;
    private RadioButton fdaPreviewOneHundredEighty;
    private RadioButton fdaPreviewTwoHundredSeventy;

    private int zero = 0;
    private int ninety = 90;
    private int oneHundredEighty = 180;
    private int twoHundredSeventy = 270;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facedetectangle);

        init();
    }

    public void init() {
        fdaPreviewZeroAngle = findViewById(R.id.fda_preview_zero_angle);
        fdaPreviewNinetyAngle = findViewById(R.id.fda_preview_ninety_angle);
        fdaPreviewOneHundredEighty = findViewById(R.id.fda_preview_one_hundred_eighty);
        fdaPreviewTwoHundredSeventy = findViewById(R.id.fda_preview_two_hundred_seventy);
        Button fdaSave = findViewById(R.id.fda_save);

        if (SingleBaseConfig.getBaseConfig().getDetectDirection() == zero) {
            fdaPreviewZeroAngle.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getDetectDirection() == ninety) {
            fdaPreviewNinetyAngle.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getDetectDirection() == oneHundredEighty) {
            fdaPreviewOneHundredEighty.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getDetectDirection() == twoHundredSeventy) {
            fdaPreviewTwoHundredSeventy.setChecked(true);
        }


        fdaPreviewZeroAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fdaPreviewNinetyAngle.setChecked(false);
                fdaPreviewOneHundredEighty.setChecked(false);
                fdaPreviewTwoHundredSeventy.setChecked(false);

            }
        });

        fdaPreviewNinetyAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fdaPreviewZeroAngle.setChecked(false);
                fdaPreviewOneHundredEighty.setChecked(false);
                fdaPreviewTwoHundredSeventy.setChecked(false);

            }
        });

        fdaPreviewOneHundredEighty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fdaPreviewZeroAngle.setChecked(false);
                fdaPreviewNinetyAngle.setChecked(false);
                fdaPreviewTwoHundredSeventy.setChecked(false);

            }
        });

        fdaPreviewTwoHundredSeventy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fdaPreviewZeroAngle.setChecked(false);
                fdaPreviewNinetyAngle.setChecked(false);
                fdaPreviewOneHundredEighty.setChecked(false);

            }
        });

        fdaSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fdaPreviewZeroAngle.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setDetectDirection(zero);
                }
                if (fdaPreviewNinetyAngle.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setDetectDirection(ninety);
                }
                if (fdaPreviewOneHundredEighty.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setDetectDirection(oneHundredEighty);
                }
                if (fdaPreviewTwoHundredSeventy.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setDetectDirection(twoHundredSeventy);
                }
                ConfigUtils.modityJson();
                finish();
            }
        });

    }

}
