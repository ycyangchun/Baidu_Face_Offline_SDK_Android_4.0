package com.yc.patrol.utils;


import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;

public class CustomDialogInput implements View.OnClickListener, DialogInterface.OnDismissListener {
    private BaseActivity activity;
    private BaseDialog dialog;
    private TextView title;
    private LinearLayout lltitle;
    private Button negativeButton;
    private Button positiveButton;
    private View lloff;
    private OnDialogClickListener onDialogClickListener;
    private Object obj;
    private LinearLayout llmessage;
    private EditText user_admin_et, psw_admin_et;

    public CustomDialogInput(BaseActivity activity) {
        this(activity, true);
    }

    public CustomDialogInput(BaseActivity activity, boolean CanceledOnTouchOutside) {
        this.activity = activity;
        dialog = new BaseDialog.Builder(activity)
                .setContentView(R.layout.dialog_custom_input)
                .setGravity(Gravity.CENTER)
                .setStyle(R.style.ProgressDialog)
                .setCancelTouchout(CanceledOnTouchOutside)
                .build();
        dialog.setOnDismissListener(this);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();

        lp.dimAmount = 0.6f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        initView();
    }

    //默认点击对话框外面可以消失
    public void show() {
        show(true);
    }

    public void show(boolean isCancle) {
        dialog.setCanceledOnTouchOutside(isCancle);
        dialog.show();
    }

    private void initView() {
        lltitle = (LinearLayout) dialog.getView().findViewById(R.id.lltitle);
        title = (TextView) dialog.getView().findViewById(R.id.title);
        llmessage = (LinearLayout) dialog.getView().findViewById(R.id.llmessage);
        negativeButton = (Button) dialog.getView().findViewById(R.id.negativeButton);
        positiveButton = (Button) dialog.getView().findViewById(R.id.positiveButton);
        user_admin_et = (EditText) dialog.getView().findViewById(R.id.user_admin_et);
        psw_admin_et = (EditText) dialog.getView().findViewById(R.id.psw_admin_et);

        lloff = dialog.getView().findViewById(R.id.lloff);
        negativeButton.setOnClickListener(this);//取消
        positiveButton.setOnClickListener(this);//确定
    }

    public BaseActivity getaAtivity() {
        return activity;
    }

    //设置标题
    public CustomDialogInput setTitle(String titleStr) {
        lltitle.setVisibility(View.VISIBLE);
        title.setText(titleStr);
        return this;
    }


    //设置显示指纹id的图片
    public CustomDialogInput setTouchid(String showTouchidStr) {
        llmessage.setVisibility(View.GONE);
        return this;
    }

    //单独设置 取消 按钮的显示，隐藏 确定 按钮
    public CustomDialogInput setNegativeButton(String negativeButtonStr) {
        positiveButton.setVisibility(View.GONE);
        lloff.setVisibility(View.GONE);
        negativeButton.setText(negativeButtonStr);
        return this;
    }

    //单独设置 确定 按钮的显示，隐藏 取消 按钮
    public CustomDialogInput setPositiveButton(String positiveButtonStr) {
        negativeButton.setVisibility(View.GONE);
        lloff.setVisibility(View.GONE);
        positiveButton.setText(positiveButtonStr);
        return this;
    }

    //设置 确定、取消 按钮的显示
    public CustomDialogInput setButton(String positiveButtonStr, String negativeButtonStr) {
        negativeButton.setVisibility(View.VISIBLE);
        positiveButton.setVisibility(View.VISIBLE);
        lloff.setVisibility(View.VISIBLE);
        positiveButton.setText(positiveButtonStr);
        negativeButton.setText(negativeButtonStr);
        dialog.setCancelable(true);
        return this;
    }

    //设置数据
    public CustomDialogInput setData(Object obj) {
        this.obj = obj;
        return this;
    }

    //设置数据
    public Object getData() {
        return obj;
    }

    public CustomDialogInput setCancelable(boolean isCancle) {
        dialog.setCancelable(isCancle);
        return this;
    }


    public void dimiss() {
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positiveButton:

                String user = user_admin_et.getText().toString().trim();
                String psw = psw_admin_et.getText().toString().trim();
                if (TextUtils.isEmpty(user)) {
                    ToastUtils.toast(activity, "管理员为空");
                    return;
                }
                if (TextUtils.isEmpty(psw)) {
                    ToastUtils.toast(activity, "密码为空");
                    return;
                }

                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(psw)) {
                    if ("admin".equals(user) && "admin123".equals(psw)) {
                        obj = "success";
                    } else {
                        ToastUtils.toast(activity, "管理员或密码不对");
                        return;
                    }
                }
                dialog.dismiss();
                if (onDialogClickListener != null) {
                    onDialogClickListener.OnDialogClickCallBack(true, obj);
                }
                break;
            case R.id.negativeButton:
                dialog.dismiss();
                if (onDialogClickListener != null) {
                    onDialogClickListener.OnDialogClickCallBack(false, obj);
                }
                break;
        }
    }


    public void onDestory() {
        dialog.cancel();
        dialog.setOnDismissListener(null);
        dialog = null;
        onDialogClickListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (onDialogClickListener != null) {
            onDialogClickListener.onDismiss(dialog, obj);
        }
    }

    public interface OnDialogClickListener {
        /**
         * 点击下面 确定 或 取消 这两个按钮的时候，触发的回调接口
         *
         * @param isPositive ：是 确定 还是 取消 。{@code 确定 ：true};{@code 取消 ：false}
         * @param obj        :回调要带回的结果数据
         */
        void OnDialogClickCallBack(boolean isPositive, Object obj);

        void onDismiss(DialogInterface dialog, Object obj);
    }

    public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener;
    }
}
