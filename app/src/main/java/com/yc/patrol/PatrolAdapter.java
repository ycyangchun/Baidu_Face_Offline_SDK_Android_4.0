package com.yc.patrol;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.facesdkdemo.R;
import com.yc.patrol.utils.PhotoUtils;
import com.yc.patrol.utils.Tools;

import java.util.List;

public class PatrolAdapter extends RecyclerView.Adapter<PatrolAdapter.VH> {

    List<PatrolBean> mDatas;
    Context mContext;
    ItemClickListener listener;

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public PatrolAdapter(List<PatrolBean> mDatas, Context ctx) {
        this.mDatas = mDatas;
        this.mContext = ctx;
    }

    public void addData(int position,PatrolBean pb) {
        mDatas.add(position, pb);
        notifyItemInserted(position);
    }

    public void upData(int position,PatrolBean pb) {
        mDatas.set(position, pb);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatrolAdapter.VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.activity_patrollist_item,viewGroup,false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatrolAdapter.VH viewHolder, final int i) {
        final PatrolBean patrolBean = mDatas.get(i);
        viewHolder.item_time.setText(patrolBean.getTime());
        viewHolder.item_title.setText(patrolBean.getPlace());
        String url = patrolBean.getPhotoUrl();
        String urlSy = patrolBean.getPhotoUrlSy();
        Uri uri = patrolBean.getUri();
        Uri uriSy = patrolBean.getUriSy();
        if(TextUtils.isEmpty(url)){ // 扫码后创建的空数据

        }else {
            if (null != uri) {
                viewHolder.item_photo.setImageBitmap(PhotoUtils.makePhoto(mContext, uri, urlSy,
                        patrolBean.getPlace()));
                Tools.deleteFile(url);
                patrolBean.setUri(null);

            } else {
                viewHolder.item_photo.setImageBitmap(PhotoUtils.getBitmapFromUri(uriSy, mContext));
            }
        }
        if(i == 0){
            viewHolder.item_status_iv.setBackgroundResource(R.drawable.ic_switch);
        }else if(i == 2){
            viewHolder.item_status_iv.setBackgroundResource(R.drawable.ic_close_line);
        }
        viewHolder.item_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != listener){
                    listener.itemClick(i,patrolBean);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    interface ItemClickListener{
        void itemClick(int position,PatrolBean patrolBean);
    }

    public static class VH extends RecyclerView.ViewHolder{
        TextView item_time;
        TextView item_title;
        ImageView item_photo;
        ImageView item_status_iv;
        public VH(@NonNull View itemView) {
            super(itemView);
            item_time = itemView.findViewById(R.id.item_time);
            item_title = itemView.findViewById(R.id.item_title);
            item_photo = itemView.findViewById(R.id.item_photo);
            item_status_iv = itemView.findViewById(R.id.item_status_iv);

        }
    }

}
