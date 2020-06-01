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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.PreviewTexture;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;

public class FaceRGBIROpenDebugSearchActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "face-rgb-ir";
    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = 640;
    private static final int PERFER_HEIGH = 480;

    private Context mContext;

    // 调试页面控件
    private TextView mDetectText;
    private CircleImageView mDetectImage;
    private TextureView mDrawDetectFaceView;
    private ImageView mFaceDetectImageView;
    private TextView mTvNum;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;

    // 深度数据显示
    private TextView mTvIr;
    private TextView mTvIrScore;

    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;

    // 导航栏控件
    private Button mButReturn;
    private Button mBtSetting;


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
    private Paint paint;
    private RectF rectF;

    private RelativeLayout relativeLayout;
    private float rgbLiveScore;
    private float nirLiveScore;
    private TextView mRGBText;
    private TextView mNIRText;

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[3];
    private boolean requestToInner = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_rgb_open_debug);
        mContext = this;
        Log.e("shang", "openOnCreate");
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

        // 预览文字显示
        findViewById(R.id.detect_nir_surface_text).setVisibility(View.VISIBLE);
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
        // Ir活体
        mTvIr = findViewById(R.id.tv_ir);
        mTvIr.setVisibility(View.VISIBLE);
        mTvIrScore = findViewById(R.id.tv_ir_score);
        mTvIrScore.setVisibility(View.VISIBLE);
        // 特征提取
        mTvFeature = findViewById(R.id.tv_feature);
        // 检索
        mTvAll = findViewById(R.id.tv_all);
        // 总耗时
        mTvAllTime = findViewById(R.id.tv_all_time);

        // 双目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);
//        mCameraPreviewView = findViewById(R.id.camera_preview_view);
//        mCameraPreviewView.setVisibility(View.VISIBLE);
        // 双目摄像头IR 图像预览
        irCameraPreviewView = findViewById(R.id.ir_camera_preview_view);
        irCameraPreviewView.setVisibility(View.VISIBLE);
        // pass 显示
        // 近红外显示
        mNIRText = findViewById(R.id.nir_text);
        // rgb显示
        mRGBText = findViewById(R.id.rgb_text);

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
        Log.e("shang", "openOnResume");
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
    protected void onDestroy() {
        super.onDestroy();
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
                            displayTip(msg);
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
                    mNIRText.setVisibility(View.GONE);
                    mRGBText.setVisibility(View.GONE);
                    mTvDetect.setText(String.format("检测 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB得分 ：%s", 0));
                    mTvIr.setText(String.format("Ir活体 ：%s ms", 0));
                    mTvIrScore.setText(String.format("Ir得分 ：%s", 0));
                    mTvFeature.setText(String.format("特征抽取 ：%s ms", 0));
                    mTvAll.setText(String.format("检索比对 ：%s ms", 0));
                    mTvAllTime.setText(String.format("总耗时 ：%s ms", 0));
                    return;
                }

                if (requestToInner) {
                    mDetectText.setText("预览区域内人脸不全");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);

                    mTvDetect.setText(String.format("检测 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB得分 ：%s", 0));
                    mTvFeature.setText(String.format("特征抽取 ：%s ms", 0));
                    mTvAll.setText(String.format("检索比对 ：%s ms", 0));
                    mTvAllTime.setText(String.format("总耗时 ：%s ms", 0));
                    return;
                }

                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();

                mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));

                float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                float irLivenessScore = livenessModel.getIrLivenessScore();
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
                if (irLivenessScore < nirLiveScore) {
                    mNIRText.setVisibility(View.VISIBLE);
                    mNIRText.setText("FAIL");
                    mNIRText.setTextColor(Color.parseColor("#ac182a"));
                    mDetectText.setText("活体检测未通过");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                } else {
                    mNIRText.setVisibility(View.VISIBLE);
                    mNIRText.setText("PASS");
                    mNIRText.setTextColor(Color.parseColor("#016838"));
                }

                if (rgbLivenessScore > rgbLiveScore && irLivenessScore > nirLiveScore) {
                    User user = livenessModel.getUser();
                    if (user != null) {
                        String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                + "/" + user.getImageName();
                        Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                        mDetectImage.setImageBitmap(bitmap);
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
                mTvIr.setText(String.format("Ir活体 ：%s ms", livenessModel.getIrLivenessDuration()));
                mTvIrScore.setText(String.format("Ir得分 ：%s", livenessModel.getIrLivenessScore()));
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

                        startActivity(new Intent(FaceRGBIROpenDebugSearchActivity.this,
                                FaceRGBIRCloseDebugSearchActivity.class));

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