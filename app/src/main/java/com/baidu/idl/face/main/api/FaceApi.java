/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.face.main.api;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.baidu.idl.face.main.db.DBManager;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.Group;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGetRecords;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceApi {
    private static FaceApi instance;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Future future;

    private int mUserNum;
    private boolean isinitSuccess = false;


    private FaceApi() {

    }

    public static synchronized FaceApi getInstance() {
        if (instance == null) {
            instance = new FaceApi();
        }
        return instance;
    }

    /**
     * 添加用户组
     */
    public boolean groupAdd(Group group) {
        if (group == null || TextUtils.isEmpty(group.getGroupId())) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(group.getGroupId());
        if (!matcher.matches()) {
            return false;
        }
        boolean ret = DBManager.getInstance().addGroup(group);

        return ret;
    }

    /**
     * 查询用户组（默认最多取1000个组）
     */
    public List<Group> getGroupList(int start, int length) {
        if (start < 0 || length < 0) {
            return null;
        }
        if (length > 1000) {
            length = 1000;
        }
        List<Group> groupList = DBManager.getInstance().queryGroups(start, length);
        return groupList;
    }

    /**
     * 根据groupId查询用户组
     */
    public List<Group> getGroupListByGroupId(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        return DBManager.getInstance().queryGroupsByGroupId(groupId);
    }

    /**
     * 根据groupId删除用户组
     */
    public boolean groupDelete(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return false;
        }
        boolean ret = DBManager.getInstance().deleteGroup(groupId);
        return ret;
    }

    /**
     * 添加用户
     */
    public boolean userAdd(User user) {
        if (user == null || TextUtils.isEmpty(user.getGroupId())) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(user.getUserId());
        if (!matcher.matches()) {
            return false;
        }
        boolean ret = DBManager.getInstance().addUser(user);

        return ret;
    }

    /**
     * 根据groupId查找用户
     */
    public List<User> getUserList(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        List<User> userList = DBManager.getInstance().queryUserByGroupId(groupId);
        return userList;
    }

    /**
     * 根据groupId、userName查找用户
     */
    public List<User> getUserListByUserName(String groupId, String userName) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userName)) {
            return null;
        }
        List<User> userList = DBManager.getInstance().queryUserByUserName(groupId, userName);
        return userList;
    }

    /**
     * 根据_id查找用户
     */
    public User getUserListById(int _id) {
        if (_id < 0) {
            return null;
        }
        List<User> userList = DBManager.getInstance().queryUserById(_id);
        if (userList != null && userList.size() > 0) {
            return userList.get(0);
        }
        return null;
    }

    /**
     * 更新用户
     */
    public boolean userUpdate(User user) {
        if (user == null) {
            return false;
        }

        boolean ret = DBManager.getInstance().updateUser(user);
        return ret;
    }

    /**
     * 更新用户
     */
    public boolean userUpdate(String groupId, String userName, String imageName, byte[] feature) {
        if (groupId == null || userName == null || imageName == null || feature == null) {
            return false;
        }

        boolean ret = DBManager.getInstance().updateUser(groupId, userName, imageName, feature);
        return ret;
    }

    /**
     * 删除用户
     */
    public boolean userDelete(String userId, String groupId) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(groupId)) {
            return false;
        }

        boolean ret = DBManager.getInstance().deleteUser(userId, groupId);
        return ret;
    }

    /**
     * 远程删除用户
     */
    public boolean userDeleteByName(String userName, String groupId) {
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(groupId)) {
            return false;
        }

        boolean ret = DBManager.getInstance().userDeleteByName(userName, groupId);
        return ret;
    }

    /**
     * 是否是有效姓名
     *
     * @param username 用户名
     * @return 有效或无效信息
     */
    public String isValidName(String username) {
        if (username == null || "".equals(username.trim())) {
            return "姓名为空";
        }

        // 姓名过长
        if (username.length() > 10) {
            return "姓名过长";
        }

        // 含有特殊符号
        String regex = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）—"
                + "—+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        if (m.find()) {
            return "姓名中含有特殊符号";
        }
        return "0";
    }

    /**
     * 提取特征值
     */
    public float getFeature(Bitmap bitmap, byte[] feature, BDFaceSDKCommon.FeatureType featureType) {
        if (bitmap == null) {
            return -1;
        }

        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        // 最大检测人脸，获取人脸信息
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            // 人脸识别，提取人脸特征值
            ret = FaceSDKManager.getInstance().getFaceFeature().feature(
                    featureType, imageInstance,
                    faceInfo.landmarks, feature);
        }
        imageInstance.destory();
        return ret;
    }


    public boolean registerUserIntoDBmanager(String groupName, String userName, String picName,
                                             String userInfo, byte[] faceFeature) {
        boolean isSuccess = false;

        Group group = new Group();
        group.setGroupId(groupName);

        User user = new User();
        user.setGroupId(groupName);
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         */
        final String uid = UUID.randomUUID().toString();
        user.setUserId(uid);
        user.setUserName(userName);
        user.setFeature(faceFeature);
        user.setImageName(picName);
        if (userInfo != null) {
            user.setUserInfo(userInfo);
        }
        // 添加用户信息到数据库
        boolean importUserSuccess = FaceApi.getInstance().userAdd(user);
        if (importUserSuccess) {
            // 如果添加到数据库成功，则添加用户组信息到数据库
            // 如果当前图片组名和上一张图片组名相同，则不添加数据库到组表
            if (FaceApi.getInstance().groupAdd(group)) {
                isSuccess = true;
            } else {
                isSuccess = false;
            }
        } else {
            isSuccess = false;
        }

        return isSuccess;
    }

    /**
     * 获取底库数量
     *
     * @return
     */
    public int getmUserNum() {
        return mUserNum;
    }

    public boolean isinitSuccess() {
        return isinitSuccess;
    }

    /**
     * 数据库发现变化时候，重新把数据库中的人脸信息添加到内存中，id+feature
     */
    public void initDatabases(final boolean isFeaturePush) {

        if (future != null && !future.isDone()) {
            future.cancel(true);
        }

        isinitSuccess = false;
        future = es.submit(new Runnable() {
            @Override
            public void run() {
                List<Group> listGroup = FaceApi.getInstance().getGroupList(0, 100);
                if (listGroup != null && listGroup.size() > 0) {
                    ArrayList<Feature> features = new ArrayList<>();
                    for (int i = 0; i < listGroup.size(); i++) {
                        List<User> listUser = FaceApi.getInstance().getUserList(listGroup.get(i).getGroupId());
                        for (int j = 0; j < listUser.size(); j++) {
                            Feature feature = new Feature();
                            feature.setId(listUser.get(j).getId());
                            feature.setFeature(listUser.get(j).getFeature());
                            features.add(feature);
                        }
                    }
                    if (isFeaturePush) {
                        FaceSDKManager.getInstance().getFaceFeature().featurePush(features);
                    }
                    mUserNum = features.size();
                }
                isinitSuccess = true;
            }
        });
    }


    // 查询识别记录
    public List<ResponseGetRecords> getRecords(String startTime, String endTime) {
//        if (TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)) {
//            return null;
//        }

        List<ResponseGetRecords> responseGetRecords = DBManager.getInstance().queryRecords(startTime, endTime);
        if (responseGetRecords != null && responseGetRecords.size() > 0) {
            return responseGetRecords;
        }

        return null;
    }

    // 添加识别记录
    public boolean addRecords(ResponseGetRecords responseGetRecords) {
        boolean ret = false;
        if (responseGetRecords == null) {
            return ret;
        }
        ret = DBManager.getInstance().addResponseGetRecords(responseGetRecords);
        return ret;
    }

    // 删除识别记录
    public boolean deleteRecords(String userName) {
        boolean ret = false;
        if (TextUtils.isEmpty(userName)) {
            return ret;
        }
        ret = DBManager.getInstance().deleteRecords(userName);
        return ret;
    }

    // 删除识别记录
    public boolean deleteRecords(String startTime, String endTime) {
        boolean ret = false;
        if (TextUtils.isEmpty(startTime) && TextUtils.isEmpty(endTime)) {
            return ret;
        }
        ret = DBManager.getInstance().deleteRecords(startTime, endTime);
        return ret;
    }

    // 清除识别记录
    public int cleanRecords() {
        boolean ret = false;
        int num = DBManager.getInstance().cleanRecords();
        return num;
    }


}
