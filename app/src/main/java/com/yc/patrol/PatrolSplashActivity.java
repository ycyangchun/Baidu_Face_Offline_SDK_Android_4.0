package com.yc.patrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.BatchImportActivity;
import com.baidu.idl.face.main.activity.FaceRGBCloseDebugSearchActivity;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.LogUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;
import com.yc.patrol.utils.Tools;

import java.io.File;

/**
 * 闪屏页面，展示SDK版本信息...
 */
public class PatrolSplashActivity extends BaseActivity {

    private Context mContext;
    public static final int PAGE_TYPE = 999;
    private static final String TAG = "PatrolSplashActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_patrol);

        mContext = this;
        initView();
        batchImport();
    }

    /**
     * UI 相关VIEW 初始化
     */
    private void initView() {
        // 获取当前版本号
        TextView versionTv = findViewById(R.id.tv_version);
        versionTv.setText(String.format("当前版本：v %s", "1.0.0"));

        //跳转登录
        Button btnSelect = findViewById(R.id.btn_select);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FaceRGBCloseDebugSearchActivity.class);
                startActivityForResult(intent, PAGE_TYPE);
            }
        });

        //跳转注册
        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PatrolFaceRegisterActivity.class);
                startActivityForResult(intent, PAGE_TYPE);
            }
        });

        Button btnAuth = findViewById(R.id.btn_auth);
        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, BatchImportActivity.class));

            }
        });


        // logo 图
        ImageView mIvIcon = findViewById(R.id.iv_icon);
        mIvIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchImport();
            }
        });
    }

    public void batchImport() {
        // 获取导入目录 /sdcard/Face-Import
        String xml = "PatrolPlan.xml";
        // 判断Face.zip是否存在
        File zipFile = FileUtils.isFileExist(Environment.getExternalStorageDirectory().toString(), xml);
        if (zipFile == null) {
            LogUtils.i(TAG, "导入数据的文件夹没有PatrolPlan.xml");
            ToastUtils.toastL(mContext,"没有PatrolPlan.xml");
            return;
        }
        Tools.ReadXml(Environment.getExternalStorageDirectory().toString()+ File.separator + xml);
    }
}
