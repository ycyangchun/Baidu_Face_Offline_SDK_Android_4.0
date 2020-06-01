package com.baidu.idl.face.main.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.FaceRoundView;
import com.baidu.idl.face.main.view.PreviewTexture;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

import java.io.File;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;


/**
 * @Time: 2019/6/4
 * @Author: v_zhangxiaoqing01
 * @Description: RGB+IR 注册页面
 */


public class FaceIRRegisterActivity extends BaseActivity implements View.OnClickListener {

    // 遮罩
    private FaceRoundView rectView;


    // textureView用于绘制人脸框等。
    private ImageView testImageview;
    // detect-info
    // 注册的 提示 view
    private TextView mTrackText;
    private TextView mDetectText;
    private CircleImageView mDetectImage;
    private Button backButton;
    private Button setButton;

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    // RGB摄像头图像宽和高
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private boolean qualityControl;

    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;


    // 摄像头个数
    private int mCameraNum;
    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    // 判断摄像头数据源
    private int camemra1DataMean;
    private int camemra2DataMean;
    private volatile boolean camemra1IsRgb = false;

    private String username = null;
    private String groupId = null;
    private String userInfo = null;

    private Bitmap rgbBitmap = null;

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[3];
    private boolean requestToInner = false;

    // 人脸框绘制
    private TextureView mDrawDetectFaceView;
    private Paint paint;
    private RectF rectF;

    private AutoTexturePreviewView rgbTexture;
    private TextureView irTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_ir_register);
        initView();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("user_name");
            groupId = intent.getStringExtra("group_id");
            userInfo = intent.getStringExtra("user_info");
        }

//        qualityControl = SingleBaseConfig.getBaseConfig().isQualityControl();
        // 注册默认开启质量检测
//        SingleBaseConfig.getBaseConfig().setQualityControl(true);
//        FaceSDKManager.getInstance().initConfig();

    }

    private void initView() {

        backButton = findViewById(R.id.id_reg_ir_back);
        setButton = findViewById(R.id.id_reg_ir_setting);
        backButton.setOnClickListener(this);
        setButton.setOnClickListener(this);
        // 双目摄像头RGB 图像预览
        rgbTexture = findViewById(R.id.face_ir_detect_view);
        rgbTexture.setVisibility(View.VISIBLE);
        // 双目摄像头IR 图像预览
        irTexture = findViewById(R.id.texture_preview_ir);
        // 调试模式打开 显示IR 图像回显
        if (SingleBaseConfig.getBaseConfig().getNirOrDepth()) {
            irTexture.setVisibility(View.VISIBLE);
        } else {
            irTexture.setVisibility(View.INVISIBLE);
        }

        testImageview = findViewById(R.id.test_ir_imgView);

        // 双摄像头
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[0] = new PreviewTexture(this, rgbTexture.getTextureView());
            mPreview[1] = new PreviewTexture(this, irTexture);
        }

        // 图像预览
        mTrackText = findViewById(R.id.track_txt);
        // 最下方的提示label
        mDetectText = findViewById(R.id.detect_reg_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);


        // 注册页面 只支持 固定区域检测
        // 遮罩
