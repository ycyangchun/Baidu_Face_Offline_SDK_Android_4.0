package com.baidu.idl.face.main.socket;


/**
 * Socket通讯的代码引用于SocketClient.jar,来源于https://github.com/vilyever/AndroidSocketClient
 * http://www.apache.org/licenses/LICENSE-2.0
 */


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.manager.RecordLogManager;
import com.baidu.idl.face.main.model.BaseConfig;
import com.baidu.idl.face.main.model.Group;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.socket.socketmodel.request.CleanLog;
import com.baidu.idl.face.main.socket.socketmodel.request.DeleteRecords;
import com.baidu.idl.face.main.socket.socketmodel.request.GetGroupList;
import com.baidu.idl.face.main.socket.socketmodel.request.GetRecords;
import com.baidu.idl.face.main.socket.socketmodel.request.GetUserInfo;
import com.baidu.idl.face.main.socket.socketmodel.request.GetUserList;
import com.baidu.idl.face.main.socket.socketmodel.request.GroupAdd;
import com.baidu.idl.face.main.socket.socketmodel.request.GroupDelete;
import com.baidu.idl.face.main.socket.socketmodel.request.RecordList;
import com.baidu.idl.face.main.socket.socketmodel.request.UserAdd;
import com.baidu.idl.face.main.socket.socketmodel.request.UserCopy;
import com.baidu.idl.face.main.socket.socketmodel.request.UserDelete;
import com.baidu.idl.face.main.socket.socketmodel.request.UserUpdate;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponeseUnKonwError;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseAddAndUpdate;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseCleanLog;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseDeleteRecords;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseDevice;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGetGroupList;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGetRecords;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGetUserInfo;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGetUserList;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGroupAdd;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGroupDelete;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseResetConfigFile;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseUserCopy;
import com.baidu.idl.face.main.socket.socketmodel.response.ResponseUserDelete;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.vis.unified.license.AndroidLicenser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.SocketResponsePacket;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;


/**
 * author : baidu
 * date : 2019/8/12 4:02 PM
 * description :
 */
public class SocketService extends Service {

