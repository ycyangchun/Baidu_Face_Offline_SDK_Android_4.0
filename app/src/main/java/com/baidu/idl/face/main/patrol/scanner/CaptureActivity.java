package com.baidu.idl.face.main.patrol.scanner;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.patrol.utils.CustomDialog2;
import com.baidu.idl.face.main.patrol.scanner.camera.CameraManager;
import com.baidu.idl.face.main.patrol.scanner.decode.CaptureActivityHandler;
import com.baidu.idl.face.main.patrol.scanner.view.ViewfinderBase;
import com.baidu.idl.face.main.patrol.scanner.view.ViewfinderView;
import com.baidu.idl.facesdkdemo.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;



/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a viewfinder to help the user place the barcode correctly, shows feedback as the
 * image processing is happening, and then overlays the results when a scan is successful.
 * <p>
 * 此Activity所做的事： 1.开启camera，在后台独立线程中完成扫描任务； 2.绘制了一个扫描区（viewfinder）来帮助用户将条码置于其中以准确扫描； 3.扫描成功后会将扫描结果展示在界面上。
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener , CustomDialog2.OnDialogClickListener{
    private LinearLayout capture_flashlight;
    private CustomDialog2 customDialog;
    private ImageView title_view_left;
    CaptureActivity _activity;
    /**
     * 是否有预览
     */
    private boolean hasSurface;

    /**
     * 活动监控器。如果手机没有连接电源线，那么当相机开启后如果一直处于不被使用状态则该服务会将当前activity关闭。 活动监控器全程监控扫描活跃状态，与CaptureActivity生命周期相同.每一次扫描过后都会重置该监控，即重新倒计时。
     */
    private InactivityTimer inactivityTimer;

    /**
     * 声音震动管理器。如果扫描成功后可以播放一段音频，也可以震动提醒，可以通过配置来决定扫描成功后的行为。
     */
    private BeepManager beepManager;

    /**
     * 闪光灯调节器。自动检测环境光线强弱并决定是否开启闪光灯
     */
    private AmbientLightManager ambientLightManager;

    public CameraManager cameraManager;
    /**
     * 扫描区域
     */
    private ViewfinderView viewfinderView;
    private SurfaceView surfaceView;
    private CaptureActivityHandler cahHandler;

    public Result lastResult;

    private boolean isFlashlightOpen;

    /**
     * 【辅助解码的参数(用作MultiFormatReader的参数)】 编码类型，该参数告诉扫描器采用何种编码方式解码，即EAN-13，QR Code等等 对应于DecodeHintType.POSSIBLE_FORMATS类型
     * 参考DecodeThread构造函数中如下代码：hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
     */
    private Collection<BarcodeFormat> decodeFormats;

    /**
     * 【辅助解码的参数(用作MultiFormatReader的参数)】 该参数最终会传入MultiFormatReader， 上面的decodeFormats和characterSet最终会先加入到decodeHints中 最终被设置到MultiFormatReader中
     * 参考DecodeHandler构造器中如下代码：multiFormatReader.setHints(hints);
     */
    private Map<DecodeHintType, ?> decodeHints;

    /**
     * 【辅助解码的参数(用作MultiFormatReader的参数)】 字符集，告诉扫描器该以何种字符集进行解码 对应于DecodeHintType.CHARACTER_SET类型 参考DecodeThread构造器如下代码：hints.put(DecodeHintType.CHARACTER_SET, characterSet);
     */
    private String characterSet;

    private Result savedResultToShow;

    public int source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());
        initView();
    }

    public int getLayoutID() {
        return R.layout.activity_homepage_capture;
    }

    public void initView() {
        _activity = this;
        title_view_left = (ImageView) findViewById(R.id.title_view_left);
        title_view_left.setOnClickListener(this);

        Window window = CaptureActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 监听图片识别按钮
        hasSurface = false;
        surfaceView = (SurfaceView) findViewById(R.id.capture_preview_view);
        capture_flashlight = (LinearLayout) findViewById(R.id.capture_flashlight);
        capture_flashlight.setOnClickListener(this);
    }

    private boolean isResume = true;
    private boolean openCamera = false;

    public void pauseCapture() {

        if (isResume == true) {
            return;
        }
        isResume = true;

        // System.out.println("活动开关 CaptureFragment******************************pauseCapture() hasSurface = " + hasSurface);
        if (cahHandler != null) {
            cahHandler.quitSynchronously();
            cahHandler = null;
        }

        inactivityTimer.onPause();
        // ambientLightManager.stop();
        beepManager.close();

        // 关闭摄像头

        if (openCamera) {
            openCamera = false;
            // System.out.println("活动开关 CaptureFragment Camera  closeDriver" + openCamera);
            cameraManager.closeDriver();
        }

        if (!hasSurface) {

            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    public void initCapture() {


        if (isResume == false) {
            return;
        }

        isResume = false;
        inactivityTimer = new InactivityTimer(CaptureActivity.this);
        beepManager = new BeepManager(CaptureActivity.this);
        ambientLightManager = new AmbientLightManager(CaptureActivity.this);

        // System.out.println("CaptureFragment******************************initCapture() hasSurface = " + hasSurface);
        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.

        // 相机初始化的动作需要开启相机并测量屏幕大小，这些操作
        // 不建议放到onCreate中，因为如果在onCreate中加上首次启动展示帮助信息的代码的 话，
        // 会导致扫描窗口的尺寸计算有误的bug
        cameraManager = new CameraManager(CaptureActivity.this.getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.capture_viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        cahHandler = null;
        lastResult = null;

        // 摄像头预览功能必须借助SurfaceView，因此也需要在一开始对其进行初始化
        // 如果需要了解SurfaceView的原理
        // 参考:http://blog.csdn.net/luoshengyang/article/details/8661317
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);

        } else {
            // 防止sdk8的设备初始化预览异常
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            // System.out.println("CaptureFragment******************************initCapture() addCallback = " + hasSurface);
            surfaceHolder.addCallback(this);
            initCamera(surfaceHolder);
        }

        // 加载声音配置，其实在BeemManager的构造器中也会调用该方法，即在onCreate的时候会调用一次
        beepManager.updatePrefs();

        // 启动闪光灯调节器
        ambientLightManager.start(cameraManager);

        // 恢复活动监控器
        inactivityTimer.onResume();

        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;

        restartPreviewAfterDelay(1000L);

    }
    private void setViewVisibility(int visibility) {
            findViewById(R.id.capture_preview_view).setVisibility(visibility);
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        // System.out.println("CaptureFragment******************************handleDecode() isCapture = " + isCapture);
        if (!isCapture) {// 不在扫描页面时，不进行扫描处理
            return;
        }

        // 重新计时
        inactivityTimer.onActivity();

        lastResult = rawResult;

        // 把图片画到扫描框
        viewfinderView.drawResultBitmap(barcode);
		beepManager.playBeepSoundAndVibrate();
        initPlayer(CaptureActivity.this, R.raw.beep);
        startPlayer(CaptureActivity.this, R.raw.beep);
        initCustomDialog();
        customDialog.setMessage("识别结果:" + ResultParser.parseResult(rawResult).toString())
                    .setData("0")
                    .setPositiveButton("知道了")
                    .show();
    }

    public static String getValueString(String str, String key, String def){
        String s = def;
        if(!TextUtils.isEmpty(str)){
            try {
                JSONObject jsonObject = new JSONObject(str);
                if(jsonObject.has(key)){
                    s = jsonObject.getString(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                return s;
            }
        }
        return s;
    }


    MediaPlayer mMediaPlayer;

    public void initPlayer(Activity activity, int resid) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = MediaPlayer.create(activity, resid);
    }

    public void startPlayerBF(Activity activity, int resid) {

        if (mMediaPlayer == null) {
            initPlayer(activity, resid);
        }
        // AudioManager mAudioManager = (AudioManager)
        // getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer.start();
    }

    public void startPlayer(Activity activity, int resid) {

        if (mMediaPlayer == null) {
            initPlayer(activity, resid);
        }
        // AudioManager mAudioManager = (AudioManager)
        // getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer.start();
    }

    public void startPlayer(Activity activity, int resid, int volume) {
        if (mMediaPlayer == null) {
            initPlayer(activity, resid);
        }
        AudioManager mAudioManager = (AudioManager) CaptureActivity.this.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, AudioManager.FLAG_ALLOW_RINGER_MODES);
        mMediaPlayer.start();
    }

    public boolean isPlay() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void stopPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }


    private void getUniAwarding(String awardCode) {
        System.out.println("chromium : =====>" + awardCode);
//        showToast("识别结果:" + awardCode);
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (cahHandler != null) {
            cahHandler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();

    }

    public ViewfinderBase getViewfinderView() {
        return viewfinderView;
    }

    public Handler getCahHandler() {
        return cahHandler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    private void resetStatusView() {
        if (viewfinderView != null) {
            viewfinderView.setVisibility(View.VISIBLE);
        }
        lastResult = null;
    }


    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }

        if (cameraManager.isOpen()) {
            // Log.w(TAG, "initCamera(surfaceHolder); initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {

            // System.out.println("Camera  openDriver" + openCamera);
            if (!openCamera) {
                openCamera = true;
                cameraManager.openDriver(surfaceHolder);
            }

            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (cahHandler == null) {
                cahHandler = new CaptureActivityHandler(CaptureActivity.this, decodeFormats, decodeHints, characterSet, cameraManager);
            }

            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            // Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            // Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }


    private boolean isCapture = false;

    /**
     * 向CaptureActivityHandler中发送消息，并展示扫描到的图像
     *
     * @param bitmap
     * @param result
     */
    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon

        if (cahHandler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(cahHandler, R.id.decode_succeeded, savedResultToShow);
                cahHandler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        initCustomDialog();
        customDialog.setMessage("无法启动相机，请在设置或安全中心>权限管理>应用权限管理>相机中允许开奖管理访问相机").setPositiveButton("知道了").show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            // Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        // System.out.println("CaptureFragment******************************surfaceCreated() hasSurface = " + hasSurface);
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // System.out.println("CaptureFragment******************************onDestroy() ");
        if (inactivityTimer != null) {
            inactivityTimer.shutdown();
        }


    }
    @Override
    public void onResume() {
        super.onResume();
        isCapture = true;
        setViewVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable(){
            public void run() {
                initCapture();
            }
        }, 1000);

    }

    @Override
    public void onPause() {
        super.onPause();
        isCapture = false;
        setViewVisibility(View.GONE);
        pauseCapture();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_view_left:
                finish();
                break;
            case R.id.capture_flashlight:
                if (cameraManager != null && cameraManager.isOpen()) {
                    if (isFlashlightOpen) {
                        cameraManager.setTorch(false); // 关闭闪光灯
                        isFlashlightOpen = false;
                        findViewById(R.id.capture_flashlight).setSelected(false);
                    } else {
                        cameraManager.setTorch(true); // 打开闪光灯
                        isFlashlightOpen = true;
                        findViewById(R.id.capture_flashlight).setSelected(true);
                    }
                }
                break;
        }
    }




    private void initCustomDialog() {
        if (customDialog == null) {
            customDialog = new CustomDialog2(this);
            customDialog.setOnDialogClickListener(this);
        }
    }

    @Override
    public void OnDialogClickCallBack(boolean isPositive, Object obj) {
        if (isPositive) {
            String mFlag = (String) obj;
            if ("0".equals(mFlag)) {
                restartPreviewAfterDelay(0L);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog, Object obj) {

    }

}