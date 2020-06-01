/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.face.main.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.PicoAutoTexturePreviewView;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.GlobalSet;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.PicoRenderer;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.picozense.sdk.IFrameCallback;
import com.picozense.sdk.PsCamera;
import com.picozense.sdk.PsFrame;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;

/**
 * 深度结构光
 */
public class PicoFaceRGBDepthOpenDebugSearchActivity extends BaseActivity implements View.OnClickListener {

    private static final int DEPTH_NEED_PERMISSION = 33;
    private static final String TAG = "PicoOSearchActivity";
    // RGB摄像头图像宽和高
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private Context mContext;

    // 调试页面控件
    private TextView mDetectText;
    private CircleImageView mDetectImage;
    private TextureView mDrawDetectFaceView;

    private PicoAutoTexturePreviewView mAutoCameraPreviewView;

    // 显示Depth图
    private GLSurfaceView mDepthGLView;
    private PicoRenderer depthRender;

    private ImageView mFaceDetectImageView;
    private TextView mTvNum;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;

    // 深度数据显示
    private TextView mTvDepth;
    private TextView mTvDepthScore;

    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;

    // 导航栏控件
    private Button mButReturn;
    private Button mBtSetting;

    private RelativeLayout relativeLayout;
    private RelativeLayout mRelativeLayout;

    // 当前摄像头类型
    private static int cameraType;

    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] depthData;

    // 人脸框绘制
    private RectF rectF;
    private Paint paint;

    private float rgbLiveScore;
    private float depthLiveScore;
    private TextView mDepthText;
    private TextView mRGBText;
    private Bitmap mBmpRGB;
    private Bitmap mBmpDepth;
    private PsCamera picoCamera;
    byte[] mByteBuffer_depth = {0};
    byte[] mByteBuffer_rgb = {0};
    int countNum = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_face_depth_open_debug_pico);
        mContext = this;
        PreferencesUtil.initPrefs(this);
        cameraType = PreferencesUtil.getInt(GlobalSet.TYPE_CAMERA, GlobalSet.ORBBECATLAS);
        initView();

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
        picoCamera = new PsCamera();
        if (picoCamera != null) {
            picoCamera.init(this, null);
        }
        mBmpRGB = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        mBmpDepth = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        mByteBuffer_rgb = new byte[640 * 480 * 3];
        mByteBuffer_depth = new byte[640 * 480 * 2];
    }

    /**
     * 开启Debug View
     */
    private void initView() {

        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);

        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // depth 阈值
        depthLiveScore = SingleBaseConfig.getBaseConfig().getDepthLiveScore();
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);

        // 返回
        mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);
        // 设置
        mBtSetting = findViewById(R.id.btn_setting);
        mBtSetting.setOnClickListener(this);

        mDetectText = findViewById(R.id.detect_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);

        // 关闭调试
        // 版本号
        TextView versionTxt = findViewById(R.id.version_txt);
        versionTxt.setText("版本 ：v " + Utils.getVersionName(this));

        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.setPreviewSize(RGB_WIDTH, RGB_HEIGHT);
        // 送检RGB 图像回显
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.VISIBLE);
        // 存在底库的数量
        mTvNum = findViewById(R.id.tv_num);
        mTvNum.setText(String.format("底库 ： %s 个", FaceApi.getInstance().getmUserNum()));
        // 检测耗时
        mTvDetect = findViewById(R.id.tv_detect);
        // RGB活体
        mTvLive = findViewById(R.id.tv_live);
        mTvLiveScore = findViewById(R.id.tv_live_score);
        // Depth活体
        mTvDepth = findViewById(R.id.tv_depth);
        mTvDepth.setVisibility(View.VISIBLE);
        mTvDepthScore = findViewById(R.id.tv_depth_score);
        mTvDepthScore.setVisibility(View.VISIBLE);
        // 特征提取
        mTvFeature = findViewById(R.id.tv_feature);
        // 检索
        mTvAll = findViewById(R.id.tv_all);
        // 总耗时
        mTvAllTime = findViewById(R.id.tv_all_time);

        // 深度摄像头数据回显
        depthRender = new PicoRenderer();
        mDepthGLView = findViewById(R.id.depth_camera_preview_view);
        mDepthGLView.setVisibility(View.VISIBLE);
        mDepthGLView.setZOrderOnTop(true);
        mDepthGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mDepthGLView.setZOrderMediaOverlay(true);
        mDepthGLView.setEGLContextClientVersion(2);
        mDepthGLView.setRenderer(depthRender);
        mDepthGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // 调试按钮
        findViewById(R.id.debug_btn).setOnClickListener(this);

        // PASS || FALL显示
        mDepthText = findViewById(R.id.depth_text);
        mRGBText = findViewById(R.id.rgb_text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
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
        if (picoCamera != null) {
            picoCamera.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (picoCamera != null) {
            picoCamera.destroy();
            picoCamera = null;
        }
        super.onDestroy();
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

    private synchronized void checkData(byte[] depth, byte[] rgb) {
        if (depth != null && rgb != null) {
            FaceSDKManager.getInstance().onDetectCheck(rgb, null, depth, RGB_HEIGHT,
                    RGB_WIDTH, 4, mFaceDetectCallBack);
        }
    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                mDetectText.setText(tip);
            }
        });
    }

    private void checkResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    mDetectText.setText("未检测到人脸");
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);

                    mTvDetect.setText(String.format("检测 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB得分 ：%s", 0));
                    mTvDepth.setText(String.format("Depth活体 ：%s ms", 0));
                    mTvDepthScore.setText(String.format("Depth得分 ：%s", 0));
                    mTvFeature.setText(String.format("特征抽取 ：%s ms", 0));
                    mTvAll.setText(String.format("检索比对 ：%s ms", 0));
                    mTvAllTime.setText(String.format("总耗时 ：%s ms", 0));
                    return;
                }