    private SocketClient socketClient;
    // 填写自己和Socket通讯IP地址
    private String ip = "192.168.43.139";
    // 填写自己和Socket通讯端口
    private int port = 8090;
    // 通知服务端操作结果
    public String result = "";
    public String deviceid = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        socktest();
        return super.onStartCommand(intent, flags, startId);
    }

    public void socktest() {
        socketClient = new SocketClient(ip, port);
        socketClient.registerSocketDelegate(socketDelegate);
        socketClient.setConnectionTimeout(1000 * 15);
        socketClient.setHeartBeatInterval(1000 * 10);
        socketClient.setHeartBeatMessage("heart");
        socketClient.setRemoteNoReplyAliveTimeout(1000 * 60);
        socketClient.setCharsetName("UTF-8");
        socketClient.connect();
    }


    // Socket回调
    public SocketClient.SocketDelegate socketDelegate = new SocketClient.SocketDelegate() {
        @Override
        public void onConnected(SocketClient client) {
            Log.e("shang", "socket连接");
            deviceid = AndroidLicenser.getDeviceId(getApplicationContext());
            sendMessage(RequestConfig.sendDeviceId, deviceid, "SUCCESS", null);
        }

        @Override
        public void onDisconnected(SocketClient client) {
            Log.i("shang", "socket断开");
            socketClient.connect();
        }

        @Override
        public void onResponse(SocketClient client, SocketResponsePacket responsePacket) {
            String responseMsg = responsePacket.getMessage().replaceAll("\r|\n", "");
            if (responseMsg.equals("heart")) {
                Log.e("shang", "心跳");
                return;
            }
            choiceMessage(responseMsg);

            Log.e("shang", "接受到：" + responseMsg);
        }
    };

    public void choiceMessage(String messsage) {

        // 数据库操作是否成功
        boolean isSuccess = false;

        try {
            JSONObject jsonObject = new JSONObject(messsage);
            final int code = Integer.valueOf(jsonObject.getInt("code"));

            if (code == RequestConfig.resetConfigFile) {
                String isSucess = "";
                Type type = new TypeToken<SocketResult<BaseConfig>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);

                SingleBaseConfig.copyInstance((com.baidu.idl.face.main.model.BaseConfig) socketResult.getData());
                try {
                    isSucess = String.valueOf(ConfigUtils.modityJson());
                } catch (Exception e) {
                    isSucess = e.getMessage();
                }
                sendMessage(code, SingleBaseConfig.getBaseConfig().getdPass(), String.valueOf(isSucess), null);
            }

            if (code == RequestConfig.userFaceAdd) {
                float ret = -1;
                Type type = new TypeToken<SocketResult<UserAdd>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                final UserAdd userAdd = (UserAdd) socketResult.getData();

                BDFaceImageInstance bdFaceImageInstance = new
                        BDFaceImageInstance(BitmapUtils.base64ToBitmap(userAdd.getImage()));
                FaceInfo[] faceInfos = FaceSDKManager.getInstance()
                        .getFaceDetect().detect(BDFaceSDKCommon.DetectType.DETECT_VIS, bdFaceImageInstance);

                byte[] faceBytes = new byte[512];
                if (faceInfos != null && faceInfos.length == 1) {
                    ret = FaceSDKManager.getInstance().getFaceFeature()
                            .feature(BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                                    bdFaceImageInstance, faceInfos[0].landmarks, faceBytes);
                    if (ret != 128) {
                        result = "图片提取特征值失败";
                    }
                } else {
                    result = "图片没有检测到人脸,";
                }

                if (ret == 128) {
                    // 注册到人脸库
                    isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(userAdd.getGroupId(),
                            userAdd.getUserName(), userAdd.getImageName(),
                            userAdd.getUserInfo(), faceBytes);
                }

                if (isSuccess) {
                    File faceDir = FileUtils.getBatchImportSuccessDirectory();
                    File file = new File(faceDir, userAdd.getImageName());
                    ImageUtils.resize(BitmapUtils.base64ToBitmap(userAdd.getImage()), file,
                            300, 300);
                    FaceApi.getInstance().initDatabases(true);
                    result = "SUCCESS";
                } else {
                    result += "注册到人脸库失败";
                }
                sendMessage(code, userAdd.getDeviceId(), result, null);

            }

            if (code == RequestConfig.userUpdate) {
                float ret = -1;
                Type type = new TypeToken<SocketResult<UserUpdate>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                UserUpdate userUpdate = (UserUpdate) socketResult.getData();

                BDFaceImageInstance bdFaceImageInstance = new
                        BDFaceImageInstance(BitmapUtils.base64ToBitmap(userUpdate.getImage()));
                FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                        .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, bdFaceImageInstance);

                byte[] faceBytes = new byte[512];
                if (faceInfos != null && faceInfos.length == 1) {
                    ret = FaceSDKManager.getInstance().getFaceFeature().
                            feature(BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                                    bdFaceImageInstance, faceInfos[0].landmarks, faceBytes);
                    if (ret != 128) {
                        result = "图片提取特征值失败";
                    }
                } else {
                    result = "图片没有检测到人脸,";
                }
                if (ret == 128) {
                    isSuccess = FaceApi.getInstance().userUpdate(userUpdate.getGroupId(),
                            userUpdate.getUserName(), userUpdate.getImageName(), faceBytes);
                }

                if (isSuccess) {
                    File faceDir = FileUtils.getBatchImportSuccessDirectory();
                    File file = new File(faceDir, userUpdate.getImageName());
                    ImageUtils.resize(BitmapUtils.base64ToBitmap(userUpdate.getImage()), file, 300, 300);
                    FaceApi.getInstance().initDatabases(true);
                    result = "SUCCESS";
                } else {
                    result += "更新到人脸库失败";
                }

                sendMessage(code, userUpdate.getDeviceId(), result, null);
            }

            if (code == RequestConfig.userDelete) {
                Type type = new TypeToken<SocketResult<UserDelete>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                UserDelete userDelete = (UserDelete) socketResult.getData();

                isSuccess = FaceApi.getInstance().userDeleteByName(userDelete.getUserName(), userDelete.getGroupId());
                if (isSuccess) {
                    result = "SUCCESS";
                } else {
                    result = "Fail";
                }
                sendMessage(code, userDelete.getDeviceId(), result, null);
            }
            if (code == RequestConfig.userCopy) {
                boolean isExitGroup = false;
                Type type = new TypeToken<SocketResult<UserCopy>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                UserCopy userCopy = (UserCopy) socketResult.getData();

                List<Group> list = FaceApi.getInstance().getGroupList(0, 1000);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getGroupId().contains(userCopy.getDstGroupId())) {
                        isExitGroup = true;
                    }
                }
                if (!isExitGroup) {
                    Group group = new Group();
                    group.setGroupId(userCopy.getDstGroupId());
                    group.setCtime(System.currentTimeMillis());
                    group.setDesc("");
                    FaceApi.getInstance().groupAdd(group);
                }

                List<User> listUser = FaceApi.getInstance()
                        .getUserListByUserName(userCopy.getSrcGroupId(), userCopy.getUserName());
                if (listUser != null && listUser.size() > 0) {
                    User user = listUser.get(0);
                    user.setGroupId(userCopy.getDstGroupId());
                    isSuccess = FaceApi.getInstance().userAdd(user);
                    if (isSuccess) {
                        FaceApi.getInstance().initDatabases(true);
                        result = "SUCCESS";
                    }
                } else {
                    result = "要复制的人不存在";
                }
                sendMessage(code, userCopy.getDeviceId(), result, null);
            }
            if (code == RequestConfig.groupAdd) {
                Type type = new TypeToken<SocketResult<GroupAdd>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                GroupAdd groupAdd = (GroupAdd) socketResult.getData();
                Group group = new Group();
                group.setGroupId(groupAdd.getGroupId());
                group.setCtime(System.currentTimeMillis());
                group.setDesc("");
                isSuccess = FaceApi.getInstance().groupAdd(group);
                if (isSuccess) {
                    result = "SUCCESS";
                } else {
                    result = "Fail";
                }
                sendMessage(code, groupAdd.getDeviceId(), result, null);
            }
            if (code == RequestConfig.groupDelete) {
                Type type = new TypeToken<SocketResult<GroupDelete>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                GroupDelete groupDelete = (GroupDelete) socketResult.getData();

                isSuccess = FaceApi.getInstance().groupDelete(groupDelete.getGroupId());
                if (isSuccess) {
                    result = "SUCCESS";
                } else {
                    result = "Fail";
                }
                sendMessage(code, groupDelete.getDeviceId(), result, null);
            }
            if (code == RequestConfig.getUserInfo) {
                Type type = new TypeToken<SocketResult<GetUserInfo>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                GetUserInfo getUserInfo = (GetUserInfo) socketResult.getData();

                List<User> list = FaceApi.getInstance().getUserListByUserName(getUserInfo.getGroupId()
                        , getUserInfo.getUserName());
                if (list.size() > 0) {
                    result = "SUCCESS";
                    sendMessage(code, getUserInfo.getDeviceId(), result, list.get(0));
                } else {
                    result = "Fail";
                    sendMessage(code, getUserInfo.getDeviceId(), result, null);
                }

            }
            if (code == RequestConfig.getUserList) {

                Type type = new TypeToken<SocketResult<GetUserList>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                GetUserList getUserList = (GetUserList) socketResult.getData();

                List<User> list = FaceApi.getInstance().getUserList(getUserList.getGroupId());
                String[] array = new String[list.size()];

                if (Integer.valueOf(getUserList.getStart()) > Integer.valueOf(getUserList.getLength())) {
                    result = "Fail";
                    sendMessage(code, getUserList.getDeviceId(), result, array);
                    return;
                }
                int length;
                if (list.size() < Integer.valueOf(getUserList.getLength())) {
                    length = list.size();
                } else {
                    length = Integer.valueOf(getUserList.getLength());
                }
                for (int i = Integer.valueOf(getUserList.getStart()); i < length; i++) {
                    array[i] = list.get(i).getUserId();
                }

                if (array.length != 0) {
                    result = "SUCCESS";
                } else {
                    result = "Fail";
                }
                sendMessage(code, getUserList.getDeviceId(), result, array);
            }

            if (code == RequestConfig.getGroupList) {
                Type type = new TypeToken<SocketResult<GetGroupList>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                GetGroupList getGroupList = (GetGroupList) socketResult.getData();

                if (Integer.valueOf(getGroupList.getStart()) > Integer.valueOf(getGroupList.getLength())) {
                    String[] array = new String[1];
                    result = "Fail";
                    sendMessage(code, getGroupList.getDeviceId(), result, array);
                    return;
                }

                List<Group> list = FaceApi.getInstance().
                        getGroupList(Integer.valueOf(getGroupList.getStart()),
                                Integer.valueOf(getGroupList.getLength()));
                String[] array = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    array[i] = list.get(i).getGroupId();
                }

                if (array.length != 0) {
                    result = "SUCCESS";
                } else {
                    result = "Fail";
                }
                sendMessage(code, getGroupList.getDeviceId(), result, array);
            }
            if (code == RequestConfig.getRecords) {
                Type type = new TypeToken<SocketResult<GetRecords>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                GetRecords getRecords = (GetRecords) socketResult.getData();

                List<ResponseGetRecords> responseGetRecords = FaceApi.getInstance().getRecords(getRecords.getStartTime()
                        , getRecords.getEndTime());
                if (responseGetRecords == null) {
                    result = "Fail";
                    sendMessage(code, getRecords.getDeviceId(), result, null);
                } else {
                    result = "SUCCESS";
                    sendMessage(code, getRecords.getDeviceId(), result, responseGetRecords);
                }

            }
            if (code == RequestConfig.deleteRecords) {
                Type type = new TypeToken<SocketResult<DeleteRecords>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                DeleteRecords deleteRecords = (DeleteRecords) socketResult.getData();

                if (TextUtils.isEmpty(deleteRecords.getUserId())) {
                    isSuccess = FaceApi.getInstance().deleteRecords(deleteRecords.getStartTime(),
                            deleteRecords.getEndTime());
                } else {
                    isSuccess = FaceApi.getInstance().deleteRecords(deleteRecords.getUserId());
                }
                if (isSuccess) {
                    result = "SUCCESS";
                } else {
                    result = "FAIL";
                }
                sendMessage(code, deleteRecords.getDeviceId(), result, null);
            }
            if (code == RequestConfig.cleanLog) {
                Type type = new TypeToken<SocketResult<CleanLog>>() {
                }.getType();
                SocketResult socketResult = getSocketRespones(messsage, type);
                final CleanLog cleanLog = (CleanLog) socketResult.getData();
                RecordLogManager.resetTimer(cleanLog.getHour(), new GetDeleteNum() {
                    @Override
                    public void getNum(int num) {
                        result = String.valueOf(num);
                        sendMessage(code, cleanLog.getDeviceId(), result, null);
                    }
                });
            }

        } catch (Exception e) {
            result = "Fail";
            String errorMsg = e.getMessage();
            sendMessage(RequestConfig.UnKonwError, deviceid, result, errorMsg);
        }
    }

    public SocketResult getSocketRespones(String json, Type type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public void sendMessage(int type, String deviceId, String message, Object object) {
        if (type == RequestConfig.sendDeviceId) {
            SocketResult<ResponseDevice> socketResult = new SocketResult<ResponseDevice>();
            ResponseDevice responseDevice = new ResponseDevice();
            responseDevice.setDeviceId(deviceId);
            successMessage(type, socketResult, responseDevice, message);
        }
        if (type == RequestConfig.resetConfigFile) {
            SocketResult<ResponseResetConfigFile> socketResult = new SocketResult<ResponseResetConfigFile>();
            ResponseResetConfigFile responseResetConfigFile = new ResponseResetConfigFile();
            responseResetConfigFile.setDeviceId("resetConfigFile");
            responseResetConfigFile.setOK(true);
            successMessage(type, socketResult, responseResetConfigFile, message);
        }

        if (type == RequestConfig.userFaceAdd) {
            SocketResult<ResponseAddAndUpdate> socketResult = new SocketResult<ResponseAddAndUpdate>();
            ResponseAddAndUpdate responseAddAndUpdate = new ResponseAddAndUpdate();
            responseAddAndUpdate.setDeviceId(deviceId);
            responseAddAndUpdate.setFaceToken(UUID.randomUUID().toString());
            successMessage(type, socketResult, responseAddAndUpdate, message);
        }

        if (type == RequestConfig.userUpdate) {
            ResponseAddAndUpdate responseAddAndUpdate = new ResponseAddAndUpdate();
            responseAddAndUpdate.setDeviceId(deviceId);
            responseAddAndUpdate.setFaceToken(UUID.randomUUID().toString());
            SocketResult<ResponseAddAndUpdate> socketResult = new SocketResult<ResponseAddAndUpdate>();
            successMessage(type, socketResult, responseAddAndUpdate, message);
        }

        if (type == RequestConfig.userDelete) {
            ResponseUserDelete responseUserDelete = new ResponseUserDelete();
            responseUserDelete.setDeviceId(deviceId);
            SocketResult<ResponseUserDelete> socketResult = new SocketResult<ResponseUserDelete>();
            successMessage(type, socketResult, responseUserDelete, message);
        }

        if (type == RequestConfig.userCopy) {
            ResponseUserCopy responseUserCopy = new ResponseUserCopy();
            responseUserCopy.setDeviceId(deviceId);
            SocketResult<ResponseUserCopy> socketResult = new SocketResult<ResponseUserCopy>();
            successMessage(type, socketResult, responseUserCopy, message);
        }
        if (type == RequestConfig.groupAdd) {
            ResponseGroupAdd responseGroupAdd = new ResponseGroupAdd();
            responseGroupAdd.setDeviceId(deviceId);
            SocketResult<ResponseGroupAdd> socketResult = new SocketResult<ResponseGroupAdd>();
            successMessage(type, socketResult, responseGroupAdd, message);
        }
        if (type == RequestConfig.groupDelete) {
            ResponseGroupDelete responseGroupDelete = new ResponseGroupDelete();
            responseGroupDelete.setDeviceId(deviceId);
            SocketResult<ResponseGroupDelete> socketResult = new SocketResult<ResponseGroupDelete>();
            successMessage(type, socketResult, responseGroupDelete, message);
        }
        if (type == RequestConfig.getUserInfo) {
            ResponseGetUserInfo responseGetUserInfo;
            if (object != null) {
                User user = (User) object;
                responseGetUserInfo = new ResponseGetUserInfo();
                responseGetUserInfo.setDeviceId(deviceId);
                responseGetUserInfo.setCreateTime(user.getCtime() + "");
                responseGetUserInfo.setFace("");
                responseGetUserInfo.setFaceToken(user.getFaceToken());
                String[] array = {};
                responseGetUserInfo.setResult(array);
                responseGetUserInfo.setGroupId(user.getGroupId());
                responseGetUserInfo.setUserInfo(user.getUserInfo());
            } else {
                responseGetUserInfo = new ResponseGetUserInfo();
            }

            SocketResult<ResponseGroupDelete> socketResult = new SocketResult<ResponseGroupDelete>();
            successMessage(type, socketResult, responseGetUserInfo, message);
        }
        if (type == RequestConfig.getUserList) {
            ResponseGetUserList responseGetUserList = new ResponseGetUserList();
            responseGetUserList.setDeviceId(deviceId);
            String[] array = (String[]) object;
            responseGetUserList.setUserIdList(array);
            SocketResult<ResponseGetUserList> socketResult = new SocketResult<ResponseGetUserList>();
            successMessage(type, socketResult, responseGetUserList, message);
        }
        if (type == RequestConfig.getGroupList) {
            ResponseGetGroupList responseGetGroupList = new ResponseGetGroupList();
            responseGetGroupList.setDeviceId(deviceId);
            String[] array = (String[]) object;
            responseGetGroupList.setGroupIdList(array);
            SocketResult<ResponseGetGroupList> socketResult = new SocketResult<ResponseGetGroupList>();
            successMessage(type, socketResult, responseGetGroupList, message);
        }

        if (type == RequestConfig.cleanLog) {
            ResponseCleanLog responseCleanLog = new ResponseCleanLog();
            responseCleanLog.setDeviceId(deviceId);
            responseCleanLog.setLogNum(message);
            responseCleanLog.setLogTimeArea(String.valueOf(System.currentTimeMillis()
                    - RecordLogManager.deleteTime));
            SocketResult<ResponseCleanLog> socketResult = new SocketResult<ResponseCleanLog>();
            successMessage(type, socketResult, responseCleanLog, message);
        }

        if (type == RequestConfig.getRecords) {
            List<ResponseGetRecords> responseGetRecordsList = (List<ResponseGetRecords>) object;
            RecordList recordList = new RecordList();
            recordList.setDeviceId(deviceId);
            recordList.setList(responseGetRecordsList);
            SocketResult<RecordList> socketResult = new SocketResult<RecordList>();
            successMessage(type, socketResult, recordList, message);
        }

        if (type == RequestConfig.deleteRecords) {
            ResponseDeleteRecords responseDeleteRecords = new ResponseDeleteRecords();
            responseDeleteRecords.setDeviceId(deviceId);
            responseDeleteRecords.setNum("");
            responseDeleteRecords.setResult("");
            SocketResult<ResponseDeleteRecords> socketResult = new SocketResult<ResponseDeleteRecords>();
            successMessage(type, socketResult, responseDeleteRecords, message);
        }

        if (type == RequestConfig.UnKonwError) {
            ResponeseUnKonwError responeseUnKonwError = new ResponeseUnKonwError();
            responeseUnKonwError.setDeviceId(deviceid);
            responeseUnKonwError.setErrorMsg((String) object);
            SocketResult<ResponeseUnKonwError> socketResult = new SocketResult<ResponeseUnKonwError>();
            successMessage(type, socketResult, responeseUnKonwError, message);
        }

    }


    public void successMessage(int type, SocketResult socketResult, Object object, String message) {
        Gson resultGson = new Gson();
        socketResult.setCode(type);
        socketResult.setData(object);
        socketResult.setMessage(message);
        socketResult.setQueryId(System.currentTimeMillis() + "");
        String resultMessage = resultGson.toJson(socketResult);

        int length = 100;
        if (resultMessage.length() < length) {
            socketClient.send(resultMessage + "&_&");
            Log.e("shang", "发送：" + resultMessage);
        } else {
            int size = resultMessage.length();
            for (int i = 0; i < size; i++) {
                String childStr = substring(resultMessage, i * length,
                        (i + 1) * length);
                if (!TextUtils.isEmpty(childStr)) {
                    socketClient.send(childStr);
                    Log.e("shang", "发送：" + childStr);
                }
            }
        }

    }


    public static String substring(String str, int x, int y) {
        if (x > str.length()) {
            return null;
        }
        if (y > str.length()) {
            return str.substring(x, str.length()) + "&_&";
        } else {
            return str.substring(x, y);
        }
    }


    public interface GetDeleteNum {
        // 获取删除的识别记录数量
        void getNum(int num);
    }
}
