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
 * author : shangrong
 * date : 2019/5/27 6:54 PM
 * description :识别模型选择
 */
public class RecognizeModleTypeAcctivity extends BaseActivity {

    private RadioButton rmtLiveModle;
    private RadioButton rmtIdModle;
    private int one = 1;
    private int two = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizemodletype);

        init();
    }

    public void init() {
        rmtLiveModle = findViewById(R.id.rmt_livemodle);
        rmtIdModle = findViewById(R.id.rmt_idmodle);
        Button rmtSave = findViewById(R.id.rmt_save);


        if (SingleBaseConfig.getBaseConfig().getActiveModel() == one) {
            rmtLiveModle.setChecked(true);

        }
        if (Integer.valueOf(SingleBaseConfig.getBaseConfig().getActiveModel()) == two) {
            rmtIdModle.setChecked(true);

        }


        rmtLiveModle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rmtIdModle.setChecked(false);
            }
        });

        rmtIdModle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rmtLiveModle.setChecked(false);

            }
        });

        rmtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rmtLiveModle.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setActiveModel(one);
                }
                if (rmtIdModle.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setActiveModel(two);
                }
                ConfigUtils.modityJson();
                finish();
            }
        });
    }

}
