package com.baidu.idl.face.main.manager;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.socket.SocketService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * author : baidu
 * date : 2019/8/20 3:25 PM
 * description :
 */
public class RecordLogManager {
    private static Timer timer;
    public static Long deleteTime = System.currentTimeMillis();
    public static int deleteNum = 0;

    private RecordLogManager() {
    }

    public static Timer getTimer() {
        if (timer == null) {
            timer = new Timer();
            return timer;
        }
        return timer;
    }

    // 重置日志删除定时器
    public static int resetTimer(String time, final SocketService.GetDeleteNum getDeleteNum) {
        try {
            if (time.equals("none")) {
                timer.cancel();
                timer = null;
            } else {
                getTimer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        deleteNum = FaceApi.getInstance().cleanRecords();
                        deleteTime = System.currentTimeMillis();
                        getDeleteNum.getNum(deleteNum);
                    }
                }, Long.valueOf(time) * 1000, Long.valueOf(time) * 1000);
            }
            return deleteNum;
        } catch (Exception e) {
            return deleteNum;
        }

    }

}
