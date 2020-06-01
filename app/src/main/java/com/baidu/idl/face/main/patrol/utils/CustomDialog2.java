package com.baidu.idl.face.main.patrol.utils;


import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
;import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.facesdkdemo.R;

public class CustomDialog2 implements View.OnClickListener, DialogInterface.OnDismissListener {
    private BaseActivity activity;
    private BaseDialog dialog;
    private TextView title;
    private LinearLayout lltitle;
    private TextView message;
    private Button negativeButton;
    private Button positiveButton;
    private View lloff;
    private OnDialogClickListener onDialogClickListener;
    private Object obj;
    private LinearLayout touchid_ll;
    private LinearLayout llmessage;
    private TextView touchid_text;

    public CustomDialog2(BaseActivity activity) {
        this(activity,true);
    }

    public CustomDialog2(BaseActivity activity, boolean CanceledOnTouchOutside){
        this.activity = activity;
        dialog = new BaseDialog.Builder(activity)
                .setContentView(R.layout.dialog_custom)
                .setGravity(Gravity.CENTER)
                .setStyle(R.style.ProgressDialog)
                .setCancelTouchout(CanceledOnTouchOutside)
                .build();
        dialog.setOnDismissListener(this);
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
        message = (TextView) dialog.getView().findViewById(R.id.message);
        negativeButton = (Button) dialog.getView().findViewById(R.id.negativeButton);
        positiveButton = (Button) dialog.getView().findViewById(R.id.positiveButton);
        lloff = dialog.getView().findViewById(R.id.lloff);
        touchid_ll = (LinearLayout) dialog.getView().findViewById(R.id.touchid_ll);
        touchid_text = (TextView) dialog.getView().findViewById(R.id.touchid_text);
        negativeButton.setOnClickListener(this);//取消
        positiveButton.setOnClickListener(this);//确定
    }

    public BaseActivity getaAtivity(){
        return activity;
    }
    //设置标题
    public CustomDialog2 setTitle(String titleStr) {
        lltitle.setVisibility(View.VISIBLE);
        title.setText(titleStr);
        return this;
    }

    //设置显示的消息
    public CustomDialog2 setMessage(String messageStr) {
        touchid_ll.setVisibility(View.GONE);
        llmessage.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(messageStr)) {
            message.setText("");
            return this;
        }
        message.setText(messageStr);
        return this;
    }

    //设置显示指纹id的图片
    public CustomDialog2 setTouchid(String showTouchidStr) {
        touchid_ll.setVisibility(View.VISIBLE);
        llmessage.setVisibility(View.GONE);
        touchid_text.setText(showTouchidStr);
        return this;
    }

    //单独设置 取消 按钮的显示，隐藏 确定 按钮
    public CustomDialog2 setNegativeButton(String negativeButtonStr) {
        positiveButton.setVisibility(View.GONE);
        lloff.setVisibility(View.GONE);
        negativeButton.setText(negativeButtonStr);
        return this;
    }

    //单独设置 确定 按钮的显示，隐藏 取消 按钮
    public CustomDialog2 setPositiveButton(String positiveButtonStr) {
        negativeButton.setVisibility(View.GONE);
        lloff.setVisibility(View.GONE);
        positiveButton.setText(positiveButtonStr);
        return this;
    }

    //设置 确定、取消 按钮的显示
    public CustomDialog2 setButton(String positiveButtonStr, String negativeButtonStr) {
        negativeButton.setVisibility(View.VISIBLE);
        positiveButton.setVisibility(View.VISIBLE);
        lloff.setVisibility(View.VISIBLE);
        positiveButton.setText(positiveButtonStr);
        negativeButton.setText(negativeButtonStr);
        dialog.setCancelable(true);
        return this;
    }

    //设置数据
    public CustomDialog2 setData(Object obj) {
        this.obj = obj;
        return this;
    }

    //设置数据
    public Object getData() {
        return obj;
    }
    public TextView getMessage(){
        return message;
    }
    public CustomDialog2 setCancelable(boolean isCancle){
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
