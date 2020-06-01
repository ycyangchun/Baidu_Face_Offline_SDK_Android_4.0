package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.facesdkdemo.R;

/**
 * 镜像调节页面
 * Created by v_liujialu01 on 2019/6/17.
 */

public class MirrorSettingActivity extends BaseActivity implements View.OnClickListener {
    private Switch mSwitchMirrorRgb;
    private Switch mSwitchMirrorNir;
    private Button mButtonMirrorSave;
    private int zero = 0;
    private int one = 1;
    public static final int cancle = 404;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror_setting);
        initView();
        initData();
    }

    private void initView() {
        mSwitchMirrorRgb = findViewById(R.id.switch_mirror_rgb);
        mSwitchMirrorNir = findViewById(R.id.switch_mirror_nir);
        mButtonMirrorSave = findViewById(R.id.button_mirror_save);
        mButtonMirrorSave.setOnClickListener(this);
    }

    private void initData() {
        if (SingleBaseConfig.getBaseConfig().getMirrorRGB() == zero) {  // rgb无镜像
            mSwitchMirrorRgb.setChecked(false);
        } else if (SingleBaseConfig.getBaseConfig().getMirrorRGB() == one) {  // rgb有镜像
            mSwitchMirrorRgb.setChecked(true);
        }

        if (SingleBaseConfig.getBaseConfig().getMirrorNIR() == zero) {  // nir无镜像
            mSwitchMirrorNir.setChecked(false);
        } else if (SingleBaseConfig.getBaseConfig().getMirrorNIR() == one) {  // nir有镜像
            mSwitchMirrorNir.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_mirror_save:
                if (mSwitchMirrorRgb.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setMirrorRGB(one);
                } else {
                    SingleBaseConfig.getBaseConfig().setMirrorRGB(zero);
                }

                if (mSwitchMirrorNir.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setMirrorNIR(one);
                } else {
                    SingleBaseConfig.getBaseConfig().setMirrorNIR(zero);
                }

                ConfigUtils.modityJson();
                finish();
                break;
            case cancle:
                break;
            default:
                break;
        }
    }
}
