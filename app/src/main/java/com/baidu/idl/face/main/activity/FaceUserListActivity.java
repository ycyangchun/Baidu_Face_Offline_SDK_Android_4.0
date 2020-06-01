package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.listener.OnItemClickListener;
import com.baidu.idl.face.main.manager.UserInfoManager;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.facesdkdemo.R;

import java.util.List;

/**
 * 用户列表信息页面
 * Created by v_liujialu01 on 2019/5/24.
 */

public class FaceUserListActivity extends BaseActivity implements View.OnClickListener,
        OnItemClickListener {
    private static final String TAG = FaceUserListActivity.class.getSimpleName();

    // view
    private Button mBtnBatchOperation;
    private EditText mEditUserSearch;
    private RecyclerView mRecyclerUserList;
    private TextView mTextGroupName;
    private LinearLayout mLinearOperation;

    private Context mContext;
    private List<User> mListUserInfo;
    private FaceUserAdapter mFaceUserAdapter;
    private UserListListener mUserListListener;

    private String mGroupId;
    private ButtonState mButtonState = ButtonState.BATCH_OPERATION;             // 当前按钮状态
    private boolean mIsClickAllSelected;                                        // 是否全选
    private int mSelectCount;                                                    // 选中的个数

    private enum ButtonState {
        BATCH_OPERATION,
        ALL_SELECT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        mContext = this;
        initView();
        initData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 读取数据库，获取用户信息
        UserInfoManager.getInstance().getUserListInfoByGroupId(null, mGroupId,
                mUserListListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListUserInfo != null) {
            mListUserInfo = null;
        }

        if (mContext != null) {
            mContext = null;
        }

        if (mUserListListener != null) {
            mUserListListener = null;
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

        mTextGroupName = findViewById(R.id.text_group_name);
        mEditUserSearch = findViewById(R.id.edit_user_search);
        mRecyclerUserList = findViewById(R.id.recycler_user_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerUserList.setLayoutManager(layoutManager);

        mFaceUserAdapter = new FaceUserAdapter();
        mRecyclerUserList.setAdapter(mFaceUserAdapter);
        mFaceUserAdapter.setItemClickListener(this);

        Button btnSearch = findViewById(R.id.button_user_search);
        btnSearch.setOnClickListener(this);

        mLinearOperation = findViewById(R.id.linear_operation);
        Button btnDelete = findViewById(R.id.button_delete);
        btnDelete.setOnClickListener(this);
        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mGroupId = intent.getStringExtra("group_id");
            mTextGroupName.setText("组：" + mGroupId);
        }

        mUserListListener = new UserListListener();
        // 读取数据库，获取用户信息
        UserInfoManager.getInstance().getUserListInfoByGroupId(null, mGroupId,
                mUserListListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_user_search:   // 根据关键字搜索用户
                String keyWords = mEditUserSearch.getText().toString().trim();
                if (TextUtils.isEmpty(keyWords)) {
                    ToastUtils.toast(mContext, "请输入关键字");
                    break;
                }
                // 读取数据库，获取用户信息
                UserInfoManager.getInstance().getUserListInfoByGroupId(keyWords, mGroupId, mUserListListener);
                break;

            case R.id.btn_back:   // 返回上一级页面
                finish();
                break;

            case R.id.btn_setting:  // 批量操作/全选
                if (mButtonState == ButtonState.BATCH_OPERATION) {      // 当前是批量操作状态
                    updateDeleteUI(true);
                } else if (mButtonState == ButtonState.ALL_SELECT) {    // 当前是全选状态
                    if (!mIsClickAllSelected) {
                        for (int i = 0; i < mListUserInfo.size(); i++) {
                            mListUserInfo.get(i).setChecked(true);
                            mSelectCount = mListUserInfo.size();
                            mIsClickAllSelected = true;
                        }
                    } else {
                        for (int i = 0; i < mListUserInfo.size(); i++) {
                            mListUserInfo.get(i).setChecked(false);
                            mSelectCount = 0;
                            mIsClickAllSelected = false;
                        }
                    }
                    mFaceUserAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.button_delete:    // 删除
                if (mSelectCount != 0) {
                    UserInfoManager.getInstance().deleteUserListInfo(mListUserInfo, mUserListListener,
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

    private void updateDeleteUI(boolean needDelete) {
        if (needDelete) {
            mButtonState = ButtonState.ALL_SELECT;
            // 右上角按钮改为“全选”
            mBtnBatchOperation.setText("全选");
            // 列表显示复选框
            mFaceUserAdapter.setShowCheckBox(true);
            mFaceUserAdapter.notifyDataSetChanged();
            // 显示删除布局
            mLinearOperation.setVisibility(View.VISIBLE);
        } else {
            mButtonState = ButtonState.BATCH_OPERATION;
            // 右上角按钮改为“批量操作”
            mBtnBatchOperation.setText("批量操作");
            // 列表隐藏复选框
            mFaceUserAdapter.setShowCheckBox(false);
            mFaceUserAdapter.notifyDataSetChanged();
            // 隐藏删除布局
            mLinearOperation.setVisibility(View.GONE);
        }
    }

    // 用于返回读取用户的结果
    private class UserListListener extends UserInfoManager.UserInfoListener {
        // 读取用户列表成功
        @Override
        public void userListQuerySuccess(final List<User> listUserInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listUserInfo == null || listUserInfo.size() == 0) {
                        ToastUtils.toast(mContext, "暂未搜索到此用户");
                    } else {
                        mListUserInfo = listUserInfo;
                    }
                    mFaceUserAdapter.setDataList(listUserInfo);

                    if (mButtonState == ButtonState.ALL_SELECT) {
                        updateDeleteUI(false);
                        ToastUtils.toast(mContext, "删除成功");
                    } else {
                        mFaceUserAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        // 读取用户列表失败
        @Override
        public void userListQueryFailure(final String message) {
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

        // 删除用户列表成功
        @Override
        public void userListDeleteSuccess() {
            // 数据变化，更新内存
            FaceApi.getInstance().initDatabases(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UserInfoManager.getInstance().getUserListInfoByGroupId(null, mGroupId, mUserListListener);
                }
            });
        }

        // 删除用户列表失败
        @Override
        public void userListDeleteFailure(final String message) {
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
    private static class FaceUserViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private CircleImageView circleUserHead;
        private TextView textUserName;
        private TextView textUserCtime;
        private TextView textUserInfo;
        private CheckBox checkView;

        private FaceUserViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            circleUserHead = itemView.findViewById(R.id.circle_user);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textUserInfo = itemView.findViewById(R.id.text_user_info);
            textUserCtime = itemView.findViewById(R.id.text_user_ctime);
            checkView = itemView.findViewById(R.id.check_btn);
        }
    }

    public class FaceUserAdapter extends RecyclerView.Adapter<FaceUserViewHolder> implements
            View.OnClickListener {
        private List<User> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;

        private void setDataList(List<User> list) {
            mList = list;
        }

        private void setShowCheckBox(boolean showCheckBox) {
            mShowCheckBox = showCheckBox;
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @Override
        public FaceUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_list, parent, false);
            FaceUserViewHolder viewHolder = new FaceUserViewHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FaceUserViewHolder holder, int position) {
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
            holder.textUserName.setText(mList.get(position).getUserName());
            String ctime = Utils.formatTime(mList.get(position).getCtime(), "yyyy.MM.dd HH:mm:ss");
            String userInfo = mList.get(position).getUserInfo();
            if (!TextUtils.isEmpty(userInfo)) {
                holder.textUserInfo.setText(userInfo);
            } else {
                holder.textUserInfo.setText("");
            }
            holder.textUserCtime.setText("创建时间：" + ctime);
            Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getBatchImportSuccessDirectory()
                    + "/" + mList.get(position).getImageName());
            holder.circleUserHead.setImageBitmap(bitmap);
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
    }

    // 用于adapter的item点击事件
    @Override
    public void onItemClick(View view, int position) {
        // 如果是全选状态
        if (mButtonState == ButtonState.ALL_SELECT) {
            // 如果当前item未选中，则选中
            if (!mListUserInfo.get(position).isChecked()) {
                mListUserInfo.get(position).setChecked(true);
                mSelectCount++;
            } else {
                // 如果当前item已经选中，则取消选中
                mListUserInfo.get(position).setChecked(false);
                mSelectCount--;
            }
            mFaceUserAdapter.notifyDataSetChanged();
        } else {
            Intent intent = new Intent(mContext, FaceUserInfoActivity.class);
            intent.putExtra("group_id", mGroupId);
            intent.putExtra("user_name", mListUserInfo.get(position).getUserName());
            intent.putExtra("user_info", mListUserInfo.get(position).getUserInfo());
            intent.putExtra("ctime", mListUserInfo.get(position).getCtime());
            intent.putExtra("user_pic", mListUserInfo.get(position).getImageName());
            intent.putExtra("user_id", mListUserInfo.get(position).getUserId());
            startActivity(intent);
        }
    }
}
