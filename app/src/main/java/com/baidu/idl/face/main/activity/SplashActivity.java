package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;

/**
 * 闪屏页面，展示SDK版本信息...
 */
public class SplashActivity extends BaseActivity {

    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;
        initView();
    }

    /**
     * UI 相关VIEW 初始化
     */
    private void initView() {
        // 获取当前版本号
        TextView versionTv = findViewById(R.id.tv_version);
        versionTv.setText(String.format("当前版本：v %s", Utils.getVersionName(mContext)));

        //跳转主页面
        Button btnSelect = findViewById(R.id.btn_select);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, MainActivity.class));
            }
        });

        // logo 图
        ImageView mIvIcon = findViewById(R.id.iv_icon);
        mIvIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, MainActivity.class));
            }
        });
    }
}
