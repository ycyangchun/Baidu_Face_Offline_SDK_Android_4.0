package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.LogUtils;
import com.baidu.idl.facesdkdemo.R;

/**
 * author : shangrong
 * date : 2019/5/27 5:38 PM
 * description :调试模式配置
 */
public class DebugModeActivity extends BaseActivity {
    private CheckBox dmRgbDisplay;
    private CheckBox dmNirOrDepthDisplay;
    private CheckBox dmIsDebug;
    private CheckBox dmIsRGBRevert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debugmode);

        init();
    }

    public void init() {
        dmRgbDisplay = findViewById(R.id.dm_rgbdisplay);
        dmNirOrDepthDisplay = findViewById(R.id.dm_nirordepthdisplay);
        dmIsDebug = findViewById(R.id.dm_isdebug);
        dmIsRGBRevert = findViewById(R.id.dm_isRGBRevert);
        Button dmSave = findViewById(R.id.dm_save);


        if (SingleBaseConfig.getBaseConfig().getDisplay()) {
            dmRgbDisplay.setChecked(true);
        } else {
            dmRgbDisplay.setChecked(false);
        }

        if (SingleBaseConfig.getBaseConfig().getNirOrDepth()) {
            dmNirOrDepthDisplay.setChecked(true);
        } else {
            dmNirOrDepthDisplay.setChecked(false);
        }

        if (SingleBaseConfig.getBaseConfig().isDebug()) {
            dmIsDebug.setChecked(true);
        } else {
            dmIsDebug.setChecked(false);
        }

        if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
            dmIsRGBRevert.setChecked(true);
        } else {
            dmIsRGBRevert.setChecked(false);
        }

        dmSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dmRgbDisplay.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setDisplay(true);
                } else {
                    SingleBaseConfig.getBaseConfig().setDisplay(false);
                }
                if (dmNirOrDepthDisplay.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setNirOrDepth(true);
                } else {
                    SingleBaseConfig.getBaseConfig().setNirOrDepth(false);
                }
                if (dmIsDebug.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setDebug(true);
                    LogUtils.setIsDebug(true);
                } else {
                    SingleBaseConfig.getBaseConfig().setDebug(false);
                    LogUtils.setIsDebug(false);
                }

                if (dmIsRGBRevert.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setRgbRevert(true);
                } else {
                    SingleBaseConfig.getBaseConfig().setRgbRevert(false);
                }


                ConfigUtils.modityJson();
                finish();
            }
        });
    }
}
