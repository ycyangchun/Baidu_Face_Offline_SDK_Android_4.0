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
 * date : 2019/5/27 6:35 PM
 * description :检测追踪策略
 */
public class DetectTrailStrategyActivity extends BaseActivity {
    private RadioButton dtsTtMax;
    private RadioButton dtsTtFirst;
    private RadioButton dtsTtNone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detecttrailstrategy);

        init();
    }

    public void init() {
        dtsTtMax = findViewById(R.id.dts_tt_max);
        dtsTtFirst = findViewById(R.id.dts_tt_first);
        dtsTtNone = findViewById(R.id.dts_tt_none);
        Button dtsSave = findViewById(R.id.dts_save);

        if (SingleBaseConfig.getBaseConfig().getTrackType().equals("max")) {
            dtsTtMax.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getTrackType().equals("none")) {
            dtsTtNone.setChecked(true);
        }

        dtsTtMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtsTtMax.setChecked(true);
                dtsTtFirst.setChecked(false);
                dtsTtNone.setChecked(false);
            }
        });
        dtsTtFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtsTtMax.setChecked(false);
                dtsTtFirst.setChecked(true);
                dtsTtNone.setChecked(false);
            }
        });
        dtsTtNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtsTtMax.setChecked(false);
                dtsTtFirst.setChecked(false);
                dtsTtNone.setChecked(true);
            }
        });

        dtsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dtsTtMax.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setTrackType("max");
                }
                if (dtsTtNone.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setTrackType("none");
                }
                ConfigUtils.modityJson();
                finish();
            }
        });
    }
}
