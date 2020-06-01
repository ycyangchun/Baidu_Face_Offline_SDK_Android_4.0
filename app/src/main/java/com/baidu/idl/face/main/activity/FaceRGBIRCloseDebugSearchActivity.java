package com.baidu.idl.face.main.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.PreviewTexture;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;

public class FaceRGBIRCloseDebugSearchActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "face-rgb-ir";
    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = 640;
    private static final int PERFER_HEIGH = 480;

    private Context mContext;

    private Button mButReturn;
    private Button mBtSetting;

    // 关闭Debug 模式
    private TextView mDetectText;
    private CircleImageView mDetectImage;
    private TextView mTrackText;


    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;

    // textureView用于显示摄像头数据。
    private AutoTexturePreviewView mAutoCameraPreviewView;
    //    private TextureView mCameraPreviewView;
    private TextureView irCameraPreviewView;

    // 摄像头个数
    private int mCameraNum;
    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    // 判断摄像头数据源
    private int camemra1DataMean;
    private int camemra2DataMean;
    private volatile boolean camemra1IsRgb = false;
    private volatile boolean rgbOrIrConfirm = false;

    // 调试按钮
    private Button mDebugBtn;

    // 人脸框绘制
    private TextureView mDrawDetectFaceView;
    private Paint paint;
    private RectF rectF;

    private RelativeLayout relativeLayout;
    private float rgbLiveScore;
    private float nirLiveScore;

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[3];
    private boolean requestToInner = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_rgb_close_debug);
        mContext = this;
        Log.e("shang", "closeOnCreate");
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
    }

    /**
     * 开启Debug View
     */
    private void initView() {

        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);

        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // Live 阈值
        nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
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
        // 图像预览
        mDetectText = findViewById(R.id.detect_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);
        mTrackText = findViewById(R.id.track_txt);

        // 双目摄像头RGB 图像预览
