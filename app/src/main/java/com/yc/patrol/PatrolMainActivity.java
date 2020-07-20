package com.yc.patrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;
import com.yc.patrol.scanner.CaptureActivity;
import com.yc.patrol.utils.CustomDialog2;
import com.yc.patrol.utils.DateUtils;
import com.yc.patrol.utils.FileUtils2;
import com.yc.patrol.utils.PhotoUtils;
import com.yc.patrol.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatrolMainActivity extends BaseActivity implements PatrolAdapter.ItemClickListener,
        CustomDialog2.OnDialogClickListener {

    RecyclerView recyclerView;
    PatrolMainActivity mContext;
    private final static int FILE_CHOOSER_RESULT_CODE = 128;
    private final static int PHOTO_REQUEST = 100;
    private final static int SCAN_REQUEST = 101;
    private Uri imageUri, imageUriSy;
    private File fileUri, fileUriSy;
    private String patrolImage;
    private PatrolAdapter adapter;
    public List<PatrolBean> list;
    private CustomDialog2 customDialog;
    private TextView nameTv;
    private CircleImageView head_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patrollist);
        mContext = this;
        initView();
        initData();
    }


    private void initData() {
        list = new ArrayList<>();
        adapter = new PatrolAdapter(list, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        adapter.setListener(this);
        findViewById(R.id.back_bnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBack();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            toBack();
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void toBack() {
        showDialog("确定退出?","finish");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null == list || list.size() == 0) {
            List<PatrolBean> localList = Tools.ReadPatrolBeanXml();
            if (null != localList && localList.size() > 0) {

                PatrolBean lastPb = localList.get(localList.size() - 1);
                if (null != nameTv) {
                    nameTv.setText(lastPb.getName());
                    String head = Environment.getExternalStorageDirectory() + File.separator +
                            "Success-Import" + File.separator +
                            "default-"+lastPb.getFullName()+".jpg";
                    head_view.setImageBitmap(PhotoUtils.getBitmapFromUri(head,mContext));
                }
                String aTime = lastPb.getArriveTime();
                if (TextUtils.isEmpty(aTime)) {
                    //登录时间
                    lastPb.setArriveTime(DateUtils.gethmsTime());
                    localList.set(localList.size() - 1, lastPb);
                }
                list.clear();
                list.addAll(localList);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(0);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(null != adapter) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Tools.createDOMXml(adapter.getPatrolBeanList(), mContext, MyConstants.tempXml, false);
                }
            }).start();

        }
    }

    private void initView() {
        nameTv = this.findViewById(R.id.title);
        recyclerView = this.findViewById(R.id.recycler_view);
        head_view = this.findViewById(R.id.avatar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initCustomDialog();
        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CaptureActivity.class);
                startActivityForResult(intent, SCAN_REQUEST);
            }
        });

        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.show();
                showDialog("确认巡更完成,导出数据？","save");
            }
        });
    }

    @Override
    public void OnDialogClickCallBack(boolean isPositive, Object obj) {
        String data = (String)obj;
        if (isPositive) {
            if("save".equals(data)) {
                Tools.createDOMXml(adapter.getPatrolBeanList(), mContext);
                mContext.finish();
            }else if("finish".equals(data)){
                mContext.finish();
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog, Object obj) {

    }

    private void initCustomDialog() {
        if (customDialog == null) {
            customDialog = new CustomDialog2(this);
            customDialog.setOnDialogClickListener(this);
        }
    }

    public void showDialog(String title,String data){
        customDialog.setMessage(title)
                .setData(data)
                .setButton("确认", "取消")
                .setCancelable(true);
        customDialog.show();
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
        String time = DateUtils.getStringTime();
        fileUri = new File(FileUtils2.getCacheFilePath(mContext, MyConstants.DATAPATH + File.separator + "tempPic" + File.separator + time + ".jpg"));
        patrolImage = qRcode + "-" + time + ".jpg";
        fileUriSy = new File(FileUtils2.getCacheFilePath(mContext, MyConstants.DATAPATH + File.separator + patrolImage));

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
    private String qRcode;

    @Override
    public void itemClick(int pos, PatrolBean pb) {
        patrolBean = pb;
        position = pos;
        qRcode = pb.getLinePlaceName();
        takeCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (null == result) {
                result = imageUri;
            }
            if (PhotoUtils.getBitmapSizeFormUri(mContext, result) > 0) {
                patrolBean.setPhotoUrl(fileUri.getAbsolutePath());
                patrolBean.setUri(result);
                patrolBean.setUriSy(imageUriSy);
                patrolBean.setPhotoUrlSy(fileUriSy.getAbsolutePath());
                patrolBean.setPatrolImage(patrolImage);
                recyclerView.scrollToPosition(position);
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.upData(position, patrolBean);
                    }
                });
            }
        }
        if (requestCode == SCAN_REQUEST) {
            String qrcode = data == null || resultCode != RESULT_OK ? null : data.getStringExtra("scan");
            boolean isHave = false;
            for (int i = 0; i < list.size(); i++) {
                final PatrolBean pb = list.get(i);
                if (qrcode.equals(pb.getqRcode())) {
                    isHave = true;
                    pb.setIsShow("1");
                    pb.setArriveTime(DateUtils.gethmsTime());
                    recyclerView.scrollToPosition(i);
                    final int finalI = i;
                    //更新UI要在 主线程 执行
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.upData(finalI, pb);
                        }
                    });


                    break;
                }
            }

            if (!isHave) {
                ToastUtils.toastL(mContext, "没有此任务！");
            }
        }
    }
}
