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
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;

/**
 * @Time 2019/06/02
 * @Author v_shishuaifeng
 * @Description RGB 开启Debug 模式
 */
public class FaceRGBOpenDebugSearchActivity extends BaseActivity implements View.OnClickListener {

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private Context mContext;

    private TextView mDetectText;
    private CircleImageView mDetectImage;
    private TextureView mDrawDetectFaceView;
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;
    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;

    private RectF rectF;
    private Paint paint;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private float mRgbLiveScore;
    private TextView mRGBText;

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[3];
    private boolean requestToInner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_rgb_open_debug);
        mContext = this;

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
        // 活体状态
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // 活体阈值
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();

        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);

        // 返回
        Button mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);
        // 设置
        Button mBtSetting = findViewById(R.id.btn_setting);
        mBtSetting.setOnClickListener(this);

        mDetectText = findViewById(R.id.detect_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);
        // 关闭调试
        // 版本号
        TextView versionTxt = findViewById(R.id.version_txt);
        versionTxt.setText("版本 ：v " + Utils.getVersionName(this));

        // 单目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        // 送检RGB 图像回显
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.VISIBLE);
        // 存在底库的数量
        TextView mTvNum = findViewById(R.id.tv_num);
        mTvNum.setText(String.format("底库 ： %s 个", FaceApi.getInstance().getmUserNum()));
        // 检测耗时
        mTvDetect = findViewById(R.id.tv_detect);
        // RGB活体
        mTvLive = findViewById(R.id.tv_live);
        mTvLiveScore = findViewById(R.id.tv_live_score);
        // 特征提取
        mTvFeature = findViewById(R.id.tv_feature);
        // 检索
        mTvAll = findViewById(R.id.tv_all);
        // 总耗时
        mTvAllTime = findViewById(R.id.tv_all_time);

        // 调试按钮
        findViewById(R.id.debug_btn).setOnClickListener(this);

        mRGBText = findViewById(R.id.rgb_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTestOpenDebugRegisterFunction();
    }

    private void startTestOpenDebugRegisterFunction() {
        // TODO ： 临时放置
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        // 设置前置摄像头
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);

        CameraPreviewManager.getInstance().startPreview(mContext, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                height, width, mLiveType, new FaceDetectCallBack() {
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
                    }
                });
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
                    mRGBText.setVisibility(View.GONE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                    mTvDetect.setText(String.format("检测 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB得分 ：%s", 0));
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
                if (image != null) {
                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                }
                if (mLiveType == 1) {
                    User user = livenessModel.getUser();
                    if (user == null) {
                        mDetectText.setText("搜索不到用户");
                        mDetectText.setVisibility(View.VISIBLE);
                        mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                        mRGBText.setVisibility(View.GONE);
                    } else {
                        String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                + "/" + user.getImageName();
                        Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                        mDetectImage.setImageBitmap(bitmap);
                        mDetectText.setText("欢迎您， " + user.getUserName());
                        mRGBText.setVisibility(View.GONE);
                    }
                } else {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    if (rgbLivenessScore < mRgbLiveScore) {
                        mDetectText.setText("活体检测未通过");
                        mDetectText.setVisibility(View.VISIBLE);
                        mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                        mRGBText.setVisibility(View.VISIBLE);
                        mRGBText.setText("FAIL");
                        mRGBText.setTextColor(Color.parseColor("#ac182a"));
                    } else {
                        mRGBText.setVisibility(View.VISIBLE);
                        mRGBText.setText("PASS");
                        mRGBText.setTextColor(Color.parseColor("#016838"));

                        User user = livenessModel.getUser();
                        if (user == null) {
                            mDetectText.setText("搜索不到用户");
                            mDetectText.setVisibility(View.VISIBLE);
                            mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                        } else {
                            String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                    + "/" + user.getImageName();
                            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                            mDetectImage.setImageBitmap(bitmap);
                            mDetectText.setText("欢迎您， " + user.getUserName());
                        }
                    }
                }
                mTvDetect.setText(String.format("检测 ：%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB活体 ：%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB得分 ：%s", livenessModel.getRgbLivenessScore()));
                mTvFeature.setText(String.format("特征抽取 ：%s ms", livenessModel.getFeatureDuration()));
                mTvAll.setText(String.format("检索比对 ：%s ms", livenessModel.getCheckDuration()));
                mTvAllTime.setText(String.format("总耗时 ：%s ms", livenessModel.getAllDetectDuration()));
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
                mAutoCameraPreviewView.removeAllViews();
                startActivity(new Intent(this, FaceRGBCloseDebugSearchActivity.class));
                finish();
                break;
            default:
                break;
        }
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
