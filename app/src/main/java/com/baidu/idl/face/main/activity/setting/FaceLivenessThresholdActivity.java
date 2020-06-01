package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.facesdkdemo.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;


/**
 * author : shangrong
 * date : 2019/5/27 6:49 PM
 * description :活体阈值
 */
public class FaceLivenessThresholdActivity extends BaseActivity {
    private Button mfRgbDecrease;
    private Button mfRgbIncrease;
    private Button mfNirDecrease;
    private Button mfNirIncrease;
    private Button mfDepthDecrease;
    private Button mfDepthIncrease;
    private EditText mfRgbEtAmount;
    private EditText mfNirEtAmount;
    private EditText mfDepthEtAmount;
    private float rgbInitValue;
    private float nirInitValue;
    private float depthInitValue;
    private Button mfflssave;
    private BigDecimal rgbDecimal;
    private BigDecimal nirDecimal;
    private BigDecimal depthDecimal;
    private BigDecimal nonmoralValue;
    private static final float templeValue = 0.01f;

    private int zero = 0;
    private int one = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facelivenessthreshold);

        init();
    }

    public void init() {

        mfRgbDecrease = findViewById(R.id.mf_rgb_Decrease);
        mfRgbIncrease = findViewById(R.id.mf_rgb_Increase);
        mfNirDecrease = findViewById(R.id.mf_nir_Decrease);
        mfNirIncrease = findViewById(R.id.mf_nir_Increase);
        mfDepthDecrease = findViewById(R.id.mf_depth_Decrease);
        mfDepthIncrease = findViewById(R.id.mf_depth_Increase);

        mfRgbEtAmount = findViewById(R.id.mf_rgb_etAmount);
        mfNirEtAmount = findViewById(R.id.mf_nir_etAmount);
        mfDepthEtAmount = findViewById(R.id.mf_depth_etAmount);

        mfflssave = findViewById(R.id.mf_fls_save);

        rgbInitValue = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        nirInitValue = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        depthInitValue = SingleBaseConfig.getBaseConfig().getDepthLiveScore();

        nonmoralValue = new BigDecimal(templeValue + "");

        mfRgbEtAmount.setText(roundByScale(rgbInitValue));
        mfNirEtAmount.setText(roundByScale(nirInitValue));
        mfDepthEtAmount.setText(roundByScale(depthInitValue));

        clickListener();
    }

    public void clickListener() {
        mfRgbDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rgbInitValue > zero && rgbInitValue <= one) {
                    rgbDecimal = new BigDecimal(rgbInitValue + "");
                    rgbInitValue = rgbDecimal.subtract(nonmoralValue).floatValue();
                    mfRgbEtAmount.setText(roundByScale(rgbInitValue));
                }
            }
        });

        mfRgbIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rgbInitValue >= zero && rgbInitValue < one) {
                    rgbDecimal = new BigDecimal(rgbInitValue + "");
                    rgbInitValue = rgbDecimal.add(nonmoralValue).floatValue();
                    mfRgbEtAmount.setText(roundByScale(rgbInitValue));
                }
            }
        });

        mfNirDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nirInitValue > zero && nirInitValue <= one) {
                    nirDecimal = new BigDecimal(nirInitValue + "");
                    nirInitValue = nirDecimal.subtract(nonmoralValue).floatValue();
                    mfNirEtAmount.setText(roundByScale(nirInitValue));
                }
            }
        });

        mfNirIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nirInitValue >= zero && nirInitValue < one) {
                    nirDecimal = new BigDecimal(nirInitValue + "");
                    nirInitValue = nirDecimal.add(nonmoralValue).floatValue();
                    mfNirEtAmount.setText(roundByScale(nirInitValue));
                }
            }
        });

        mfDepthDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (depthInitValue > zero && depthInitValue <= one) {
                    depthDecimal = new BigDecimal(depthInitValue + "");
                    depthInitValue = depthDecimal.subtract(nonmoralValue).floatValue();
                    mfDepthEtAmount.setText(roundByScale(depthInitValue));
                }
            }
        });


        mfDepthIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (depthInitValue >= zero && depthInitValue < one) {
                    depthDecimal = new BigDecimal(depthInitValue + "");
                    depthInitValue = depthDecimal.add(nonmoralValue).floatValue();
                    mfDepthEtAmount.setText(roundByScale(depthInitValue));
                }
            }
        });

        mfflssave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleBaseConfig.getBaseConfig().setRgbLiveScore(Float.parseFloat(mfRgbEtAmount.getText().toString()));
                SingleBaseConfig.getBaseConfig().setNirLiveScore(Float.parseFloat(mfNirEtAmount.getText().toString()));
                SingleBaseConfig.getBaseConfig().
                        setDepthLiveScore(Float.parseFloat(mfDepthEtAmount.getText().toString()));
                ConfigUtils.modityJson();
                finish();
            }
        });
    }

    public static String roundByScale(float numberValue) {
        // 构造方法的字符格式这里如果小数不足2位,会以0补足.
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        // format 返回的是字符串
        String resultNumber = decimalFormat.format(numberValue);
        return resultNumber;
    }

}
