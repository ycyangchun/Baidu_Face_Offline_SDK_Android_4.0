package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.view.CustomDialog;
import com.baidu.idl.facesdkdemo.R;

public class FaceConfigActivity extends BaseActivity implements CustomDialog.OnDialogClickListener {

    private CustomDialog mDialog;
    private Context mContext;
    private RelativeLayout relativeLayout;
    private String pageType;
    private TextView titleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_config);
        titleTv = findViewById(R.id.textView4);
        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            pageType = intent.getStringExtra("page_type");
            if ("register".equals(pageType)) {
                titleTv.setText("注册");
            }
        }

        mDialog = new CustomDialog(this);
        mDialog.setDialogClickListener(this);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        relativeLayout = findViewById(R.id.all_relative);

        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // 当屏幕的宽大于屏幕宽时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfirmClick(View view) {

        if ("search".equals(pageType)) {
            if (!FaceApi.getInstance().isinitSuccess()) {
                ToastUtils.toast(mContext, "人脸数据库未加载成功，请稍等");
                return;
            }
            // 判断活体类型
            int liveType = SingleBaseConfig.getBaseConfig().getType();
            boolean isDebug = SingleBaseConfig.getBaseConfig().isDebug();
            int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();

            switch (liveType) {
                case 1: {
                    // RGB 无活体
                    if (isDebug) {
                        startActivity(new Intent(mContext, FaceRGBOpenDebugSearchActivity.class));
                    } else {
                        startActivity(new Intent(mContext, FaceRGBCloseDebugSearchActivity.class));
                    }
                    break;
                }
                case 2: {
                    // RGB
                    if (isDebug) {
                        startActivity(new Intent(mContext, FaceRGBOpenDebugSearchActivity.class));
                    } else {
                        startActivity(new Intent(mContext, FaceRGBCloseDebugSearchActivity.class));
                    }
                    break;
                }

                case 3: {
                    // NIR
                    if (isDebug) {
                        startActivity(new Intent(mContext, FaceRGBIROpenDebugSearchActivity.class));
                    } else {
                        startActivity(new Intent(mContext, FaceRGBIRCloseDebugSearchActivity.class));
                    }
                    break;
                }

                case 4: {
                    // Depth
                    if (isDebug) {
                        if (6 == cameraType) {
                            startActivity(new Intent(mContext, PicoFaceRGBDepthOpenDebugSearchActivity.class));
                        } else {
                            startActivity(new Intent(mContext, FaceRGBDepthOpenDebugSearchActivity.class));
                        }
                    } else {
                        if (6 == cameraType) {
                            startActivity(new Intent(mContext, PicoFaceRGBDepthCloseDebugSearchActivity.class));
                        } else {
                            startActivity(new Intent(mContext, FaceRGBDepthCloseDebugSearchActivity.class));
                        }
                    }
                    break;
                }

                default:
                    break;
            }
            finish();
        } else if ("register".equals(pageType)) {
            // 跳转注册
            startActivity(new Intent(mContext, FaceRegisterActivity.class));
            finish();

        } else {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT);
        }

        mDialog.dismiss();
    }

    @Override
    public void onModifierClick(View view) {
        mDialog.dismiss();
        startActivity(new Intent(this, SettingMainActivity.class));
    }

}
