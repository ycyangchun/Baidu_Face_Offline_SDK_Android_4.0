package com.baidu.idl.face.main.patrol;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.FaceRGBCloseDebugSearchActivity;
import com.baidu.idl.face.main.patrol.scanner.CaptureActivity;
import com.baidu.idl.face.main.patrol.utils.PhotoUtils;
import com.baidu.idl.facesdkdemo.R;
import com.flyco.roundview.RoundTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PatrolMainActivity extends BaseActivity {

    RecyclerView recyclerView;
    PatrolMainActivity mContext;
    private final static int FILE_CHOOSER_RESULT_CODE = 128;
    private final static int PHOTO_REQUEST = 100;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patrollist);
        mContext = this;
        recyclerView = this.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");

        PatrolAdapter adapter = new PatrolAdapter(list);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CaptureActivity.class);
                startActivityForResult(intent, 111);
            }
        });

        findViewById(R.id.photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeCamera();
            }
        });
    }

    /**
     * 选择图片
     */
    private void takePhoto() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * 拍照
     */
    private void takeCamera() {
        File fileUri = new File(Environment.getExternalStorageDirectory().getPath() + "/patrol/" + SystemClock.currentThreadTimeMillis() + ".jpg");
        imageUri = Uri.fromFile(fileUri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(mContext, getPackageName() + ".fileprovider", fileUri);//通过FileProvider创建一个content类型的Uri
        }
        PhotoUtils.takePicture(mContext, imageUri, PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_REQUEST) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();

        }
    }
}