//                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
//                if (image != null) {
//                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
//                }
                float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                float depthLivenessScore = livenessModel.getDepthLivenessScore();
                // 判断活体状态UI显示
                if (rgbLivenessScore < rgbLiveScore) {
                    mRGBText.setVisibility(View.VISIBLE);
                    mRGBText.setText("FAIL");
                    mRGBText.setTextColor(Color.parseColor("#ac182a"));
                    mDetectText.setText("活体检测未通过");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                } else {
                    mRGBText.setVisibility(View.VISIBLE);
                    mRGBText.setText("PASS");
                    mRGBText.setTextColor(Color.parseColor("#016838"));
                }
                if (depthLivenessScore < depthLiveScore) {
                    mDepthText.setVisibility(View.VISIBLE);
                    mDepthText.setText("FAIL");
                    mDepthText.setTextColor(Color.parseColor("#ac182a"));
                    mDetectText.setText("活体检测未通过");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                } else {
                    mDepthText.setVisibility(View.VISIBLE);
                    mDepthText.setText("PASS");
                    mDepthText.setTextColor(Color.parseColor("#016838"));
                }

                if (rgbLivenessScore > rgbLiveScore && depthLivenessScore > depthLiveScore) {
                    User user = livenessModel.getUser();
                    if (user != null) {
                        String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                + "/" + user.getImageName();
                        Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                        if (bitmap != null) {
                            mDetectImage.setImageBitmap(bitmap);
                        } else {
                            Log.e(TAG, "bitmap is null  path  " + absolutePath);
                        }
                        mDetectText.setText("欢迎您， " + user.getUserName());

                    } else {
                        mDetectText.setText("搜索不到用户");
                        mDetectText.setVisibility(View.VISIBLE);
                        mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                    }
                }
                mTvDetect.setText(String.format("检测 ：%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB活体 ：%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB得分 ：%s", livenessModel.getRgbLivenessScore()));
                mTvDepth.setText(String.format("Depth活体 ：%s ms", livenessModel.getDepthtLivenessDuration()));
                mTvDepthScore.setText(String.format("Depth得分 ：%s", livenessModel.getDepthLivenessScore()));
                mTvFeature.setText(String.format("特征抽取 ：%s ms", livenessModel.getFeatureDuration()));
                mTvAll.setText(String.format("检索比对 ：%s ms", livenessModel.getCheckDuration()));
                mTvAllTime.setText(String.format("总耗时 ：%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }

    /**
     * 绘制人脸框
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 返回
            case R.id.btn_back:
                if (picoCamera != null) {
                    picoCamera.stop();
                    picoCamera.destroy();
                    picoCamera = null;
                }
                finish();
                break;
            // 设置
            case R.id.btn_setting:
                Intent intent = new Intent(mContext, SettingMainActivity.class);
                intent.putExtra("page_type", "search");
                startActivityForResult(intent, PAGE_TYPE);
                finish();
                break;
            case R.id.debug_btn:
                // mAutoCameraPreviewView.removeAllViews();
                if (picoCamera != null) {
                    picoCamera.stop();
                    picoCamera.destroy();
                    picoCamera = null;
                }
                startActivity(new Intent(this, PicoFaceRGBDepthCloseDebugSearchActivity.class));
                finish();
                break;
        }
    }

    public FaceDetectCallBack mFaceDetectCallBack = new FaceDetectCallBack() {
        @Override
        public void onFaceDetectCallback(LivenessModel livenessModel) {
            checkResult(livenessModel);
        }

        @Override
        public void onTip(int code, String msg) {
            if (msg != null && !msg.isEmpty()) {
                displayTip(msg);
            }
        }

        @Override
        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
            showFrame(livenessModel);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (0 == msg.what) {
                mAutoCameraPreviewView.draw(mBmpRGB);
                mFaceDetectImageView.setImageBitmap(mBmpRGB);
            }
        }
    };

    // 预览帧回调
    public IFrameCallback depthIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(PsFrame depthFrame, PsFrame irFrame, PsFrame rgbFrame) {
            if (countNum < 3) {
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
                depthRender.setBuf(mBmpDepth);
                mDepthGLView.requestRender();
            }
            checkData(mByteBuffer_depth, mByteBuffer_rgb);
        }
    };
}