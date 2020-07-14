package com.yc.patrol;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.yc.patrol.scanner.CaptureActivity;
import com.yc.patrol.utils.DateUtils;
import com.yc.patrol.utils.FileUtils2;
import com.yc.patrol.utils.PhotoUtils;
import com.baidu.idl.facesdkdemo.R;
import com.yc.patrol.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PatrolMainActivity extends BaseActivity implements PatrolAdapter.ItemClickListener{

    RecyclerView recyclerView;
    PatrolMainActivity mContext;
    private final static int FILE_CHOOSER_RESULT_CODE = 128;
    private final static int PHOTO_REQUEST = 100;
    private final static int SCAN_REQUEST = 101;
    private Uri imageUri,imageUriSy;
    private File fileUri,fileUriSy;
    PatrolAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patrollist);
        mContext = this;
        recyclerView = this.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<PatrolBean> list = new ArrayList<>();

        list.add(new PatrolBean(DateUtils.gethmsTime(),"打卡",""));

        adapter = new PatrolAdapter(list,this);
        recyclerView.setAdapter(adapter);
        adapter.setListener(this);
        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CaptureActivity.class);
                startActivityForResult(intent, SCAN_REQUEST);
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
        String time = DateUtils.getNowTime();
        fileUri = new File(FileUtils2.getCacheFilePath(mContext,MyConstants.DATAPATH + File.separator +"tempPic"+ File.separator + time + ".jpg") );
        fileUriSy = new File( FileUtils2.getCacheFilePath(mContext, MyConstants.DATAPATH + File.separator + place + time + ".jpg") );

        imageUri = Uri.fromFile(fileUri);
        imageUriSy = Uri.fromFile(fileUriSy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(mContext, getPackageName() + ".fileprovider", fileUri);//通过FileProvider创建一个content类型的Uri
            imageUriSy = FileProvider.getUriForFile(mContext, getPackageName() + ".fileprovider", fileUriSy);//通过FileProvider创建一个content类型的Uri

        }
        PhotoUtils.takePicture(mContext, imageUri, PHOTO_REQUEST);
    }


    private PatrolBean patrolBean;
    private int position;
    private String place;
    @Override
    public void itemClick(int pos, PatrolBean pb) {
        patrolBean = pb;
        position = pos;
        place = pb.getPlace();
        takeCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if(null == result) {
                result = imageUri;
            }
            if(PhotoUtils.getBitmapSizeFormUri(mContext,result) > 0) {
                patrolBean.setPhotoUrl(fileUri.getAbsolutePath());
                patrolBean.setUri(result);
                patrolBean.setUriSy(imageUriSy);
                patrolBean.setPhotoUrlSy(fileUriSy.getAbsolutePath());
                adapter.upData(position, patrolBean);
                recyclerView.scrollToPosition(0);
            }
        }
        if(requestCode == SCAN_REQUEST){
            String place = data == null || resultCode != RESULT_OK ? null : data.getStringExtra("scan");
            adapter.addData(0,new PatrolBean(DateUtils.gethmsTime(),place,""));
            recyclerView.scrollToPosition(0);
        }
    }
}
