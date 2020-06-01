package com.baidu.idl.face.main.patrol.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;



public class BaseDialog extends Dialog {

    private Context context;
    private int height;
    private int width;
    private boolean cancelTouchout;
    private View view;
    private Builder builder;

    private BaseDialog(Builder builder) {
        super(builder.context);
        initDialog(builder);
    }


    private BaseDialog(Builder builder, int resStyle) {
        super(builder.context, resStyle);
        initDialog(builder);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
        setCanceledOnTouchOutside(cancelTouchout);
        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.gravity = builder.gravity;
        lp.height = height;
        lp.width = width;
        if (builder.paddings != null) {
            win.getDecorView().setPadding(DensityUtil.dp2px(builder.paddings[0]), DensityUtil.dp2px(builder.paddings[1]), DensityUtil.dp2px(builder.paddings[2]), DensityUtil.dp2px(builder.paddings[3]));
        }
        win.setAttributes(lp);
    }

    private void initDialog(Builder builder) {
        this.context = builder.context;
        this.height = builder.height;
        this.width = builder.width;
        this.cancelTouchout = builder.cancelTouchout;
        this.view = builder.view;
        this.builder = builder;
    }

    public View getView(){
        return view;
    }

    public static final class Builder {
        private Context context;
        private int height = WindowManager.LayoutParams.WRAP_CONTENT;
        private int width = WindowManager.LayoutParams.MATCH_PARENT;
        private boolean cancelTouchout = true;
        private View view;
        private int resStyle = -1;
        private int gravity = Gravity.CENTER;
        private int[] paddings = null;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setContentView(int resView) {
            this.view = LayoutInflater.from(context).inflate(resView, null);
            return this;
        }

        public Builder setStyle(int resStyle) {
            this.resStyle = resStyle;
            return this;
        }

        /**
         * 点击dialog外面是否消失
         *
         * @param val ：true：消失。false：不消失
         * @return Builder
         */
        public Builder setCancelTouchout(boolean val) {
            this.cancelTouchout = val;
            return this;
        }

        /**
         * 设置padding
         *
         * @param paddings : new int[]{left,top,right,bottom}
         * @return Builder
         */
        public Builder setPadding(int[] paddings) {
            this.paddings = paddings;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = DensityUtil.dp2px(height);
            return this;
        }

        public Builder setWidth(int width) {
            this.width = DensityUtil.dp2px(width);
            return this;
        }


        public Builder setOnClickListener(int viewRes, View.OnClickListener listener) {
            view.findViewById(viewRes).setOnClickListener(listener);
            return this;
        }

        public BaseDialog build() {
            if (resStyle != -1) {
                return new BaseDialog(this, resStyle);
            } else {
                return new BaseDialog(this);
            }
        }
    }

}
