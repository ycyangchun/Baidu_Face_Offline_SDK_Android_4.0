package com.baidu.idl.face.main.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.FaceRoundView;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.ImageRegistrationMode;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;
import org.openni.android.OpenNIView;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;


/**
 * @Time: 2019/6/4
 * @Author: v_zhangxiaoqing01
 * @Description: RGB+Depth(深度)注册页面
 */


public class FaceDepthRegisterActivity extends BaseOrbbecActivity implements OpenNIHelper.DeviceOpenListener,
        ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {

    // 遮罩
    private FaceRoundView rectView;

    private static final int DEPTH_NEED_PERMISSION = 33;

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
    private AutoTexturePreviewView rgbTexture;
    // 显示Depth图
    private OpenNIView mDepthGLView;
    // RGB摄像头图像宽和高
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    // Depth摄像头图像宽和高
    private static final int DEPTH_WIDTH = SingleBaseConfig.getBaseConfig().getDepthWidth();
    private static final int DEPTH_HEIGHT = SingleBaseConfig.getBaseConfig().getDepthHeight();

    private boolean qualityControl;

    // 设备初始化状态标记
    private boolean initOk = false;
    // 摄像头驱动
    private Device mDevice;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream mDepthStream;

    private Object sync = new Object();
    // 循环取深度图像数据
    private boolean exit = false;

    // 当前摄像头类型
    private int cameraType;

    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] depthData;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_depth_register);

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
//        SingleBaseConfig.getBaseConfig().setQualityControl(true);
//        FaceSDKManager.getInstance().initConfig();

    }


    private void initView() {

        backButton = findViewById(R.id.id_reg_depth_back);
        setButton = findViewById(R.id.id_reg_depth_setting);
        backButton.setOnClickListener(this);
        setButton.setOnClickListener(this);

        // 双目摄像头RGB 图像预览
        rgbTexture = findViewById(R.id.rgb_pro_surface);
        rgbTexture.setVisibility(View.VISIBLE);

        // 深度摄像头数据回显
        mDepthGLView = findViewById(R.id.depth_reg_surface_view);
        // 调试模式打开 显示深度摄像头数据回显
        if (SingleBaseConfig.getBaseConfig().getNirOrDepth()) {
            mDepthGLView.setVisibility(View.VISIBLE);
        } else {
            mDepthGLView.setVisibility(View.INVISIBLE);
        }


        testImageView = findViewById(R.id.test_reg_depth_imgView);

        // 初始化 深度摄像头
        mOpenNIHelper = new OpenNIHelper(this);
        mOpenNIHelper.requestDeviceOpen(this);


        // 图像预览
        mTrackText = findViewById(R.id.track_txt);
        mDetectText = findViewById(R.id.detect_reg_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);


        // 注册页面 只支持 固定区域检测
        // 遮罩
//        rectView = findViewById(R.id.rect_view_depth);
//        rectView.setVisibility(View.VISIBLE);
//
//        DisplayMetrics dm = new DisplayMetrics();
//        Display display = this.getWindowManager().getDefaultDisplay();
//        display.getMetrics(dm);
//        int mDisplayWidth = dm.widthPixels;
//        int mDisplayHeight = dm.heightPixels;
//
//        int w = mDisplayWidth;
//        int h = mDisplayHeight;
//
//        FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
//                (int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO),
//                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//        rgbTexture.setLayoutParams(cameraFL);

        // 画人脸框
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        paint = new Paint();
        rectF = new RectF();
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 摄像头图像预览
        startCameraPreview();
    }


    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        CameraPreviewManager.getInstance().startPreview(this, rgbTexture,
                RGB_WIDTH, RGB_HEIGHT, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] rgbData, Camera camera, int srcWidth, int srcHeight) {
                        // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
                        boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                        if (isRGBDisplay) {
                            showDetectImage(rgbData);
                        }
                        dealRgb(rgbData);

                    }
                });
    }


    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();
    }


    /**
     * 在device 启动时候初始化USB 驱动
     *
     * @param device
     */
    private void initUsbDevice(UsbDevice device) {

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.isEmpty()) {
            Toast.makeText(this, " openni enumerateDevices 0 devices", Toast.LENGTH_LONG).show();
            return;
        }
        this.mDevice = null;
        // Find mDevice ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
