package com.baidu.idl.face.main.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.PicoAutoTexturePreviewView;
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
import com.picozense.sdk.IFrameCallback;
import com.picozense.sdk.PsCamera;
import com.picozense.sdk.PsFrame;

public class PicoFaceAttributeRGBActivity extends BaseActivity {

    private static final String TAG = "PicoFaceAttrActivity";
    private PicoAutoTexturePreviewView mAutoCameraPreviewView;
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

    private PsCamera picoCamera;
    private final Object mySync = new Object();
    private Bitmap mBmpRGB;
    byte[] mByteBuffer_rgb = {0};
    int countNum = 0;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_track_pico);

        // 属性开启属性检测
        SingleBaseConfig.getBaseConfig().setAttribute(true);
        FaceSDKManager.getInstance().initConfig();

        findView();

        picoCamera = new PsCamera();
        if (picoCamera != null) {
            picoCamera.init(this, null);
        }
        mBmpRGB = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        mByteBuffer_rgb = new byte[640 * 480 * 3];
    }

    private void findView() {

        btnBack = findViewById(R.id.btn_back);
        mAutoCameraPreviewView = findViewById(R.id.preview_view);
        mAutoCameraPreviewView.setPreviewSize(mWidth, mHeight);
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
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (picoCamera != null) {
            picoCamera.destroy();
            picoCamera = null;
        }
        SingleBaseConfig.getBaseConfig().setAttribute(false);
        FaceSDKManager.getInstance().initConfig();
        super.onDestroy();
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
                // 展示model
                checkResult(livenessModel);
            }

            @Override
            public void onTip(int code, final String msg) {
                if (msg != null && !msg.isEmpty()) {
                    displayTip(msg);
                }
            }

            @Override
            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                // 绘制人脸框
                showFrame(livenessModel);
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
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(mBmpRGB);
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

    // 检测结果输出
    private void checkResult(LivenessModel model) {

        if (model == null) {
            clearTip();
            return;
        } else {
            displayTip("");
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
                Log.i(TAG, "人脸属性：" + getMsg(faceInfo));
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
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                textureView.unlockCanvasAndPost(canvas);
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (0 == msg.what) {
                mAutoCameraPreviewView.draw(mBmpRGB);

                // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                if (isRGBDisplay) {
                    imagePreview.setVisibility(View.VISIBLE);
                    imagePreview.setImageBitmap(mBmpRGB);
                }
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
                picoCamera.RgbToRgba(mByteBuffer_rgb, mBmpRGB, rgbFrame.width, rgbFrame.height);

                Message fwMsg = mHandler.obtainMessage();
                fwMsg.what = 0;
                mHandler.sendMessage(fwMsg);

                faceDetect(mByteBuffer_rgb, rgbFrame.width, rgbFrame.height);
            }
        }
    };

}
