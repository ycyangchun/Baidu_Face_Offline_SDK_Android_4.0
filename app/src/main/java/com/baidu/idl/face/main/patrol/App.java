package com.baidu.idl.face.main.patrol;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.baidu.idl.face.main.activity.FaceAuthActicity;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.callback.Callback;

public class App extends Application {
    Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
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
}
