package com.baidu.idl.face.main.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.facesdkdemo.R;

import java.text.DecimalFormat;

/**
 * 自定义Dialog，显示配置等信息
 */
public class CustomDialog extends Dialog {

    private Context mContext;
    private TextView minFaceTv;
    private TextView imageSizeTv;
    private TextView silentLiveTypeTv;
    private TextView featureThresholdValueTv;
    private TextView rgbLiveThresholdValueTv;
    private TextView nirLiveThresholdValueTv;
    private TextView depthLiveThresholdValueTv;
    private TextView detectTypeTv;

    private OnDialogClickListener dialogClickListener;

    public CustomDialog(@NonNull Context context) {
        this(context, 0);
    }

    public CustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_config_info, null);
        setContentView(view);

        view.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onConfirmClick(view);
                }
            }
        });

        view.findViewById(R.id.btn_modifier).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onModifierClick(view);
                }
            }
        });

        minFaceTv = ((TextView) view.findViewById(R.id.tv_min_face));
        imageSizeTv = ((TextView) view.findViewById(R.id.tv_image_size));
        silentLiveTypeTv = ((TextView) view.findViewById(R.id.tv_silent_live_type));
        featureThresholdValueTv = ((TextView) view.findViewById(R.id.tv_feature_threshold_value));
        rgbLiveThresholdValueTv = ((TextView) view.findViewById(R.id.tv_rgb_live_threshold_value));
        nirLiveThresholdValueTv = ((TextView) view.findViewById(R.id.tv_nir_live_threshold_value));
        depthLiveThresholdValueTv = ((TextView) view.findViewById(R.id.tv_depth_live_threshold_value));
        detectTypeTv = ((TextView) view.findViewById(R.id.tv_detect_type));
    }

    private void refresh() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        minFaceTv.setText(mContext.getString(R.string.min_face_size,
                SingleBaseConfig.getBaseConfig().getMinimumFace() + ""));

        imageSizeTv.setText(mContext.getString(R.string.image_size, "640*480"));

        silentLiveTypeTv.setText(mContext.getString(R.string.silent_live_type,
                SingleBaseConfig.getBaseConfig().getType() == 1 ? "不使用活体"
                        : SingleBaseConfig.getBaseConfig().getType() == 2 ? "RGB+LIVE活体"
                        : SingleBaseConfig.getBaseConfig().getType() == 3 ? "RGB+NIR活体"
                        : SingleBaseConfig.getBaseConfig().getType() == 4 ? "RGB+Depth活体"
                        : "不使用活体"));

        String rgbLiveScore = decimalFormat.format(SingleBaseConfig.getBaseConfig().getRgbLiveScore());
        featureThresholdValueTv.setText(mContext.getString(R.string.feature_threshold_value,
                SingleBaseConfig.getBaseConfig().getThreshold() + ""));
        rgbLiveThresholdValueTv.setText(mContext.getString(R.string.rgb_live_threshold_value,
                rgbLiveScore));
        String nirLiveScore = decimalFormat.format(SingleBaseConfig.getBaseConfig().getNirLiveScore());
        nirLiveThresholdValueTv.setText(mContext.getString(R.string.nir_live_threshold_value,
                nirLiveScore));
        String depthLiveScore = decimalFormat.format(SingleBaseConfig.getBaseConfig().getDepthLiveScore());
        depthLiveThresholdValueTv.setText(mContext.getString(R.string.depth_live_threshold_value,
                depthLiveScore));

        detectTypeTv.setText(mContext.getString(R.string.detect_type,
                "wireframe".equals(SingleBaseConfig.getBaseConfig().getDetectFrame()) ? "全屏线框"
                        : "fixedarea".equals(SingleBaseConfig.getBaseConfig().getDetectFrame())
                        ? "固定检测区域" : "全屏线框"));
    }

    @Override
    public void show() {
        super.show();
        refresh();
    }

    public void setDialogClickListener(OnDialogClickListener listener) {
        this.dialogClickListener = listener;
    }

    public interface OnDialogClickListener {
        void onConfirmClick(View view);

        void onModifierClick(View view);
    }
}