//                if (cameraType == 1 && (device.getProductId() == 1555 || device.getProductId() == 1547
//                        || device.getProductId() == 1550)) { // pro =1 ROS1
//                    Toast.makeText(this, "当前模式跟镜头不匹配", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (cameraType == 4 && device.getProductId() == 1550) { // deeyea
//                    Toast.makeText(this, "当前模式跟镜头不匹配", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                this.mDevice = Device.open();
                break;
            }
        }

        if (this.mDevice == null) {
            Toast.makeText(this, " openni open devices failed: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
            return;
        }

    }


    @Override
    public void onDeviceOpened(UsbDevice device) {
        initUsbDevice(device);
        mDepthStream = VideoStream.create(this.mDevice, SensorType.DEPTH);
        if (mDepthStream != null) {
            List<VideoMode> mVideoModes = mDepthStream.getSensorInfo().getSupportedVideoModes();
            for (VideoMode mode : mVideoModes) {
                int x = mode.getResolutionX();
                int y = mode.getResolutionY();
//                if ((cameraType == 3 || cameraType == 4) && x == DEPTH_WIDTH && y == DEPTH_HEIGHT &&
//                        mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
//                    mDepthStream.setVideoMode(mode);
//                    this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
//                    break;
//                } else if (cameraType == 2 && x == DEPTH_HEIGHT && y == DEPTH_WIDTH &&
//                        mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) { // Atlas
//                    mDepthStream.setVideoMode(mode);
//                    this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
//                    break;
//                } else if (x == DEPTH_WIDTH && y == DEPTH_HEIGHT && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
//                    mDepthStream.setVideoMode(mode);
//                    break;
//                }
                if (cameraType == 2) {
                    if (x == DEPTH_HEIGHT && y == DEPTH_WIDTH && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                } else {
                    if (x == DEPTH_WIDTH && y == DEPTH_HEIGHT && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                }
            }
            startThread();
        }
    }


    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    @Override
    public void onDeviceNotFound() {

    }

    /**
     * 开启线程接收深度数据
     */
    private void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

                streams.add(mDepthStream);
                mDepthStream.start();
                while (!exit) {

                    try {
                        OpenNI.waitForAnyStream(streams, 2000);

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {
                        if (mDepthStream != null) {
                            mDepthGLView.update(mDepthStream);
                            VideoFrameRef videoFrameRef = mDepthStream.readFrame();
                            ByteBuffer depthByteBuf = videoFrameRef.getData();
                            if (depthByteBuf != null) {
                                int depthLen = depthByteBuf.remaining();
                                byte[] depthByte = new byte[depthLen];
                                depthByteBuf.get(depthByte);
                                dealDepth(depthByte);
                            }
                            videoFrameRef.release();
                        }
                    }
                }
            }
        };

        thread.start();
    }


    private void dealDepth(byte[] data) {
        depthData = data;
        checkData();
    }


    private synchronized void checkData() {
        if (rgbData != null && depthData != null) {
            FaceSDKManager.getInstance().onDetectCheck(rgbData, null, depthData, RGB_HEIGHT,
                    RGB_WIDTH, 4, 2, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 做距离的检测过滤
//                            boolean isFilterSuccess = faceSizeFilter(livenessModel.getFaceInfo(),
//                                    RGB_WIDTH, RGB_HEIGHT);
//                            if (isFilterSuccess) {
//                                // 展示model
//                                checkResult(livenessModel);
//                            }

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
        }
    }


    private void checkResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
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
    protected void onPause() {
        super.onPause();
        CameraPreviewManager.getInstance().stopPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v("gangzi", "onDestroy:");
        super.onDestroy();
        exit = true;
        if (initOk) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDepthStream != null) {
                mDepthStream.stop();
                mDepthStream.destroy();
                mDepthStream = null;
            }
            if (mDevice != null) {
                mDevice.close();
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
        }
        // 重置质检状态
        SingleBaseConfig.getBaseConfig().setQualityControl(qualityControl);
        FaceSDKManager.getInstance().initConfig();

        finish();
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
                RGB_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
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

                if (requestToInner) {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("预览区域内人脸不全");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectText.setText("");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.mipmap.ic_littleicon);
                    return;
                }

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
                            FaceDepthRegisterActivity.this.finish();
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
            FaceDepthRegisterActivity.this.finish();
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
