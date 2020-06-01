package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.listener.OnItemClickListener;
import com.baidu.idl.face.main.listener.OnItemLongClickListener;
import com.baidu.idl.face.main.manager.UserInfoManager;
import com.baidu.idl.face.main.model.Group;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;

import java.util.List;

/**
 * 组列表信息页面
 * Created by v_liujialu01 on 2019/5/24.
 */

public class FaceUserGroupListActivity extends BaseActivity implements View.OnClickListener,
        OnItemClickListener, OnItemLongClickListener {
    private static final String TAG = FaceUserGroupListActivity.class.getSimpleName();

    // view
    private Button mBtnBatchOperation;
    private EditText mEditGroupSearch;
    private RecyclerView mRecyclerGroupList;
    private LinearLayout mLinearOperation;
    private RelativeLayout mRelativePop;

    private Context mContext;
    private List<Group> mListGroupInfo;
    private FaceUserGroupAdapter mFaceUserGroupAdapter;
    private UserGroupListener mUserGroupListener;

    private ButtonState mButtonState = ButtonState.BATCH_OPERATION;             // 当前按钮状态
    private boolean mIsClickAllSelected;                                        // 是否全选
    private int mSelectCount;                                                    // 选中的个数
    private int mPosition;

    private enum ButtonState {    // 判断右上角按钮的状态
        BATCH_OPERATION,         // 批量操作
        ALL_SELECT               // 全选
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_list);
        mContext = this;
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListGroupInfo != null) {
            mListGroupInfo = null;
        }

        if (mContext != null) {
            mContext = null;
        }

        if (mUserGroupListener != null) {
            mUserGroupListener = null;
        }

        UserInfoManager.getInstance().release();
    }

    private void initView() {
        // title相关
        TextView textTitle = findViewById(R.id.tv_title);
        textTitle.setText("人脸库管理");
        mBtnBatchOperation = findViewById(R.id.btn_setting);
        mBtnBatchOperation.setText("批量操作");
        mBtnBatchOperation.setOnClickListener(this);
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        mEditGroupSearch = findViewById(R.id.edit_group_search);
        mRecyclerGroupList = findViewById(R.id.recycler_group_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerGroupList.setLayoutManager(layoutManager);

        mFaceUserGroupAdapter = new FaceUserGroupAdapter();
        mRecyclerGroupList.setAdapter(mFaceUserGroupAdapter);
        mFaceUserGroupAdapter.setItemClickListener(this);
        mFaceUserGroupAdapter.setItemLongClickListener(this);

        Button btnSearch = findViewById(R.id.button_group_search);
        btnSearch.setOnClickListener(this);

        mLinearOperation = findViewById(R.id.linear_operation);
        Button btnDelete = findViewById(R.id.button_delete);
        btnDelete.setOnClickListener(this);
        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mRelativePop = findViewById(R.id.relative_pop_window);
    }

    private void initData() {
        mUserGroupListener = new UserGroupListener();
        // 读取数据库，获取用户组信息
        UserInfoManager.getInstance().getUserGroupInfo(null, mUserGroupListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_group_search:   // 根据关键字搜索用户组
                String keyWords = mEditGroupSearch.getText().toString().trim();
                if (TextUtils.isEmpty(keyWords)) {
                    ToastUtils.toast(mContext, "请输入关键字");
                    break;
                }
                // 读取数据库，获取用户组信息
                UserInfoManager.getInstance().getUserGroupInfo(keyWords, mUserGroupListener);
                break;

            case R.id.btn_back:   // 返回上一级页面
                finish();
                break;

            case R.id.btn_setting:  // 批量操作or全选
                if (mButtonState == ButtonState.BATCH_OPERATION) {      // 当前是批量操作状态
                    updateDeleteUI(true);
                } else if (mButtonState == ButtonState.ALL_SELECT) {    // 当前是全选状态
                    if (!mIsClickAllSelected) {
                        if (mListGroupInfo == null) {
                            return;
                        }
                        for (int i = 0; i < mListGroupInfo.size(); i++) {
                            mListGroupInfo.get(i).setChecked(true);
                            mSelectCount = mListGroupInfo.size();
                            mIsClickAllSelected = true;
                        }
                    } else {
                        for (int i = 0; i < mListGroupInfo.size(); i++) {
                            mListGroupInfo.get(i).setChecked(false);
                            mSelectCount = 0;
                            mIsClickAllSelected = false;
                        }
                    }
                    mFaceUserGroupAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.button_delete:    // 删除
                if (mSelectCount != 0) {
                    UserInfoManager.getInstance().deleteUserGroupListInfo(mListGroupInfo, mUserGroupListener,
                            mSelectCount);
                } else {
                    updateDeleteUI(false);
                }
                break;

            case R.id.button_cancel:   // 取消
                updateDeleteUI(false);
                break;
        }
    }

    /**
     * 删除组
     */
    public void groupDelete(View view) {
        UserInfoManager.getInstance().deleteUserGroupListInfo(mListGroupInfo.get(mPosition).getGroupId(),
                mUserGroupListener);
    }

    /**
     * 取消
     */
    public void groupCancel(View view) {
        if (mRelativePop.getVisibility() == View.VISIBLE) {
            mRelativePop.setVisibility(View.GONE);
        }
    }

    /**
     * 更新删除操作相关的UI
     *
     * @param needDelete 是否需要删除操作
     */
    private void updateDeleteUI(boolean needDelete) {
        if (needDelete) {
            mButtonState = ButtonState.ALL_SELECT;
            // 右上角按钮改为“全选”
            mBtnBatchOperation.setText("全选");
            // 列表显示复选框
            mFaceUserGroupAdapter.setShowCheckBox(true);
            mFaceUserGroupAdapter.notifyDataSetChanged();
            // 显示删除布局
            mLinearOperation.setVisibility(View.VISIBLE);
        } else {
            mButtonState = ButtonState.BATCH_OPERATION;
            // 右上角按钮改为“批量操作”
            mBtnBatchOperation.setText("批量操作");
            // 列表隐藏复选框
            mFaceUserGroupAdapter.setShowCheckBox(false);
            mFaceUserGroupAdapter.notifyDataSetChanged();
            // 隐藏删除布局
            mLinearOperation.setVisibility(View.GONE);
        }
    }

    // 用于返回读取用户组的结果
    private class UserGroupListener extends UserInfoManager.UserInfoListener {
        // 读取组列表成功
        @Override
        public void userGroupQuerySuccess(final List<Group> listGroupInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listGroupInfo == null || listGroupInfo.size() == 0) {
                        ToastUtils.toast(mContext, "暂未搜索到此用户组");
                    } else {
                        mListGroupInfo = listGroupInfo;
                    }
                    mFaceUserGroupAdapter.setDataList(listGroupInfo);

                    if (mButtonState == ButtonState.ALL_SELECT) {
                        updateDeleteUI(false);
                        ToastUtils.toast(mContext, "删除成功");
                    } else {
                        mFaceUserGroupAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        // 读取组列表失败
        @Override
        public void userGroupQueryFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    ToastUtils.toast(mContext, message);
                }
            });
        }

        // 删除组列表成功
        @Override
        public void userGroupDeleteSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.toast(mContext, "删除组列表成功");
                    UserInfoManager.getInstance().getUserGroupInfo(null, mUserGroupListener);
                    if (mRelativePop.getVisibility() == View.VISIBLE) {
                        mRelativePop.setVisibility(View.GONE);
                    }
                }
            });
        }

        // 删除组列表失败
        @Override
        public void userGroupDeleteFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    ToastUtils.toast(mContext, message);
                }
            });
        }
    }

    // ----------------------------------------adapter相关------------------------------------------
    private static class FaceUserGroupViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView textGroupName;
        private TextView textGroupCtime;
        private CheckBox checkView;

        private FaceUserGroupViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textGroupName = itemView.findViewById(R.id.text_group_name);
            textGroupCtime = itemView.findViewById(R.id.text_group_ctime);
            checkView = itemView.findViewById(R.id.check_btn);
        }
    }

    public class FaceUserGroupAdapter extends RecyclerView.Adapter<FaceUserGroupViewHolder> implements
            View.OnClickListener, View.OnLongClickListener {
        private List<Group> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;

        private void setDataList(List<Group> list) {
            mList = list;
        }

        private void setShowCheckBox(boolean showCheckBox) {
            mShowCheckBox = showCheckBox;
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        private void setItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
            mItemLongClickListener = itemLongClickListener;
        }

        @Override
        public FaceUserGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_list, parent, false);
            FaceUserGroupViewHolder viewHolder = new FaceUserGroupViewHolder(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final FaceUserGroupViewHolder holder, final int position) {
            holder.itemView.setTag(position);
            // 是否显示多选按钮
            if (mShowCheckBox) {
                holder.checkView.setVisibility(View.VISIBLE);
                if (mList.get(position).isChecked()) {
                    holder.checkView.setChecked(true);
                } else {
                    holder.checkView.setChecked(false);
                }
            } else {
                holder.checkView.setVisibility(View.GONE);
            }
            // 添加数据
            holder.textGroupName.setText(mList.get(position).getGroupId() + " >");
            String ctime = Utils.formatTime(mList.get(position).getCtime(), "yyyy.MM.dd");
            holder.textGroupCtime.setText("创建时间：" + ctime);
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, (Integer) view.getTag());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mItemLongClickListener != null) {
                mItemLongClickListener.onLongItemClick(view, (Integer) view.getTag());
            }
            return true;
        }
    }

    // 用于adapter的item点击事件
    @Override
    public void onItemClick(View view, int position) {
        // 如果是全选状态
        if (mButtonState == ButtonState.ALL_SELECT) {
            // 如果当前item未选中，则选中
            if (!mListGroupInfo.get(position).isChecked()) {
                mListGroupInfo.get(position).setChecked(true);
                mSelectCount++;
            } else {
                // 如果当前item已经选中，则取消选中
                mListGroupInfo.get(position).setChecked(false);
                mSelectCount--;
            }
            mFaceUserGroupAdapter.notifyDataSetChanged();
        } else {
            Intent intent = new Intent(mContext, FaceUserListActivity.class);
            intent.putExtra("group_id", mListGroupInfo.get(position).getGroupId());
            startActivity(intent);
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        mPosition = position;
        if (mRelativePop.getVisibility() == View.GONE) {
            mRelativePop.setVisibility(View.VISIBLE);
        }
    }
}
