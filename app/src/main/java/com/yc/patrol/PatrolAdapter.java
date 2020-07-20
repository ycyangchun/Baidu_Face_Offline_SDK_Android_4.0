package com.yc.patrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.idl.facesdkdemo.R;
import com.yc.patrol.utils.PhotoUtils;
import com.yc.patrol.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class PatrolAdapter extends RecyclerView.Adapter<PatrolAdapter.VH> {

    List<PatrolBean> patrolBeanList;
    Context mContext;
    ItemClickListener listener;
    PatrolAdapter adapter;

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public PatrolAdapter(List<PatrolBean> mDatas, Context ctx) {
        this.patrolBeanList = mDatas;
        this.mContext = ctx;
        adapter = this;
    }

    public void addData(int position,PatrolBean pb) {
        patrolBeanList.add(position, pb);
        notifyItemInserted(position);
    }

    public void upData(int position,PatrolBean pb) {
        patrolBeanList.set(position, pb);
        notifyItemChanged(position);
    }

    public List<PatrolBean> getPatrolBeanList() {
        return patrolBeanList;
    }

    /**
     * switch 更新巡更状态
     * @param position
     * @param subPosition
     * @param objResult
     */
    public void updateStatus(int position,int subPosition,String objResult){
        if(null != patrolBeanList && patrolBeanList.size() > 0 ) {
            if(position < patrolBeanList.size()) {
                PatrolBean pbUpdate = patrolBeanList.get(position);
                List<PatrolBean.ProjectResult> results = pbUpdate.getProjectResults();
                if(null != results && results.size() > 0 ) {
                    PatrolBean.ProjectResult projectResult = results.get(subPosition);

                    if (!objResult.equals(projectResult.getResult())) {
                        //result
                        projectResult.setResult(objResult);
                        results.set(subPosition, projectResult);
                        // List<PatrolBean.ProjectResult>
                        pbUpdate.setProjectResults(results);

                        int k = 1;
                        for (int i = 0; i < results.size(); i++) {
                            k *= Integer.parseInt(getCtx(results.get(i).getResult()));
                        }
                        //IsAbnormal
                        pbUpdate.setIsAbnormal(k + "");
                        patrolBeanList.set(position, pbUpdate);

                        int m = 1;
                        for (int i = 0; i < patrolBeanList.size(); i++) {
                            m *= Integer.parseInt(getCtx(patrolBeanList.get(i).getIsAbnormal()));
                        }
                        //TodayIsAbnormal
                        pbUpdate.setTodayIsAbnormal(m + "");
                        patrolBeanList.set(position, pbUpdate);
                    }
                }

            }
        }

    }


    public static String getCtx(String str) {
        String s = "1";
        if (TextUtils.isEmpty(str)) {

        } else {
            s = str;
        }
        return s;
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
        PatrolBean patrolBean = patrolBeanList.get(i);
        String show = patrolBean.getIsShow();

        if("1".equals(show)){
            viewHolder.list_item_ll.setVisibility(View.VISIBLE);
        }else {
            viewHolder.list_item_ll.setVisibility(View.GONE);
        }

        subAdapter(viewHolder, i, patrolBean);

        viewHolder.item_time.setText(patrolBean.getArriveTime());
        viewHolder.item_title.setText(patrolBean.getLinePlaceName());
        setBg(viewHolder, patrolBean);

        viewHolder.item_status_iv.setBackgroundResource(R.drawable.ic_switch);

        final PatrolBean finalPb = patrolBean;
        viewHolder.item_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != listener){
                    listener.itemClick(i,finalPb);
                }
            }
        });
    }

    private void subAdapter(@NonNull VH viewHolder, int i, PatrolBean patrolBean) {
        if(null != patrolBean.getProjectResults()) {
            viewHolder.list.clear();
            viewHolder.list.addAll(patrolBean.getProjectResults());
            if(viewHolder.list != null && viewHolder.list.size() > 0){
                viewHolder.recycler_sub.setVisibility(View.VISIBLE);
            }else{
                viewHolder.recycler_sub.setVisibility(View.GONE);
            }
            if (viewHolder.patrolSubAdapter == null) {
                viewHolder.patrolSubAdapter = new PatrolSubAdapter(viewHolder.list,i,adapter,mContext);
                viewHolder.recycler_sub.setLayoutManager(new LinearLayoutManager(mContext));
                viewHolder.recycler_sub.setAdapter(viewHolder.patrolSubAdapter);
            } else {
                viewHolder.patrolSubAdapter.setPosition(i);
            }

        }
    }

    private void setBg(@NonNull final VH viewHolder, final PatrolBean patrolBean) {
        String url = patrolBean.getPhotoUrl();
        final String urlSy = patrolBean.getPhotoUrlSy();
        final Uri uri = patrolBean.getUri();
        Uri uriSy = patrolBean.getUriSy();
        if(TextUtils.isEmpty(url)){ // 扫码后创建的空数据

        }else {
            if (null != uri) {// 拍照生成水印照片
                viewHolder.item_photo.setImageBitmap(PhotoUtils.makePhoto(mContext, uri, urlSy,
                        patrolBean.getLinePlaceName()));
                Tools.deleteFile(url);
                patrolBean.setUri(null);

            } else {
                if(null != urlSy){
                    viewHolder.item_photo.setImageBitmap(PhotoUtils.getBitmapFromUri(urlSy, mContext));
                }else {
                    viewHolder.item_photo.setBackground(mContext.getResources().getDrawable(R.drawable.image_practice_repast_1));
                }
            }
        }
    }


    @Override
    public int getItemCount() {
        return patrolBeanList != null ? patrolBeanList.size() : 0;
    }

    interface ItemClickListener{
        void itemClick(int position,PatrolBean patrolBean);
    }

    public static class VH extends RecyclerView.ViewHolder{
        TextView item_time;
        TextView item_title;
        ImageView item_photo;
        ImageView item_status_iv;
        RecyclerView recycler_sub;
        PatrolSubAdapter patrolSubAdapter;
        LinearLayout list_item_ll;
        List<PatrolBean.ProjectResult> list = new ArrayList<>();
        public VH(@NonNull View itemView) {
            super(itemView);
            item_time = itemView.findViewById(R.id.item_time);
            item_title = itemView.findViewById(R.id.item_title);
            item_photo = itemView.findViewById(R.id.item_photo);
            item_status_iv = itemView.findViewById(R.id.item_status_iv);
            recycler_sub = itemView.findViewById(R.id.recycler_sub);
            list_item_ll = itemView.findViewById(R.id.list_item_ll);

        }
    }

}
