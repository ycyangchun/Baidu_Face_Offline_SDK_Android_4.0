package com.baidu.idl.face.main.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.LogUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.yc.patrol.utils.Tools;

import java.io.File;
import java.util.ArrayList;

public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 将activity设置为全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestPermissions(99);
    }

    // 请求权限
    public void requestPermissions(int requestCode) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                ArrayList<String> requestPerssionArr = new ArrayList<>();
                int hasCamrea = checkSelfPermission(Manifest.permission.CAMERA);
                if (hasCamrea != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.CAMERA);
                }

                int hasSdcardRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                if (hasSdcardRead != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                int hasSdcardWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasSdcardWrite != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                // 是否应该显示权限请求
                if (requestPerssionArr.size() >= 1) {
                    String[] requestArray = new String[requestPerssionArr.size()];
                    for (int i = 0; i < requestArray.length; i++) {
                        requestArray[i] = requestPerssionArr.get(i);
                    }
                    requestPermissions(requestArray, requestCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        boolean flag = false;
        for (int i = 0; i < permissions.length; i++) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                flag = true;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//            return true;
//        } else {
//            return super.dispatchKeyEvent(event);
//        }
        return super.dispatchKeyEvent(event);
    }

    public void batchImport() {
        // 获取导入目录 /sdcard/Face-Import
        String xml = "PatrolPlan.xml";
        // 判断Face.zip是否存在
        File zipFile = FileUtils.isFileExist(Environment.getExternalStorageDirectory().toString(), xml);
        if (zipFile == null) {
            LogUtils.i("TAG", "导入数据的文件夹没有PatrolPlan.xml");
            ToastUtils.toastL(this,"没有PatrolPlan.xml");
            return;
        }
        Tools.ReadXml(Environment.getExternalStorageDirectory().toString()+ File.separator + xml);
    }
}
