package com.yc.patrol;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.baidu.idl.face.main.activity.FaceAuthActicity;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.yc.patrol.utils.CommonUtils;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.callback.Callback;

public class App extends Application {
    Context mContext;
    private Boolean isInitConfig;
    private Boolean isConfigExit;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        CommonUtils.init(this);
        if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_UNACTIVATION) {
            initLicense();
        } else {
            initLicense(2);
        }

    }
    /**
     * 启动应用程序，如果之前初始过，自动初始化鉴权和模型（可以添加到Application 中）
     */
    private void initLicense() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().init(mContext, new SdkInitListener() {
                @Override
                public void initStart() {

                }

                @Override
                public void initLicenseSuccess() {
                    initcfg();
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                    // 如果授权失败，跳转授权页面
                    ToastUtils.toast(mContext, errorCode + msg);
                    startActivity(new Intent(mContext, FaceAuthActicity.class));
                }

                @Override
                public void initModelSuccess() {

                }

                @Override
                public void initModelFail(int errorCode, String msg) {

                }
            });
        }
    }

    private void initLicense(int num) {
        FaceAuth faceAuth = new FaceAuth();
        if (num == 1) {

        } else {
            faceAuth.initLicenseOffLine(this, new Callback() {
                @Override
                public void onResponse(final int code, final String response) {
                    if (code == 0) {
                        FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
                            @Override
                            public void initStart() {

                            }

                            @Override
                            public void initLicenseSuccess() {

                            }

                            @Override
                            public void initLicenseFail(int errorCode, String msg) {
                                ToastUtils.toast(mContext, errorCode + msg);
                            }

                            @Override
                            public void initModelSuccess() {

                            }

                            @Override
                            public void initModelFail(int errorCode, String msg) {

                            }
                        });
                    } else {
                        ToastUtils.toast(mContext, response);
                    }
                }
            });
        }
    }

    private void initcfg() {
        // todo shangrong 增加配置信息初始化操作
        isConfigExit = ConfigUtils.isConfigExit();
        isInitConfig = ConfigUtils.initConfig();
        if (isInitConfig && isConfigExit) {
//            Toast.makeText(PatrolSplashActivity.this, "初始配置加载成功", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(PatrolSplashActivity.this, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            ConfigUtils.modityJson();
        }
        // 属性开启属性检测
        SingleBaseConfig.getBaseConfig().setAttribute(true);
        FaceSDKManager.getInstance().initConfig();

    }
}
