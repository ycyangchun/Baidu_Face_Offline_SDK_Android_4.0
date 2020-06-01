package com.baidu.idl.face.main.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.PicoAutoTexturePreviewView;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.GlobalSet;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.FaceRoundView;
import com.baidu.idl.face.main.view.PicoRenderer;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.picozense.sdk.IFrameCallback;
import com.picozense.sdk.PsCamera;
import com.picozense.sdk.PsFrame;

import java.io.File;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;


/**
 * @Time: 2019/6/4
 * @Author: v_zhangxiaoqing01
 * @Description: RGB+Depth(深度)注册页面
 */


public class PicoFaceDepthRegisterActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "PicoRegisterActivity";
    // 遮罩
    private FaceRoundView rectView;

    private Context mContext;
    // textureView用于绘制人脸框等。
    private ImageView testImageView;

    private Button backButton;
    private Button setButton;
    // detect-info
    // 注册的 提示 view
    private TextView mTrackText;
    private TextView mDetectText;
    private CircleImageView mDetectImage;

    // textureView用于显示摄像头数据。
    private PicoAutoTexturePreviewView mAutoCameraPreviewView;

    // RGB摄像头图像宽和高
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private boolean qualityControl;

    private final Object mySync = new Object();
    // 循环取深度图像数据
    private boolean exit = false;

    // 当前摄像头类型
    private int cameraType;

    private String username = null;
    private String groupId = null;
    private String userInfo = null;

    private Bitmap rgbBitmap = null;

    // 显示Depth图
    private GLSurfaceView mDepthGLView;
    private PicoRenderer depthRender;

    private Bitmap mBmpRGB;
    private Bitmap mBmpDepth;
    private PsCamera picoCamera;
    byte[] mByteBuffer_depth = {0};
    byte[] mByteBuffer_rgb = {0};
    int countNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_depth_register_pico);

        initView();
        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("user_name");
            groupId = intent.getStringExtra("group_id");
            userInfo = intent.getStringExtra("user_info");
        }

        PreferencesUtil.initPrefs(this);
        cameraType = SingleBaseConfig.getBaseConfig().getCameraType();

        qualityControl = SingleBaseConfig.getBaseConfig().isQualityControl();
        // 注册默认开启质量检测
        SingleBaseConfig.getBaseConfig().setQualityControl(true);
        FaceSDKManager.getInstance().initConfig();

        picoCamera = new PsCamera();
        if (picoCamera != null) {
            picoCamera.init(this, null);
        }
        mBmpRGB = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        mBmpDepth = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        mByteBuffer_rgb = new byte[640 * 480 * 3];
        mByteBuffer_depth = new byte[640 * 480 * 2];
    }


    private void initView() {

        backButton = findViewById(R.id.id_reg_depth_back);
        setButton = findViewById(R.id.id_reg_depth_setting);
        backButton.setOnClickListener(this);
        setButton.setOnClickListener(this);

        // 双目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.rgb_pro_surface);
        mAutoCameraPreviewView.setPreviewSize(RGB_WIDTH, RGB_HEIGHT);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);

        // 深度摄像头数据回显
        depthRender = new PicoRenderer();
        mDepthGLView = findViewById(R.id.depth_reg_surface_view);
        mDepthGLView.setZOrderOnTop(true);
        mDepthGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mDepthGLView.setZOrderMediaOverlay(true);
        mDepthGLView.setEGLContextClientVersion(2);
        mDepthGLView.setRenderer(depthRender);
        mDepthGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        testImageView = findViewById(R.id.test_reg_depth_imgView);

        // 图像预览
        mTrackText = findViewById(R.id.track_txt);
        mDetectText = findViewById(R.id.detect_reg_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);


        // 注册页面 只支持 固定区域检测
        // 遮罩
        rectView = findViewById(R.id.rect_view_depth);
        rectView.setVisibility(View.VISIBLE);

        DisplayMetrics dm = new DisplayMetrics();
        Display display = this.getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        int mDisplayWidth = dm.widthPixels;
        int mDisplayHeight = dm.heightPixels;

        int w = mDisplayWidth;
        int h = mDisplayHeight;

        FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
                (int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO),
                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mAutoCameraPreviewView.setLayoutParams(cameraFL);
    }


    private synchronized void checkData(byte[] depthData, byte[] rgbData) {
        if (rgbData != null && depthData != null) {
            FaceSDKManager.getInstance().onDetectCheck(rgbData, null, depthData, RGB_HEIGHT,
                    RGB_WIDTH, 4, 2, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 做距离的检测过滤
                            boolean isFilterSuccess = faceSizeFilter(livenessModel.getFaceInfo(),
                                    RGB_WIDTH, RGB_HEIGHT);
                            if (isFilterSuccess) {
                                // 展示model
                                checkResult(livenessModel);
                            }
                        }

                        @Override
                        public void onTip(int code, String msg) {
                            displayTip(msg);
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            Log.e("qing", "");
                        }
                    });
        }
    }


    private void checkResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        if (livenessModel == null) {
            clearTip();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackText.setVisibility(View.GONE);
                mDetectText.setText("活体检测判断中...");
            }
        });

        Float rgbLiveScore = livenessModel.getRgbLivenessScore();
        Float depthLiveScore = livenessModel.getDepthLivenessScore();

        if (rgbLiveScore >= SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                && depthLiveScore >= SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
            float ret = livenessModel.getFeatureCode();
            if (ret == 128) {
                if (livenessModel.getFeature() == null) {
                    return;
                }
                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                rgbBitmap = BitmapUtils.getInstaceBmp(image);
                // 注册
                register(ret, livenessModel.getFeature());
            } else if (ret == -1) {
                displayResult("特征提取失败");
            } else {
                displayResult("特征提取失败");
            }

        } else {
            displayResult("活体检测未通过");

        }


    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        if (picoCamera != null) {
            picoCamera.setFrameCallback(depthIFrameCallback);
            if (SingleBaseConfig.getBaseConfig().getMirrorRGB() == 1) {
                Log.i(TAG, "mirror is open");
                picoCamera.setImageMirror(0);
            } else {
                picoCamera.setImageMirror(1);
            }
            picoCamera.setWorkMode(1);
            picoCamera.start(this);
        }

    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        synchronized (mySync) {
            if (picoCamera != null) {
                picoCamera.stop();
            }
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v("gangzi", "onDestroy:");
        super.onDestroy();
        if (picoCamera != null) {
            picoCamera.destroy();
            picoCamera = null;
        }

        // 重置质检状态
        SingleBaseConfig.getBaseConfig().setQualityControl(qualityControl);
        FaceSDKManager.getInstance().initConfig();

        finish();
    }


    /**
     * 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
     *
     * @param rgb
     */
    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, RGB_HEIGHT,
                RGB_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testImageView.setVisibility(View.VISIBLE);
                testImageView.setImageBitmap(bitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }


    private void clearTip() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectText.setText("未检测到人脸");
                mTrackText.setVisibility(View.GONE);
            }
        });

    }

    private void displayTip(final String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectText.setText(msg);
            }
        });

    }


    private void displayResult(final String status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackText.setVisibility(View.GONE);
                mDetectText.setText(status);
            }
        });
    }


    // 根据特征抽取的结果 注册人脸
    private void register(float ret, byte[] faceFeature) {

        if (username == null || groupId == null) {
            displayResult("注册信息缺失");
            return;
        }


        String imageName = groupId + "-" + username + ".jpg";
        // 注册到人脸库
        boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(groupId, username, imageName,
                userInfo, faceFeature);


        if (isSuccess) {

            // 关闭摄像头
            if (picoCamera != null) {
                picoCamera.stop();
                picoCamera.destroy();
                picoCamera = null;
            }
            Log.e("qing", "注册成功");

            // 压缩、保存人脸图片至300 * 300
            File faceDir = FileUtils.getBatchImportSuccessDirectory();
            File file = new File(faceDir, imageName);
            ImageUtils.resize(rgbBitmap, file, 300, 300);

            // 数据变化，更新内存
            FaceApi.getInstance().initDatabases(true);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("采集完毕");
                    mTrackText.setBackgroundColor(Color.rgb(66, 147, 136));
                    mDetectText.setText("用户" + username + "," + "已经注册完毕");
                    mDetectImage.setImageBitmap(rgbBitmap);
                    // 防止重复注册
                    username = null;
                    groupId = null;

                    // 做延时 finish
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            PicoFaceDepthRegisterActivity.this.finish();
                        }
                    }, 1);

                }
            });

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("采集完毕");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectText.setText("注册失败");
                }
            });
        }


    }


    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }


    @Override
    public void onClick(View view) {

        if (view == backButton) {
            if (picoCamera != null) {
                picoCamera.stop();
                picoCamera.destroy();
                picoCamera = null;
            }

            PicoFaceDepthRegisterActivity.this.finish();
        } else if (view == setButton) {
            Intent intent = new Intent(mContext, SettingMainActivity.class);
            intent.putExtra("page_type", "register");
            startActivityForResult(intent, PAGE_TYPE);
            finish();
        }
    }

    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {

        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        float ratio = (float) faceInfo.width / (float) bitMapHeight;
        if (ratio > 0.6) {

            displayTip("人脸离屏幕太近，请调整与屏幕的距离");
            return false;
        } else if (ratio < 0.2) {
            displayTip("人脸离屏幕太远，请调整与屏幕的距离");
            return false;
        } else if (faceInfo.centerX > bitMapWidth * 3 / 4) {
            displayTip("人脸在屏幕中太靠右");
            return false;
        } else if (faceInfo.centerX < bitMapWidth / 4) {
            displayTip("人脸在屏幕中太靠左");
            Log.e("qing", "--------屏幕中太靠左--------");
            return false;
        } else if (faceInfo.centerY > bitMapHeight * 3 / 4) {
            displayTip("人脸在屏幕中太靠下");
            return false;
        } else if (faceInfo.centerY < bitMapHeight / 4) {
            displayTip("人脸在屏幕中太靠上");
            return false;
        }

        return true;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (0 == msg.what) {
                mAutoCameraPreviewView.draw(mBmpRGB);

                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                if (isRGBDisplay) {
                    testImageView.setVisibility(View.VISIBLE);
                    testImageView.setImageBitmap(mBmpRGB);
                }

                // 调试模式打开 显示深度摄像头数据回显
                if (SingleBaseConfig.getBaseConfig().getNirOrDepth()) {
                    mDepthGLView.setVisibility(View.VISIBLE);
                    depthRender.setBuf(mBmpDepth);
                    mDepthGLView.requestRender();
                } else {
                    mDepthGLView.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    public IFrameCallback depthIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(PsFrame depthFrame, PsFrame irFrame, PsFrame rgbFrame) {
            if (countNum < 5) {
                countNum++;
                return;
            }
            if (rgbFrame != null) {
                rgbFrame.frameData.rewind();
                rgbFrame.frameData.get(mByteBuffer_rgb);
                picoCamera.RgbToRgba_bf(rgbFrame.frameData, mBmpRGB, rgbFrame.width, rgbFrame.height);

                Message fwMsg = mHandler.obtainMessage();
                fwMsg.what = 0;
                mHandler.sendMessage(fwMsg);

            }

            if (depthFrame != null) {
                depthFrame.frameData.rewind();
                depthFrame.frameData.get(mByteBuffer_depth);
                picoCamera.Y16ToRgba_bf(depthFrame.frameData, mBmpDepth, depthFrame.width, depthFrame.height, 1500);
            }
            checkData(mByteBuffer_depth, mByteBuffer_rgb);
        }
    };
}
