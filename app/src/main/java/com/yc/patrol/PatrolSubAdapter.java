package com.yc.patrol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.baidu.idl.facesdkdemo.R;

import java.util.List;

public class PatrolSubAdapter extends RecyclerView.Adapter<PatrolSubAdapter.VH> {

    List<PatrolBean.ProjectResult> mDatas;
    Context mContext;
    private int mPosition;

    public PatrolSubAdapter(List<PatrolBean.ProjectResult> mDatas, Context ctx) {
        this.mDatas = mDatas;
        this.mContext = ctx;
    }

    /**
     * 新增方法
     *
     * @param position 位置
     */
    public void setPosition(int position) {
        this.mPosition = position;
    }


    @NonNull
    @Override
    public PatrolSubAdapter.VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_sub,viewGroup,false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatrolSubAdapter.VH viewHolder, final int i) {
        PatrolBean.ProjectResult result = mDatas.get(i);
        String str = result.getObjName() + " "+ result.getObjDesc();
        viewHolder.tv_item.setText(str);
        viewHolder.switch_item.setTag(i+"_"+str);
        viewHolder.switch_item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                System.out.println(buttonView.getTag().toString()+ " "+ isChecked);

            }
        });
    }


    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }


    public static class VH extends RecyclerView.ViewHolder{
        Switch switch_item;
        TextView tv_item;
        public VH(@NonNull View itemView) {
            super(itemView);
            switch_item = itemView.findViewById(R.id.switch_item);
            tv_item = itemView.findViewById(R.id.tv_item);

        }
    }

}
