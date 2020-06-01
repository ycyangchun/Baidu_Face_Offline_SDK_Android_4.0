package com.baidu.idl.face.main.activity.setting;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.facesdkdemo.R;

import java.math.BigDecimal;


/**
 * author : shangrong
 * date : 2019/5/27 6:43 PM
 * description :质量参数控制
 */
public class QualityControlActivity extends BaseActivity {

    private EditText qcGestureEtAmount;
    private EditText qcIlluminiationEtAmount;
    private EditText qcBlurEtAmount;
    private EditText qcOcclusionetAmount;
    private EditText qcOcclusionLeftEyeEtAmount;
    private EditText qcOcclusionRightEyeEtAmount;
    private EditText qcOcclusionLeftCheekEtAmount;
    private EditText qcOcclusionRightCheekEtAmount;
    private EditText qcOcclusionNoseEtAmount;
    private EditText qcOcclusionMouthEtAmount;
    private EditText qcOcclusionChinEtAmount;

    private float gestureValue;
    private int illuminiationValue;
    private float occlusionValue;
    private float blurValue;
    private float occlusionLeftEye;
    private float occlusionRightEye;
    private float occlusionLeftCheek;
    private float occlusionRightCheek;
    private float occlusionNose;
    private float occlusionMouth;
    private float occulusionChin;


    private BigDecimal gestureDecimal;
    private BigDecimal occlusionDecimal;
    private BigDecimal blurDecimal;
    private BigDecimal occlusionLeftEyeDecimal;
    private BigDecimal occlusionRightEyeDecimal;
    private BigDecimal occlusionLeftCheekDecimal;
    private BigDecimal occlusionRightCheekDecimal;
    private BigDecimal occlusionNoseDecimal;
    private BigDecimal occulusionChinDecimal;

    private BigDecimal obNonmoralValue;
    private BigDecimal gestureNormalValue;

