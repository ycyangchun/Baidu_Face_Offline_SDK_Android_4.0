package com.baidu.idl.face.main.patrol;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.facesdkdemo.R;

import java.util.List;

public class PatrolAdapter extends RecyclerView.Adapter<PatrolAdapter.VH> {

    List<String> mDatas;

    public PatrolAdapter(List<String> mDatas) {
        this.mDatas = mDatas;
    }

    @NonNull
    @Override
    public PatrolAdapter.VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.activity_patrollist_item,viewGroup,false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatrolAdapter.VH viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public static class VH extends RecyclerView.ViewHolder{
        TextView item_time;
        TextView item_title;
        ImageView item_photo;
        public VH(@NonNull View itemView) {
            super(itemView);
            item_time = itemView.findViewById(R.id.item_time);
            item_title = itemView.findViewById(R.id.item_title);
            item_photo = itemView.findViewById(R.id.item_photo);

        }
    }

}