//        mCameraPreviewView = findViewById(R.id.camera_preview_view);
//        mCameraPreviewView.setVisibility(View.VISIBLE);
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);


        // 双目摄像头IR 图像预览
        irCameraPreviewView = findViewById(R.id.ir_camera_preview_view);
        // todo 用户可以根据自己需求启动或者关闭红外
        irCameraPreviewView.setVisibility(View.VISIBLE);

        // 调试按钮
        mDebugBtn = findViewById(R.id.debug_btn);
        mDebugBtn.setOnClickListener(this);

        // 双摄像头
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {

            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
//            mPreview[0] = new PreviewTexture(this, mCameraPreviewView);
            mPreview[1] = new PreviewTexture(this, irCameraPreviewView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("shang", "closeOnResume");
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {

                startTestCloseDebugRegisterFunction();
//                mCamera[0] = Camera.open(0);
                mCamera[1] = Camera.open(1);
//                mPreview[0].setCamera(mCamera[0], PREFER_WIDTH, PERFER_HEIGH);
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGH);
//                mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] data, Camera camera) {
//                        dealRgb(data);
//                    }
//                });
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
//        if (textureView != null) {
//            textureView.setOpaque(false);
//        }
//        if (textureViewOne != null) {
//            textureViewOne.setOpaque(false);
//        }
    }


    private void startTestCloseDebugRegisterFunction() {
        // TODO ： 临时放置
        // 设置前置摄像头
         CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        CameraPreviewManager.getInstance().startPreview(this, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        dealRgb(data);
                    }
                });
    }


    @Override
    protected void onPause() {

        CameraPreviewManager.getInstance().stopPreview();

        if (mCameraNum >= 2) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCameraNum >= 2) {
                    if (mCamera[i] != null) {
                        mCamera[i].setPreviewCallback(null);
                        mCamera[i].stopPreview();
                        mPreview[i].release();
                        mCamera[i].release();
                        mCamera[i] = null;
                    }
                }
            }
        }

        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private synchronized void rgbOrIr(int index, byte[] data) {
        byte[] tmp = new byte[PREFER_WIDTH * PERFER_HEIGH];
        try {
            System.arraycopy(data, 0, tmp, 0, PREFER_WIDTH * PERFER_HEIGH);
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }
        int count = 0;
        int total = 0;
        for (int i = 0; i < PREFER_WIDTH * PERFER_HEIGH; i = i + 10) {
            total += byteToInt(tmp[i]);
            count++;
        }
        if (index == 0) {
            camemra1DataMean = total / count;
        } else {
            camemra2DataMean = total / count;
        }
        if (camemra1DataMean != 0 && camemra2DataMean != 0) {
            if (camemra1DataMean > camemra2DataMean) {
                camemra1IsRgb = true;
            } else {
                camemra1IsRgb = false;
            }
            rgbOrIrConfirm = true;
        }
    }

    public int byteToInt(byte b) {
        // Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    private void choiceRgbOrIrType(int index, byte[] data) {
        // camera1如果为rgb数据，调用dealRgb，否则为Ir数据，调用Ir
        if (index == 0) {
            if (camemra1IsRgb) {
                dealRgb(data);
            } else {
                dealIr(data);
            }
        } else {
            if (camemra1IsRgb) {
                dealIr(data);
            } else {
                dealRgb(data);
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
            FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, null, PERFER_HEIGH,
                    PREFER_WIDTH, 3, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 输出结果
                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("fixedarea")) {
                                isInserLimit(livenessModel);
                                // 输出结果
                                checkResult(livenessModel);
                            }

                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                                checkResult(livenessModel);
                            }
                        }

                        @Override
                        public void onTip(int code, String msg) {
                            displayTip(code, msg);
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            // 绘制人脸框
                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                                showFrame(livenessModel);
                            }
                        }
                    });
            rgbData = null;
            irData = null;
        }
    }

    private void displayTip(final int code, final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (code == 0) {
                    mTrackText.setVisibility(View.GONE);
                } else {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("识别失败");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                }
                mDetectText.setText(tip);
            }
        });
    }

    private void checkResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    mTrackText.setVisibility(View.GONE);
                    mDetectText.setText("未检测到人脸");
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                    return;
                } else if (requestToInner) {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("预览区域内人脸不全");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectText.setText("");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                    return;
                } else {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    float irLivenessScore = livenessModel.getIrLivenessScore();
                    if (rgbLivenessScore < rgbLiveScore || irLivenessScore < nirLiveScore) {
                        mTrackText.setVisibility(View.VISIBLE);
                        mTrackText.setText("识别失败");
                        mTrackText.setBackgroundColor(Color.RED);
                        mDetectText.setText("活体检测未通过");
                        mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                    } else {
                        User user = livenessModel.getUser();
                        if (user != null) {
                            String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                    + "/" + user.getImageName();
                            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                            mDetectImage.setImageBitmap(bitmap);
                            mTrackText.setVisibility(View.VISIBLE);
                            mTrackText.setText("识别成功");
                            mTrackText.setBackgroundColor(Color.rgb(66, 147, 136));
                            mDetectText.setText("欢迎您， " + user.getUserName());

                        } else {
                            mTrackText.setVisibility(View.VISIBLE);
                            mTrackText.setText("识别失败");
                            mTrackText.setBackgroundColor(Color.RED);
                            mDetectText.setText("搜索不到用户");
                            mDetectText.setVisibility(View.VISIBLE);
                            mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                        }
                    }
                }

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
//                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
//                        mCameraPreviewView, model.getBdFaceImageInstance());
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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        startActivity(new Intent(this, FaceRGBIRCloseDebugSearchActivity.class));

                        startActivity(new Intent(FaceRGBIRCloseDebugSearchActivity.this,
                                FaceRGBIROpenDebugSearchActivity.class));
                    }
                }, 1000);

                finish();

                break;
        }
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
        FaceOnDrawTexturViewUtil.converttPointXY(pointXY, mAutoCameraPreviewView,
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