    private Switch qcisbegin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qualitycontrol);

        init();
    }

    public void init() {
        Button qcGestureDecrease = findViewById(R.id.qc_gesture_Decrease);
        qcGestureEtAmount = findViewById(R.id.qc_gesture_etAmount);
        Button qcGestureIncrease = findViewById(R.id.qc_gesture_Increase);

        Button qcIlluminiationDecrease = findViewById(R.id.qc_illuminiation_Decrease);
        qcIlluminiationEtAmount = findViewById(R.id.qc_illuminiation_etAmount);
        Button qcIlluminiationIncrease = findViewById(R.id.qc_illuminiation_Increase);

        Button qcBlurDecrease = findViewById(R.id.qc_blur_Decrease);
        qcBlurEtAmount = findViewById(R.id.qc_blur_etAmount);
        Button qcBlurIncrease = findViewById(R.id.qc_blur_Increase);

        Button qcOcclusionDecrease = findViewById(R.id.qc_occlusion_Decrease);
        qcOcclusionetAmount = findViewById(R.id.qc_occlusion_etAmount);
        Button qcOcclusionIncrease = findViewById(R.id.qc_occlusion_Increase);

        Button qcOcclusionLeftEyeDecrease = findViewById(R.id.qc_occlusion_lefteye_Decrease);
        qcOcclusionLeftEyeEtAmount = findViewById(R.id.qc_occlusion_lefteye_etAmount);
        Button qcOcclusionLeftEyeIncrease = findViewById(R.id.qc_occlusion_lefteye_Increase);

        Button qcOcclusionRightEyeDecrease = findViewById(R.id.qc_occlusion_righteye_Decrease);
        qcOcclusionRightEyeEtAmount = findViewById(R.id.qc_occlusion_righteye_etAmount);
        Button qcOcclusionRightEyeIncrease = findViewById(R.id.qc_occlusion_righteye_Increase);

        Button qcOcclusionLeftCheekDecrease = findViewById(R.id.qc_occlusion_leftcheek_Decrease);
        qcOcclusionLeftCheekEtAmount = findViewById(R.id.qc_occlusion_leftcheek_etAmount);
        Button qcOcclusionLeftCheekIncrease = findViewById(R.id.qc_occlusion_leftcheek_Increase);

        Button qcOcclusioRightCheekDecrease = findViewById(R.id.qc_occlusion_rightcheek_Decrease);
        qcOcclusionRightCheekEtAmount = findViewById(R.id.qc_occlusion_rightcheek_etAmount);
        Button qcOcclusionRightCheekIncrease = findViewById(R.id.qc_occlusion_rightcheek_Increase);

        Button qcOcclusionNoseDecrease = findViewById(R.id.qc_occlusion_nose_Decrease);
        qcOcclusionNoseEtAmount = findViewById(R.id.qc_occlusion_nose_etAmount);
        Button qcOcclusionNoseIncrease = findViewById(R.id.qc_occlusion_nose_Increase);

        Button qcOcclusionMouthDecrease = findViewById(R.id.qc_occlusion_mouth_Decrease);
        qcOcclusionMouthEtAmount = findViewById(R.id.qc_occlusion_mouth_etAmount);
        Button qcOcclusionMouthIncrease = findViewById(R.id.qc_occlusion_mouth_Increase);

        Button qcOcclusionChinDecrease = findViewById(R.id.qc_occlusion_chin_Decrease);
        qcOcclusionChinEtAmount = findViewById(R.id.qc_occlusion_chin_etAmount);
        Button qcOcclusionChinIncrease = findViewById(R.id.qc_occlusion_chin_Increase);


        qcisbegin = findViewById(R.id.qc_isbegin);

        Button fdaSave = findViewById(R.id.fda_save);

        gestureValue = SingleBaseConfig.getBaseConfig().getGesture();
        illuminiationValue = SingleBaseConfig.getBaseConfig().getIllumination();
        blurValue = SingleBaseConfig.getBaseConfig().getBlur();
        occlusionValue = SingleBaseConfig.getBaseConfig().getOcclusion();
        occlusionLeftEye = SingleBaseConfig.getBaseConfig().getLeftEye();
        occlusionRightEye = SingleBaseConfig.getBaseConfig().getRightEye();
        occlusionLeftCheek = SingleBaseConfig.getBaseConfig().getLeftCheek();
        occlusionRightCheek = SingleBaseConfig.getBaseConfig().getRightCheek();
        occlusionNose = SingleBaseConfig.getBaseConfig().getNose();
        occlusionMouth = SingleBaseConfig.getBaseConfig().getMouth();
        occulusionChin = SingleBaseConfig.getBaseConfig().getChinContour();


        if (SingleBaseConfig.getBaseConfig().isQualityControl()) {
            qcisbegin.setChecked(true);
        } else {
            qcisbegin.setChecked(false);
        }

        qcGestureEtAmount.setText((int) gestureValue + "");
        qcIlluminiationEtAmount.setText(+illuminiationValue + "");
        qcOcclusionetAmount.setText(occlusionValue + "");
        qcBlurEtAmount.setText(blurValue + "");
        qcOcclusionLeftEyeEtAmount.setText(occlusionLeftEye + "");
        qcOcclusionRightEyeEtAmount.setText(occlusionRightEye + "");
        qcOcclusionLeftCheekEtAmount.setText(occlusionLeftCheek + "");
        qcOcclusionRightCheekEtAmount.setText(occlusionRightCheek + "");
        qcOcclusionNoseEtAmount.setText(occlusionNose + "");
        qcOcclusionMouthEtAmount.setText(occlusionMouth + "");
        qcOcclusionChinEtAmount.setText(occulusionChin + "");


        obNonmoralValue = new BigDecimal(0.1 + "");
        gestureNormalValue = new BigDecimal(1 + "");

        qcGestureDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gestureValue <= 90 && gestureValue > 0) {
                    gestureDecimal = new BigDecimal(gestureValue + "");
                    gestureValue = gestureDecimal.subtract(gestureNormalValue).floatValue();
                    qcGestureEtAmount.setText((int) gestureValue + "");
                }
            }
        });

        qcGestureIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gestureValue < 90 && gestureValue >= 0) {
                    gestureDecimal = new BigDecimal(gestureValue + "");
                    gestureValue = gestureDecimal.add(gestureNormalValue).floatValue();
                    qcGestureEtAmount.setText((int) gestureValue + "");
                }
            }
        });

        qcIlluminiationDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (illuminiationValue > 0 && illuminiationValue <= 255) {
                    illuminiationValue = illuminiationValue - 5;
                    qcIlluminiationEtAmount.setText(illuminiationValue + "");
                }
            }
        });

        qcIlluminiationIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (illuminiationValue >= 0 && illuminiationValue < 255) {
                    illuminiationValue = illuminiationValue + 5;
                    qcIlluminiationEtAmount.setText(illuminiationValue + "");
                }
            }
        });

        qcOcclusionDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionValue > 0 && occlusionValue <= 1) {
                    occlusionDecimal = new BigDecimal(occlusionValue + "");
                    occlusionValue = occlusionDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionetAmount.setText(occlusionValue + "");
                }
            }
        });

        qcOcclusionIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionValue >= 0 && occlusionValue < 1) {
                    occlusionDecimal = new BigDecimal(occlusionValue + "");
                    occlusionValue = occlusionDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionetAmount.setText(occlusionValue + "");
                }
            }
        });

        qcBlurDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blurValue > 0f && blurValue <= 1f) {
                    blurDecimal = new BigDecimal(blurValue + "");
                    blurValue = blurDecimal.subtract(obNonmoralValue).floatValue();
                    qcBlurEtAmount.setText(blurValue + "");
                }
            }
        });

        qcBlurIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blurValue >= 0f && blurValue < 1f) {
                    blurDecimal = new BigDecimal(blurValue + "");
                    blurValue = blurDecimal.add(obNonmoralValue).floatValue();
                    qcBlurEtAmount.setText(blurValue + "");
                }
            }
        });

        qcOcclusionLeftEyeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionLeftEye > 0f && occlusionLeftEye <= 1f) {
                    occlusionLeftEyeDecimal = new BigDecimal(occlusionLeftEye + "");
                    occlusionLeftEye = occlusionLeftEyeDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionLeftEyeEtAmount.setText(occlusionLeftEye + "");
                }
            }
        });

        qcOcclusionLeftEyeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionLeftEye >= 0f && occlusionLeftEye < 1f) {
                    occlusionLeftEyeDecimal = new BigDecimal(occlusionLeftEye + "");
                    occlusionLeftEye = occlusionLeftEyeDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionLeftEyeEtAmount.setText(occlusionLeftEye + "");
                }
            }
        });

        qcOcclusionRightEyeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionRightEye > 0f && occlusionRightEye <= 1f) {
                    occlusionRightEyeDecimal = new BigDecimal(occlusionRightEye + "");
                    occlusionRightEye = occlusionRightEyeDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionRightEyeEtAmount.setText(occlusionRightEye + "");
                }
            }
        });

        qcOcclusionRightEyeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionRightEye >= 0f && occlusionRightEye < 1f) {
                    occlusionRightEyeDecimal = new BigDecimal(occlusionRightEye + "");
                    occlusionRightEye = occlusionRightEyeDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionRightEyeEtAmount.setText(occlusionRightEye + "");
                }
            }
        });


        qcOcclusionLeftCheekDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionLeftCheek > 0f && occlusionLeftCheek <= 1f) {
                    occlusionLeftCheekDecimal = new BigDecimal(occlusionLeftCheek + "");
                    occlusionLeftCheek = occlusionLeftCheekDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionLeftCheekEtAmount.setText(occlusionLeftCheek + "");
                }
            }
        });

        qcOcclusionLeftCheekIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionLeftCheek >= 0f && occlusionLeftCheek < 1f) {
                    occlusionLeftCheekDecimal = new BigDecimal(occlusionLeftCheek + "");
                    occlusionLeftCheek = occlusionLeftCheekDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionLeftCheekEtAmount.setText(occlusionLeftCheek + "");
                }
            }
        });

        qcOcclusioRightCheekDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionRightCheek > 0f && occlusionRightCheek <= 1f) {
                    occlusionRightCheekDecimal = new BigDecimal(occlusionRightCheek + "");
                    occlusionRightCheek = occlusionRightCheekDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionRightCheekEtAmount.setText(occlusionRightCheek + "");
                }
            }
        });

        qcOcclusionRightCheekIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionRightCheek >= 0f && occlusionRightCheek < 1f) {
                    occlusionRightCheekDecimal = new BigDecimal(occlusionRightCheek + "");
                    occlusionRightCheek = occlusionRightCheekDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionRightCheekEtAmount.setText(occlusionRightCheek + "");
                }
            }
        });

        qcOcclusionNoseDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionNose > 0f && occlusionNose <= 1f) {
                    occlusionNoseDecimal = new BigDecimal(occlusionNose + "");
                    occlusionNose = occlusionNoseDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionNoseEtAmount.setText(occlusionNose + "");
                }
            }
        });

        qcOcclusionNoseIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionNose >= 0f && occlusionNose < 1f) {
                    occlusionNoseDecimal = new BigDecimal(occlusionNose + "");
                    occlusionNose = occlusionNoseDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionNoseEtAmount.setText(occlusionNose + "");
                }
            }
        });

        qcOcclusionMouthDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionMouth > 0f && occlusionMouth <= 1f) {
                    occlusionNoseDecimal = new BigDecimal(occlusionMouth + "");
                    occlusionMouth = occlusionNoseDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionMouthEtAmount.setText(occlusionMouth + "");
                }
            }
        });

        qcOcclusionMouthIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occlusionMouth >= 0f && occlusionMouth < 1f) {
                    occlusionNoseDecimal = new BigDecimal(occlusionMouth + "");
                    occlusionMouth = occlusionNoseDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionMouthEtAmount.setText(occlusionMouth + "");
                }
            }
        });

        qcOcclusionChinDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occulusionChin > 0f && occulusionChin <= 1f) {
                    occulusionChinDecimal = new BigDecimal(occulusionChin + "");
                    occulusionChin = occulusionChinDecimal.subtract(obNonmoralValue).floatValue();
                    qcOcclusionChinEtAmount.setText(occulusionChin + "");
                }
            }
        });

        qcOcclusionChinIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occulusionChin >= 0f && occulusionChin < 1f) {
                    occulusionChinDecimal = new BigDecimal(occulusionChin + "");
                    occulusionChin = occulusionChinDecimal.add(obNonmoralValue).floatValue();
                    qcOcclusionChinEtAmount.setText(occulusionChin + "");
                }
            }
        });


        fdaSave.setOnClickListener(saveListener);

    }

    public View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SingleBaseConfig.getBaseConfig().setGesture
                    (Float.parseFloat(qcGestureEtAmount.getText().toString()));
            // 保存根据gesture 设置的roll、yaw、pitch 三个角度值，实际使用的的是这三个分量；
            SingleBaseConfig.getBaseConfig().setYaw
                    (Float.parseFloat(qcGestureEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setRoll
                    (Float.parseFloat(qcGestureEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setPitch
                    (Float.parseFloat(qcGestureEtAmount.getText().toString()));

            SingleBaseConfig.getBaseConfig().setIllumination
                    (Integer.parseInt(qcIlluminiationEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setOcclusion
                    (Float.parseFloat(qcOcclusionetAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setBlur
                    (Float.parseFloat(qcBlurEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setLeftEye
                    (Float.parseFloat(qcOcclusionLeftEyeEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setRightEye
                    (Float.parseFloat(qcOcclusionRightEyeEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setLeftCheek
                    (Float.parseFloat(qcOcclusionLeftCheekEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setRightCheek
                    (Float.parseFloat(qcOcclusionRightCheekEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setNose
                    (Float.parseFloat(qcOcclusionNoseEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setMouth
                    (Float.parseFloat(qcOcclusionMouthEtAmount.getText().toString()));
            SingleBaseConfig.getBaseConfig().setChinContour
                    (Float.parseFloat(qcOcclusionChinEtAmount.getText().toString()));

            if (qcisbegin.isChecked()) {
                SingleBaseConfig.getBaseConfig().setQualityControl(true);
                FaceSDKManager.getInstance().initConfig();
            } else {
                SingleBaseConfig.getBaseConfig().setQualityControl(false);
                FaceSDKManager.getInstance().initConfig();
            }
            ConfigUtils.modityJson();
            finish();
        }
    };


}
