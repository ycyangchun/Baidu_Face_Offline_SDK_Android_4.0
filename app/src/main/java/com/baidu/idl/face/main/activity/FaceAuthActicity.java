package com.baidu.idl.face.main.activity;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.utils.FileUitls;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.yc.patrol.App;

/**
 * 设备激活 （在线激活、离线激活）
 *
 * @Time: 2019/05/23
 * @Author: v_shishuaifeng
 * @Description:
 */
public class FaceAuthActicity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private TextView deviceId;
    private FaceAuth faceAuth;
    private int lastKeyLen = 0;
    private EditText etKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_auth);
        mContext = this;
        initView();

    }

    private void initView() {

        // 复制按钮
        faceAuth = new FaceAuth();
        TextView copyText = findViewById(R.id.copy_text);
        copyText.setOnClickListener(this);
        // 返回按钮
        Button btReturn = findViewById(R.id.bt_return);
        btReturn.setOnClickListener(this);
        // device id
        deviceId = findViewById(R.id.device_id);
        deviceId.setText(faceAuth.getDeviceId(this));
        // 输入序列码
        etKey = findViewById(R.id.et_key);
        etKey.setTransformationMethod(new AllCapTransformationMethod(true));
        addLisenter();

        final String licenseOnLineKey = PreferencesUtil.getString("activate_online_key", "");
        etKey.setText(licenseOnLineKey);

        // 在线激活按钮
        Button btOnLineActive = findViewById(R.id.bt_on_line_active);
        btOnLineActive.setOnClickListener(this);
        // 检查文件按钮
        Button btInspectSdcard = findViewById(R.id.bt_inspect_sdcard);
        btInspectSdcard.setOnClickListener(this);
        // 离线激活按钮
        Button btOffLineActive = findViewById(R.id.bt_off_line_active);
        btOffLineActive.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_return:
                finish();
                break;
            // 点击复制
            case R.id.copy_text:
                ClipboardManager clipboardManager = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(deviceId.getText());
                toast("deviceID 复制成功", this);
                break;
            // 在线激活
            case R.id.bt_on_line_active:
                initLicense(1);
                break;
            // 查看sdcard
            case R.id.bt_inspect_sdcard:
                String path = FileUitls.getSDPath();
                String sdCardDir = path + "/" + "License.zip";
                if (FileUitls.fileIsExists(sdCardDir)) {
                    toast("读取到License.zip文件，文件地址为：" + sdCardDir, this);
                } else {
                    toast("未查找到License.zip文件", this);
                }
                break;
            // 离线激活
            case R.id.bt_off_line_active:
                initLicense(2);
                break;
            default:
                break;
        }

    }

    private void initLicense(int num) {
        if (num == 1) {
            String key = etKey.getText().toString().trim().toUpperCase();
            if (TextUtils.isEmpty(key)) {
                Toast.makeText(this, "请输入激活序列号!", Toast.LENGTH_SHORT).show();
                return;
            }
            faceAuth.initLicenseOnLine(this, key, new Callback() {
                @Override
                public void onResponse(final int code, final String response) {
                    ToastUtils.toast(mContext, code + response);
                    if (code == 0) {
                        FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
                            @Override
                            public void initStart() {

                            }

                            @Override
                            public void initLicenseSuccess() {

                            }

                            @Override
                            public void initLicenseFail(int errorCode, String msg) {
                                ToastUtils.toast(mContext, errorCode + msg);

                            }

                            @Override
                            public void initModelSuccess() {

                            }

                            @Override
                            public void initModelFail(int errorCode, String msg) {

                            }
                        });
                        finish();
                    }
                }
            });
        } else {
            faceAuth.initLicenseOffLine(this, new Callback() {
                @Override
                public void onResponse(final int code, final String response) {
                    if (code == 0) {
                        ConfigUtils.modityJson();
                        // 属性开启属性检测
                        SingleBaseConfig.getBaseConfig().setAttribute(true);
                        FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
                            @Override
                            public void initStart() {

                            }

                            @Override
                            public void initLicenseSuccess() {

                            }

                            @Override
                            public void initLicenseFail(int errorCode, String msg) {
                                ToastUtils.toast(mContext, errorCode + msg);
                            }

                            @Override
                            public void initModelSuccess() {

                            }

                            @Override
                            public void initModelFail(int errorCode, String msg) {

                            }
                        });
                        finish();
                    } else {
                        ToastUtils.toast(mContext, response);
                    }

                }
            });
        }
    }

    private void addLisenter() {
        etKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 19) {
                    etKey.setText(s.toString().substring(0, 19));
                    etKey.setSelection(etKey.getText().length());
                    lastKeyLen = s.length();
                    return;
                }
                if (s.toString().length() < lastKeyLen) {
                    lastKeyLen = s.length();
                    return;
                }
                String text = s.toString().trim();
                if (etKey.getSelectionStart() < text.length()) {
                    return;
                }
                if (text.length() == 4 || text.length() == 9 || text.length() == 14) {
                    etKey.setText(text + "-");
                    etKey.setSelection(etKey.getText().length());
                }

                lastKeyLen = s.length();
            }
        });
    }

    public static class AllCapTransformationMethod extends ReplacementTransformationMethod {

        private char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private boolean allUpper = false;

        public AllCapTransformationMethod(boolean needUpper) {
            this.allUpper = needUpper;
        }

        @Override
        protected char[] getOriginal() {
            if (allUpper) {
                return lower;
            } else {
                return upper;
            }
        }

        @Override
        protected char[] getReplacement() {
            if (allUpper) {
                return upper;
            } else {
                return lower;
            }
        }
    }

    private void toast(final String text, final Context context) {

        Toast.makeText(context, text, Toast.LENGTH_LONG).show();


    }
}
