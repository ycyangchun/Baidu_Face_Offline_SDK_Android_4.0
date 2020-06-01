package com.baidu.idl.face.main.manager;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.model.Group;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.LogUtils;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户管理
 * Created by v_liujialu01 on 2018/12/14.
 */

public class UserInfoManager {
    private static final String TAG = UserInfoManager.class.getSimpleName();
    private ExecutorService mExecutorService = null;

    // 私有构造
    private UserInfoManager() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    private static class HolderClass {
        private static final UserInfoManager instance = new UserInfoManager();
    }

    public static UserInfoManager getInstance() {
        return HolderClass.instance;
    }

    /**
     * 释放
     */
    public void release() {
        LogUtils.i(TAG, "release");
    }

    /**
     * 获取组列表信息
     */
    public void getUserGroupInfo(final String groupId, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                // 如果关键字为null，则全局查找
                if (groupId == null) {
                    listener.userGroupQuerySuccess(FaceApi.getInstance().getGroupList(0, 100));
                } else {
                    // 如果关键字不为null，则按关键字查找
                    if (TextUtils.isEmpty(groupId)) {
                        listener.userGroupQueryFailure("请输入关键字");
                        return;
                    }
                    listener.userGroupQuerySuccess(FaceApi.getInstance().getGroupListByGroupId(groupId));
                }
            }
        });
    }

    /**
     * 删除用户组列表信息
     */
    public void deleteUserGroupListInfo(final List<Group> list, final UserInfoListener listener, final int selectCount) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (list == null) {
                    listener.userGroupDeleteFailure("参数异常");
                    return;
                }

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isChecked()) {
                        FaceApi.getInstance().groupDelete(list.get(i).getGroupId());
                    }
                }
                listener.userGroupDeleteSuccess();
            }
        });
    }

    /**
     * 删除用户组列表信息
     */
    public void deleteUserGroupListInfo(final String groupId, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (groupId == null) {
                    listener.userGroupDeleteFailure("参数异常");
                    return;
                }

                FaceApi.getInstance().groupDelete(groupId);
                listener.userGroupDeleteSuccess();
            }
        });
    }

    /**
     * 获取用户列表信息
     */
    public void getUserListInfoByGroupId(final String userName, final String groupId,
                                         final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (groupId == null || "".equals(groupId)) {
                    listener.userListQueryFailure("groupId为空");
                    return;
                }

                // 如果关键字为null，则全局查找
                if (userName == null) {
                    listener.userListQuerySuccess(FaceApi.getInstance().getUserList(groupId));
                } else {
                    // 如果关键字不为null，则按关键字查找
                    if (TextUtils.isEmpty(userName)) {
                        listener.userListQueryFailure("请输入关键字");
                        return;
                    }
                    listener.userListQuerySuccess(FaceApi.getInstance().getUserListByUserName(groupId, userName));
                }
            }
        });
    }

    /**
     * 删除用户信息
     */
    public void deleteUserInfo(final String userId, final String groupId, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                boolean success = FaceApi.getInstance().userDelete(userId, groupId);
                if (success) {
                    listener.userDeleteSuccess();
                } else {
                    listener.userDeleteFailure("删除用户失败");
                }
            }
        });
    }

    /**
     * 删除用户列表信息
     */
    public void deleteUserListInfo(final List<User> list, final UserInfoListener listener, final int selectCount) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (list == null) {
                    listener.userListDeleteFailure("参数异常");
                    return;
                }

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isChecked()) {
                        FaceApi.getInstance().userDelete(list.get(i).getUserId(), list.get(i).getGroupId());
                    }
                }
                listener.userListDeleteSuccess();
            }
        });
    }

    /**
     * 更换图片
     */
    public void updateImage(final Bitmap bitmap, final String groupId, final String userName,
                            final String imageName, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if(listener == null) {
                    return;
                }

                if (bitmap == null || imageName == null) {
                    listener.updateImageFailure("参数异常");
                    return;
                }

                byte[] bytes = new byte[512];
                float ret = -1;
                // 检测人脸，提取人脸特征值
                ret = FaceApi.getInstance().getFeature(bitmap, bytes,
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
                if (ret == -1) {
                    listener.updateImageFailure("未检测到人脸，可能原因：人脸太小");
                } else if (ret == 128) {
                    // 添加用户信息到数据库
                    boolean update = FaceApi.getInstance().userUpdate(groupId, userName, imageName, bytes);
                    if (update) {
                        // 保存图片到新目录中
                        File facePicDir = FileUtils.getBatchImportSuccessDirectory();
                        if (facePicDir != null) {
                            File savePicPath = new File(facePicDir, imageName);
                            if (FileUtils.saveBitmap(savePicPath, bitmap)) {
                                listener.updateImageSuccess(bitmap);
                            } else {
                                listener.updateImageFailure("图片保存失败");
                            }
                        }
                    } else {
                        listener.updateImageFailure("更新数据库失败");
                    }
                } else {
                    listener.updateImageFailure("未检测到人脸");
                }
            }
        });
    }

    public static class UserInfoListener {
        public void userGroupQuerySuccess(List<Group> listGroupInfo) {
            // 用户组列表查询成功
        }

        public void userGroupQueryFailure(String message) {
            // 用户组列表查询失败
        }

        public void userListQuerySuccess(List<User> listUserInfo) {
            // 用户列表查询成功
        }

        public void userListQueryFailure(String message) {
            // 用户列表查询失败
        }

        public void userGroupDeleteSuccess() {
            // 用户组列表删除成功
        }

        public void userGroupDeleteFailure(String message) {
            // 用户组列表删除失败
        }

        public void userListDeleteSuccess() {
            // 用户列表删除成功
        }

        public void userListDeleteFailure(String message) {
            // 用户列表删除失败
        }

        public void updateImageSuccess(Bitmap bitmap) {
            // 更新图片成功
        }

        public void updateImageFailure(String message) {
            // 更新图片失败
        }

        public void userDeleteSuccess() {
            // 用户删除成功
        }

        public void userDeleteFailure(String message) {
            // 用户删除失败
        }
    }
}