//        rectView = findViewById(R.id.rect_view_ir);
//        rectView.setVisibility(View.VISIBLE);
//        // 修改IR 的适配
//        DisplayMetrics dm = new DisplayMetrics();
//        Display display = this.getWindowManager().getDefaultDisplay();
//        display.getMetrics(dm);
//        int mDisplayWidth = dm.widthPixels;
//        int mDisplayHeight = dm.heightPixels;
//        int h = 0;
//        int w = 0;
//        // TODO 有两种方式，现在采用的是按长边适配，人脸会比较大，按短边适配人脸会比较小
//        // 与RGB 仍有区别，RGB页面是宽>长,IR 是长>宽
//        if (mDisplayWidth * PERFER_HEIGH > mDisplayHeight * PREFER_WIDTH) {
//            w = mDisplayWidth;
//            h = PERFER_HEIGH * w / PREFER_WIDTH;
//        } else {
//            h = mDisplayHeight;
//            w = PREFER_WIDTH * h / PERFER_HEIGH;
//        }
//        FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
//                (int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO),
//                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//        rgbTexture.setLayoutParams(cameraFL);

        // 画人脸框
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        paint = new Paint();
        rectF = new RectF();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                mCamera[0] = Camera.open(0);
                mCamera[1] = Camera.open(1);
                mPreview[0].setCamera(mCamera[0], PREFER_WIDTH, PERFER_HEIGH);
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGH);
                // 摄像头图像预览角度
                int cameraRotation = SingleBaseConfig.getBaseConfig().getVideoDirection();
                mCamera[0].setDisplayOrientation(cameraRotation);
                mCamera[1].setDisplayOrientation(cameraRotation);
                mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealRgb(data);
                    }
                });
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
            } catch (RuntimeException e) {
                Log.e("qing", e.getMessage());
            }
        }

    }


    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();

    }

    private void dealIr(byte[] data) {
        irData = data;
        checkData();
    }

    private synchronized void checkData() {
        if (rgbData != null && irData != null) {

            // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
            boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
            if (isRGBDisplay) {
                showDetectImage(rgbData);
            }

            // 注册默认开启质量检测
            FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, null,
                    PERFER_HEIGH, PREFER_WIDTH, 3, 2, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {

                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("fixedarea")) {
                                isInserLimit(livenessModel);
                                // 输出结果
                                checkResult(livenessModel);
                            }

                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                                showFrame(livenessModel);
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

            rgbData = null;
            irData = null;
        }
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
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, PERFER_HEIGH,
                PREFER_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        Bitmap bitmap = null;
        try {
            bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        } catch (Exception e) {
            Log.e("qing", String.valueOf(e.getStackTrace()));
        }
        final Bitmap finalBitmap = bitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testImageview.setVisibility(View.VISIBLE);
                testImageview.setImageBitmap(finalBitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }


    @Override
    protected void onPause() {
        if (mCameraNum >= 2) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCamera[i] != null) {
                    mCamera[i].setPreviewCallback(null);
                    mCamera[i].stopPreview();
                    mPreview[i].release();
                    mCamera[i].release();
                    mCamera[i] = null;
                }
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 重置质检状态
        SingleBaseConfig.getBaseConfig().setQualityControl(qualityControl);
        FaceSDKManager.getInstance().initConfig();
    }

    private void checkResult(LivenessModel livenessModel) {
        if (livenessModel == null) {
            clearTip();
            return;
        }

        if (requestToInner) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("预览区域内人脸不全");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectText.setText("");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                }
            });
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
        Float irLiveScore = livenessModel.getIrLivenessScore();

        if (rgbLiveScore >= SingleBaseConfig.getBaseConfig().getRgbLiveScore() &&
                irLiveScore >= SingleBaseConfig.getBaseConfig().getNirLiveScore()) { // RGb & NIR 活体同时通过
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
                displayTip("特征提取失败");
            } else {
                displayTip("特征提取失败");
            }
        } else {
            displayTip("活体检测未通过");
        }

    }


    // 根据特征抽取的结果 注册人脸
    private void register(float ret, byte[] faceFeature) {

        if (username == null || groupId == null) {
            displayTip("注册信息缺失");
            return;
        }

        String imageName = groupId + "-" + username + ".jpg";
        // 注册到人脸库
        boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(groupId, username, imageName,
                userInfo, faceFeature);

        if (isSuccess) {
            // 关闭摄像头
            CameraPreviewManager.getInstance().stopPreview();
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
                            FaceIRRegisterActivity.this.finish();
                        }
                    }, 0);

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


    private void clearTip() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectText.setText("未检测到人脸");
                mTrackText.setVisibility(View.GONE);
            }
        });

    }


    private void displayTip(final String status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (requestToInner) {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("预览区域内人脸不全");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectText.setText("");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                    return;
                }

                mTrackText.setVisibility(View.GONE);
                mDetectText.setText(status);
            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view == backButton) {
            FaceIRRegisterActivity.this.finish();
        } else if (view == setButton) {
            Intent intent = new Intent(this, SettingMainActivity.class);
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


    /**
     * 绘制人脸框。
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
                        rgbTexture, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);

            }
        });
    }

    // 判断人脸是否在园内
    private void isInserLimit(final LivenessModel livenessModel) {
        if (livenessModel == null) {
            requestToInner = false;
            return;
        }
        FaceInfo[] faceInfos = livenessModel.getTrackFaceInfo();
        if (faceInfos == null || faceInfos.length == 0) {
            requestToInner = false;
            return;
        }

        pointXY[0] = livenessModel.getFaceInfo().centerX;
        pointXY[1] = livenessModel.getFaceInfo().centerY;
        pointXY[2] = livenessModel.getFaceInfo().width;
        FaceOnDrawTexturViewUtil.converttPointXY(pointXY, rgbTexture,
                livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);
        float lfetLimitX = AutoTexturePreviewView.circleX - AutoTexturePreviewView.circleRadius;
        float rightLimitX = AutoTexturePreviewView.circleX + AutoTexturePreviewView.circleRadius;
        float topLimitY = AutoTexturePreviewView.circleY - AutoTexturePreviewView.circleRadius;
        float bottomLimitY = AutoTexturePreviewView.circleY + AutoTexturePreviewView.circleRadius;

        if (pointXY[0] - pointXY[2] / 2 < lfetLimitX
                || pointXY[0] + pointXY[2] / 2 > rightLimitX
                || pointXY[1] - pointXY[2] / 2 < topLimitY
                || pointXY[1] + pointXY[2] / 2 > bottomLimitY) {
            requestToInner = true;
        } else {
            requestToInner = false;
        }
    }

}
