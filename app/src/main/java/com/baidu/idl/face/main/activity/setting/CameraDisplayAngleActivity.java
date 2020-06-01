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
 * date : 2019/5/27 6:41 PM
 * description :摄像头视频流回显角度
 */
public class CameraDisplayAngleActivity extends BaseActivity {
    private RadioButton cdaDisplayZeroAngle;
    private RadioButton cdaDisplayNinetyAngle;
    private RadioButton cdaDisplayOneHundredEighty;
    private RadioButton cdaDisplayTwoHundredSeventy;
    private int zero = 0;
    private int ninety = 90;
    private int oneHundredEighty = 180;
    private int twoHundredSeventy = 270;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameradisplayangle);

        init();
    }

    public void init() {
        cdaDisplayZeroAngle = findViewById(R.id.cda_display_zero_angle);
        cdaDisplayNinetyAngle = findViewById(R.id.cda_display_ninety_angle);
        cdaDisplayOneHundredEighty = findViewById(R.id.cda_display_one_hundred_eighty);
        cdaDisplayTwoHundredSeventy = findViewById(R.id.cda_display_two_hundred_seventy);
        Button cdaSave = findViewById(R.id.cda_save);

        if (SingleBaseConfig.getBaseConfig().getVideoDirection() == zero) {
            cdaDisplayZeroAngle.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getVideoDirection() == ninety) {
            cdaDisplayNinetyAngle.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getVideoDirection() == oneHundredEighty) {
            cdaDisplayOneHundredEighty.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getVideoDirection() == twoHundredSeventy) {
            cdaDisplayTwoHundredSeventy.setChecked(true);
        }

        cdaDisplayZeroAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdaDisplayNinetyAngle.setChecked(false);
                cdaDisplayOneHundredEighty.setChecked(false);
                cdaDisplayTwoHundredSeventy.setChecked(false);

            }
        });

        cdaDisplayNinetyAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdaDisplayZeroAngle.setChecked(false);
                cdaDisplayOneHundredEighty.setChecked(false);
                cdaDisplayTwoHundredSeventy.setChecked(false);

            }
        });

        cdaDisplayOneHundredEighty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdaDisplayZeroAngle.setChecked(false);
                cdaDisplayNinetyAngle.setChecked(false);
                cdaDisplayTwoHundredSeventy.setChecked(false);

            }
        });

        cdaDisplayTwoHundredSeventy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdaDisplayZeroAngle.setChecked(false);
                cdaDisplayNinetyAngle.setChecked(false);
                cdaDisplayOneHundredEighty.setChecked(false);

            }
        });

        cdaSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cdaDisplayZeroAngle.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setVideoDirection(zero);
                }
                if (cdaDisplayNinetyAngle.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setVideoDirection(ninety);
                }
                if (cdaDisplayOneHundredEighty.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setVideoDirection(oneHundredEighty);
                }
                if (cdaDisplayTwoHundredSeventy.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setVideoDirection(twoHundredSeventy);
                }
                ConfigUtils.modityJson();
                finish();
            }
        });
    }


}
