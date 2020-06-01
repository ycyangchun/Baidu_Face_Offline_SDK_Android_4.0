package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.listener.OnImportListener;
import com.baidu.idl.face.main.manager.ImportFileManager;
import com.baidu.idl.face.main.utils.LogUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;

/**
 * 批量导入
 * Created by v_liujialu01 on 2019/5/27.
 */

public class BatchImportActivity extends BaseActivity implements View.OnClickListener, OnImportListener {

    // view
    private Button mButtonImport;
    private RelativeLayout mRelativeContent;    // 显示说明的布局
    private RelativeLayout mRelativeImport;     // 显示进度的布局
    private RelativeLayout mRelativeFinish;     // 显示结果的布局

    // import
    private ProgressBar mProgressBar;
    private TextView mTextImportFinish;   // 已处理
    private TextView mTextImportSuccess;  // 成功
    private TextView mTextImportFailure;  // 失败

    // finish
    private TextView mTextFinish;           // 已处理
    private TextView mTextFinishSuccess;   // 成功
    private TextView mTextFinishFailure;   // 失败

    private Context mContext;
    private volatile boolean mImporting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_import);
        mContext = this;
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放
        ImportFileManager.getInstance().release();
    }

    private void initView() {
        Button buttonBack = findViewById(R.id.button_import_back);
        buttonBack.setOnClickListener(this);
        mButtonImport = findViewById(R.id.button_import);
        mButtonImport.setOnClickListener(this);
        mRelativeContent = findViewById(R.id.relative_content);
        mRelativeImport = findViewById(R.id.relative_progress);
        mRelativeFinish = findViewById(R.id.relative_finish);
        mProgressBar = findViewById(R.id.progress_bar);
        mTextImportFinish = findViewById(R.id.text_import_finish);
        mTextImportSuccess = findViewById(R.id.text_import_success);
        mTextImportFailure = findViewById(R.id.text_import_failure);
        mTextFinish = findViewById(R.id.text_finish);
        mTextFinishSuccess = findViewById(R.id.text_finish_success);
        mTextFinishFailure = findViewById(R.id.text_finish_failure);
    }

    private void initData() {
        ImportFileManager.getInstance().setOnImportListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_import:   // 点击导入数据按钮
                if (!mImporting) {
                    // 如果按钮文案是“搜索SD卡，并导入数据”，则执行批量导入操作
                    if ("搜索SD卡，并导入数据".equals(mButtonImport.getText().toString())) {
                        mImporting = true;
                        ToastUtils.toast(mContext, "搜索中，请稍后");
                        ImportFileManager.getInstance().batchImport();
                        // 如果按钮文案是“确认”，则说明导入结束，返回上一层一面
                    } else if ("确认".equals(mButtonImport.getText().toString())) {
                        finish();
                    } else {
                        LogUtils.i("BatchImportActivity", "");
                    }
                }
                break;

            case R.id.button_import_back:
                finish();
                break;

            default:
                break;
        }
    }

    /**
     * 开始解压
     */
    @Override
    public void startUnzip() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mButtonImport == null) {
                    return;
                }
                ToastUtils.toast(mContext, "搜索到压缩包，开始解压");
                mButtonImport.setText("解压中......");
                mButtonImport.setBackgroundColor(Color.parseColor("#7f7f7f"));
            }
        });
    }

    /**
     * 解压完毕，显示导入进度View
     */
    @Override
    public void showProgressView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRelativeContent == null || mRelativeImport == null || mRelativeFinish == null) {
                    return;
                }
                mRelativeContent.setVisibility(View.GONE);
                mRelativeImport.setVisibility(View.VISIBLE);
                mRelativeFinish.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 正在导入，实时更新导入状态
     */
    @Override
    public void onImporting(final int finishCount, final int successCount, final int failureCount,
                            final float progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressBar == null || mTextImportFinish == null
                        || mTextImportSuccess == null || mTextImportFailure == null
                        || mButtonImport == null) {
                    return;
                }
                mButtonImport.setText("导入中......请勿退出此页面");
                mButtonImport.setBackgroundColor(Color.parseColor("#7f7f7f"));
                mProgressBar.setProgress((int) (progress * 100));
                mTextImportFinish.setText("已处理：" + finishCount);
                mTextImportSuccess.setText("导入成功：" + successCount);
                mTextImportFailure.setText("导入失败：" + failureCount);
            }
        });
    }

    /**
     * 导入结束，显示导入结果
     */
    @Override
    public void endImport(final int finishCount, final int successCount, final int failureCount) {
        // 数据变化，更新内存
        FaceApi.getInstance().initDatabases(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRelativeContent == null || mRelativeImport == null || mRelativeFinish == null) {
                    return;
                }
                mRelativeContent.setVisibility(View.GONE);
                mRelativeImport.setVisibility(View.GONE);
                mRelativeFinish.setVisibility(View.VISIBLE);
                mTextFinish.setText("已处理：" + finishCount);
                mTextFinishSuccess.setText("导入成功：" + successCount);
                mTextFinishFailure.setText("导入失败：" + failureCount);
                mButtonImport.setText("确认");
                mButtonImport.setBackgroundColor(Color.parseColor("#036838"));
                mImporting = false;
            }
        });
    }

    /**
     * 提示导入过程中的错误信息
     */
    @Override
    public void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message == null) {
                    return;
                }
                ToastUtils.toast(mContext, message);
                mImporting = false;
            }
        });
    }
}
