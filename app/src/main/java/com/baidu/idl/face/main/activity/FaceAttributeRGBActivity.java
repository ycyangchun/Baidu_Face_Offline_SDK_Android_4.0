package com.baidu.idl.face.main.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.manager.FaceTrackManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

public class FaceAttributeRGBActivity extends BaseActivity {


    private AutoTexturePreviewView mPreviewView;
    private TextView textAttr;
    private TextView rgbLivenessScoreTv;
    private TextView rgbLivenssDurationTv;
    private TextView tipTv;
    private TextView tvDetectDuration;
    private ImageView imagePreview;

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int mWidth = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int mHeight = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    // textureView用于绘制人脸框等。
    private TextureView textureView;

    private RectF rectF;
    private Paint paint;

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[3];
    private boolean requestToInner = false;
    private TextView detectText;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_track);

        // 属性开启属性检测
        SingleBaseConfig.getBaseConfig().setAttribute(true);
        FaceSDKManager.getInstance().initConfig();

        findView();

    }

    private void findView() {
        btnBack = findViewById(R.id.btn_back);
        mPreviewView = findViewById(R.id.preview_view);
        textureView = findViewById(R.id.texture_view);
        textureView.setOpaque(false);
        // 不需要屏幕自动变黑。
        textureView.setKeepScreenOn(true);
        textAttr = findViewById(R.id.text_attr);
        rgbLivenessScoreTv = findViewById(R.id.text_rgb_liveness_score);
        rgbLivenssDurationTv = findViewById(R.id.text_rgb_livenss_duration);
        tipTv = findViewById(R.id.text_tip);
        tvDetectDuration = findViewById(R.id.text_face_detect_duration);

        imagePreview = findViewById(R.id.image_preview);

        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        detectText = findViewById(R.id.detect_text);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 摄像头图像预览
        startCameraPreview();
        Log.e("qing", "start camera");
    }

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
         CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
//         CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        CameraPreviewManager.getInstance().startPreview(this, mPreviewView, mWidth, mHeight, new CameraDataCallback() {
            @Override
            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                // 拿到相机帧数据
                faceDetect(data, width, height);

                // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                if (isRGBDisplay) {
                    showDetectImage(data);
                }
            }
        });
    }

    /**
     * 摄像头数据处理
     *
     * @param data
     * @param width
     * @param height
     */
    private void faceDetect(byte[] data, int width, int height) {

        // 无活体检测
        FaceTrackManager.getInstance().setAliving(false);
        FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                // 输出结果
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
            public void onTip(int code, final String msg) {
                displayTip(msg);
            }

            @Override
            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {

            }
        });


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
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, mHeight,
                mWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageBitmap(bitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
        // 关闭属性检测
        SingleBaseConfig.getBaseConfig().setAttribute(false);
        FaceSDKManager.getInstance().initConfig();
    }


    // 检测结果输出
    private void checkResult(LivenessModel model) {

        if (model == null) {
            clearTip();
            return;
        } else {
            displayTip("");
        }

        if (requestToInner) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearTip();
                    detectText.setText("预览区域内人脸不全");
                }
            });
            return;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    detectText.setText("");
                }
            });
        }

        displayResult(model, null);
        attrCheck(model);
    }

    /**
     * 人脸属性检测
     *
     * @param model
     */
    private void attrCheck(LivenessModel model) {
        final FaceInfo faceInfo = model.getFaceInfo();
        // todo 人脸属性数据获取
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textAttr.setText("人脸属性：" + getMsg(faceInfo));
            }
        });
    }

    public String getMsg(FaceInfo faceInfo) {
        StringBuilder msg = new StringBuilder();
        if (faceInfo != null) {
            msg.append(faceInfo.age);
            msg.append(",").append(faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_CALM ?
                    "平静"
                    : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_SMILE ? "笑"
                    : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_FROWN ? "皱眉" : "没有表情");
            msg.append(",").append(faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_FEMALE ? "女性" :
                    faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_MALE ? "男性" : "婴儿");
            msg.append(",").append(faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_NO_GLASSES ? "无眼镜"
                    : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_GLASSES ? "有眼镜"
                    : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_SUN_GLASSES ? "墨镜" : "太阳镜");
            msg.append(",").append(faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_YELLOW ? "黄种人"
                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_WHITE ? "白种人"
                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_BLACK ? "黑种人"
                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_INDIAN ? "印度人"
                    : "地球人");
        }
        return msg.toString();
    }

    private void displayResult(final LivenessModel livenessModel, final String livess) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livess != null && livess.equals("livess")) {
                    rgbLivenessScoreTv.setVisibility(View.VISIBLE);
                    rgbLivenssDurationTv.setVisibility(View.VISIBLE);
                    rgbLivenessScoreTv.setText("RGB活体得分：" + livenessModel.getRgbLivenessScore());
                    rgbLivenssDurationTv.setText("RGB活体耗时：" + livenessModel.getRgbLivenessDuration());
                } else {
                    rgbLivenessScoreTv.setVisibility(View.GONE);
                    rgbLivenssDurationTv.setVisibility(View.GONE);
                }
                tvDetectDuration.setText("人脸检测耗时：" + livenessModel.getRgbDetectDuration());
            }
        });
    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipTv.setText(tip);
            }
        });
    }

    private void clearTip() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDetectDuration.setText("");
                rgbLivenessScoreTv.setText("");
                rgbLivenssDurationTv.setText("");
                textAttr.setText("");
                if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                    detectText.setText("");
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
                Canvas canvas = textureView.lockCanvas();
                if (canvas == null) {
                    textureView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    textureView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    textureView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mPreviewView, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                textureView.unlockCanvasAndPost(canvas);
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
        FaceOnDrawTexturViewUtil.converttPointXY(pointXY, mPreviewView,
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
