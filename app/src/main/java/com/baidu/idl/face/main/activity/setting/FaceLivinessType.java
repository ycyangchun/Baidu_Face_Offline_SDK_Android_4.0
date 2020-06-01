package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.facesdkdemo.R;

/**
 * author : shangrong
 * date : two019/five/two7 six:four8 PM
 * description :活体检测模式
 */
public class FaceLivinessType extends BaseActivity {


    private RadioGroup flsCameraType;
    private RadioButton fltZero;
    private RadioButton fltOne;
    private RadioButton fltTwo;
    private RadioButton fltThree;
    private RadioButton fltFour;
    private RadioButton fltFive;
    private RadioButton fltSix;
    private int liveTypeValue;
    private int cameraTypeValue;

    // 0:奥比中光Astra Mini、Mini S系列(结构光)
    private static final int zero = 0;
    // 1:奥比中光 Astra Pro 、Pro S 、蝴蝶（结构光）
    private static final int one = 1;
    // 2:奥比中光Atlas（结构光）
    private static final int two = 2;
    // 3:奥比中光大白、海燕(结构光)
    private static final int three = 3;
    // 4:奥比中光Deeyea(结构光)
    private static final int four = 4;
    // 5:华捷艾米A100S、A200(结构光)
    private static final int five = 5;
    // 6:Pico DCAM710(ToF)
    private static final int six = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facelivinesstype);

        init();

    }

    public void init() {
        RadioGroup flsLiveType = findViewById(R.id.fls_live_type);
        RadioButton flsNoLive = findViewById(R.id.fls_no_live);
        RadioButton flsRgbLive = findViewById(R.id.fls_rgb_live);
        RadioButton flsRgbAndNirLive = findViewById(R.id.fls_rgbandnir_live);
        RadioButton flsRgbAndDepthLive = findViewById(R.id.fls_rgbanddepth_live);
        fltZero = findViewById(R.id.flt_zero);
        fltOne = findViewById(R.id.flt_one);
        fltTwo = findViewById(R.id.flt_two);
        fltThree = findViewById(R.id.flt_three);
        fltFour = findViewById(R.id.flt_four);
        fltFive = findViewById(R.id.flt_five);
        fltSix = findViewById(R.id.flt_six);
        flsCameraType = findViewById(R.id.fls_camera_type);
        Button flsSave = findViewById(R.id.fls_save);

        flsLiveType.setOnCheckedChangeListener(liveType);
        flsCameraType.setOnCheckedChangeListener(cameraType);

        liveTypeValue = SingleBaseConfig.getBaseConfig().getType();
        cameraTypeValue = SingleBaseConfig.getBaseConfig().getCameraType();

        if (liveTypeValue == one) {
            flsNoLive.setChecked(true);
            flsCameraType.setVisibility(View.GONE);
        }
        if (liveTypeValue == two) {
            flsRgbLive.setChecked(true);
            flsCameraType.setVisibility(View.GONE);
        }
        if (liveTypeValue == three) {
            flsRgbAndNirLive.setChecked(true);
            flsCameraType.setVisibility(View.GONE);
        }
        if (liveTypeValue == four) {
            flsRgbAndDepthLive.setChecked(true);
            flsCameraType.setVisibility(View.VISIBLE);
            setlectCamera();
        }


        flsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                justify();
                ConfigUtils.modityJson();
                finish();
            }
        });

    }


    public RadioGroup.OnCheckedChangeListener liveType = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getCheckedRadioButtonId()) {
                case R.id.fls_no_live:
                    liveTypeValue = one;
                    flsCameraType.setVisibility(View.GONE);
                    break;
                case R.id.fls_rgb_live:
                    liveTypeValue = two;
                    flsCameraType.setVisibility(View.GONE);
                    break;
                case R.id.fls_rgbandnir_live:
                    liveTypeValue = three;
                    flsCameraType.setVisibility(View.GONE);
                    break;
                case R.id.fls_rgbanddepth_live:
                    liveTypeValue = four;
                    flsCameraType.setVisibility(View.VISIBLE);
                    setlectCamera();
                    break;
                default:
                    break;
            }
        }
    };

    public RadioGroup.OnCheckedChangeListener cameraType = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getCheckedRadioButtonId()) {
                case R.id.flt_zero:
                    cameraTypeValue = zero;
                    break;
                case R.id.flt_one:
                    cameraTypeValue = one;
                    break;
                case R.id.flt_two:
                    cameraTypeValue = two;
                    break;
                case R.id.flt_three:
                    cameraTypeValue = three;
                    break;
                case R.id.flt_four:
                    cameraTypeValue = four;
                    break;
                case R.id.flt_five:
                    cameraTypeValue = five;
                    break;
                case R.id.flt_six:
                    cameraTypeValue = six;
                    break;
                default:
                    break;
            }
        }
    };


    public void setlectCamera() {
        if (cameraTypeValue == zero) {
            fltZero.setChecked(true);
        }
        if (cameraTypeValue == one) {
            fltOne.setChecked(true);
        }
        if (cameraTypeValue == two) {
            fltTwo.setChecked(true);
        }
        if (cameraTypeValue == three) {
            fltThree.setChecked(true);
        }
        if (cameraTypeValue == four) {
            fltFour.setChecked(true);
        }
        if (cameraTypeValue == five) {
            fltFive.setChecked(true);
        }
        if (cameraTypeValue == six) {
            fltSix.setChecked(true);
        }
    }

    public void justify() {
        if (liveTypeValue == one) {
            SingleBaseConfig.getBaseConfig().setType(one);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (liveTypeValue == two) {
            SingleBaseConfig.getBaseConfig().setType(two);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (liveTypeValue == three) {
            SingleBaseConfig.getBaseConfig().setType(three);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (liveTypeValue == four) {
            SingleBaseConfig.getBaseConfig().setType(four);
        }

        cameraSelect();
    }

    /**
     * Pico摄像头因为适配屏幕需要，所以width和height的值相互对调赋予
     */
    public void cameraSelect() {
        if (cameraTypeValue == zero) {
            SingleBaseConfig.getBaseConfig().setCameraType(zero);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
        }
        if (cameraTypeValue == one) {
            SingleBaseConfig.getBaseConfig().setCameraType(one);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
        }
        if (cameraTypeValue == two) {
            SingleBaseConfig.getBaseConfig().setCameraType(two);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
        }
        if (cameraTypeValue == three) {
            SingleBaseConfig.getBaseConfig().setCameraType(three);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
        }
        if (cameraTypeValue == four) {
            SingleBaseConfig.getBaseConfig().setCameraType(four);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
        }
        if (cameraTypeValue == five) {
            SingleBaseConfig.getBaseConfig().setCameraType(five);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
        }
        if (cameraTypeValue == six) {
            SingleBaseConfig.getBaseConfig().setCameraType(six);

            // Pico摄像头因为适配屏幕需要，所以width和height的值相互对调赋予
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(480);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(640);
            SingleBaseConfig.getBaseConfig().setDepthWidth(480);
            SingleBaseConfig.getBaseConfig().setDepthHeight(640);
        }
    }
}
