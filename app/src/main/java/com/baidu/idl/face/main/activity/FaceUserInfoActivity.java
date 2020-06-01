package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.manager.UserInfoManager;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 用户信息页面
 * Created by liujialu on 2019/5/31.
 */

public class FaceUserInfoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = FaceUserInfoActivity.class.getSimpleName();
    private static final int REQUEST_LOAD_IMAGE = 1000;
    private static final int REQUEST_CAMERA_IMAGE = 1001;

    private TextView mTextUserName;
    private TextView mTextUserInfoName;
    private TextView mTextUserInfo;
    private TextView mTextUserInfoCtime;
    private ImageView mImageUserHead;

    // 更换图片相关View
    private RelativeLayout mRelativePopwindow;
    private Button mBtAlbum;
    private Button mBtCamera;
    private Button mBtCancle;

    private String mImageName;
    private String mUserName;
    private String mGroupId;
    private String mUserId;
    private FaceUserInfoListener mListener;
    private Context mContext;

    private File mImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        mContext = this;
        initView();
        initData();
    }

    private void initView() {
        // title相关
        TextView textTitle = findViewById(R.id.tv_title);
        textTitle.setText("用户信息");
        Button btnBatchOperation = findViewById(R.id.btn_setting);
        btnBatchOperation.setVisibility(View.GONE);
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        mTextUserName = findViewById(R.id.text_user_name);
        mTextUserInfoName = findViewById(R.id.text_info_name);
        mTextUserInfo = findViewById(R.id.text_info);
        mTextUserInfoCtime = findViewById(R.id.text_info_ctime);
        mImageUserHead = findViewById(R.id.image_info_pic);
        Button btnUpdate = findViewById(R.id.button_update);
        btnUpdate.setOnClickListener(this);
        Button btnDelete = findViewById(R.id.button_delete);
        btnDelete.setOnClickListener(this);

        mRelativePopwindow = findViewById(R.id.relative_pop_window);
        mRelativePopwindow.setOnClickListener(this);
        mBtAlbum = findViewById(R.id.btn_pop_album);
        mBtAlbum.setOnClickListener(this);
        mBtCamera = findViewById(R.id.btn_pop_camera);
        mBtCamera.setOnClickListener(this);
        mBtCancle = findViewById(R.id.btn_pop_cancel);
        mBtCancle.setOnClickListener(this);
    }

    private void initData() {
        mListener = new FaceUserInfoListener();
        Intent intent = getIntent();
        if (intent != null) {
            mGroupId = intent.getStringExtra("group_id");
            mUserId = intent.getStringExtra("user_id");

            mUserName = intent.getStringExtra("user_name");
            mTextUserName.setText("用户：" + mUserName);
            mTextUserInfoName.setText("用户名：" + mUserName);

            String userInfo = intent.getStringExtra("user_info");
            if (TextUtils.isEmpty(userInfo)) {
                mTextUserInfo.setVisibility(View.GONE);
            } else {
                mTextUserInfo.setVisibility(View.VISIBLE);
                mTextUserInfo.setText("用户信息：" + userInfo);
            }

            long ctime = intent.getLongExtra("ctime", 0);
            String ctimeFormat = Utils.formatTime(ctime, "yyyy.MM.dd HH:mm:ss");
            mTextUserInfoCtime.setText("创建时间：" + ctimeFormat);

            mImageName = intent.getStringExtra("user_pic");
            Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getBatchImportSuccessDirectory()
                    + "/" + mImageName);
            mImageUserHead.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserInfoManager.getInstance().release();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:   // 返回
                finish();
                break;

            case R.id.button_update:  // 更新图片
                mRelativePopwindow.setVisibility(View.VISIBLE);
                break;

            case R.id.button_delete:   // 删除
                UserInfoManager.getInstance().deleteUserInfo(mUserId, mGroupId, mListener);
                break;

            case R.id.relative_pop_window:  // 点击外部隐藏
                if (mRelativePopwindow.getVisibility() == View.VISIBLE) {
                    mRelativePopwindow.setVisibility(View.GONE);
                }
                break;

            case R.id.btn_pop_album:  // 相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_LOAD_IMAGE);
                if (mRelativePopwindow.getVisibility() == View.VISIBLE) {
                    mRelativePopwindow.setVisibility(View.GONE);
                }
                break;

            case R.id.btn_pop_camera: // 相机
                // 打开相机
                takeCamera(REQUEST_CAMERA_IMAGE);
                if (mRelativePopwindow.getVisibility() == View.VISIBLE) {
                    mRelativePopwindow.setVisibility(View.GONE);
                }
                break;

            case R.id.btn_pop_cancel:   // 取消
                if (mRelativePopwindow.getVisibility() == View.VISIBLE) {
                    mRelativePopwindow.setVisibility(View.GONE);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 打开相机
     */
    private void takeCamera(int requestCode) {
        // 获取根路径
        File root = Environment.getExternalStorageDirectory();
        // 保存的图片文件
        mImageFile = new File(root, "test.jpg");
        Uri uri = Uri.fromFile(mImageFile);
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 从相册返回
            if (requestCode == REQUEST_LOAD_IMAGE && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    InputStream input = null;
                    try {
                        input = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(input);
                        UserInfoManager.getInstance().updateImage(bitmap, mGroupId, mUserName, mImageName, mListener);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (input != null) {
                                input.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else if (requestCode == REQUEST_CAMERA_IMAGE) {
                if (mImageFile.exists()) {
                    Bitmap srcBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
                    srcBitmap = BitmapUtils.calculateInSampleSize(srcBitmap, 640, 480);
                    UserInfoManager.getInstance().updateImage(srcBitmap, mGroupId, mUserName, mImageName, mListener);

                    mImageFile.delete();
                } else {
                    ToastUtils.toast(mContext, "相机获取图像失败");
                }
            }
        }
    }

    private class FaceUserInfoListener extends UserInfoManager.UserInfoListener {
        @Override
        public void updateImageSuccess(final Bitmap bitmap) {
            // 数据变化，更新内存
            FaceApi.getInstance().initDatabases(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    mImageUserHead.setImageBitmap(bitmap);
                    ToastUtils.toast(mContext, "更换成功");
                }
            });
        }

        @Override
        public void updateImageFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.toast(mContext, message);
                }
            });
        }

        @Override
        public void userDeleteSuccess() {
            // 数据变化，更新内存
            FaceApi.getInstance().initDatabases(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.toast(mContext, "删除成功");
                    finish();
                }
            });
        }

        @Override
        public void userDeleteFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.toast(mContext, message);
                }
            });
        }
    }
}
