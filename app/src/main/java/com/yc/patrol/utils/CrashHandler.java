package com.yc.patrol.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import com.yc.patrol.MyConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 *
 * @author user
 */
public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";
    // 系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    // CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();
    // 程序的Context对象
    private Context mContext;
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private String crashPath;

    final UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public boolean haveSD() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public String getSDPath() {
        File sdDir = null;
        String path = "/sdcard";
        if (haveSD()) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }
        File file = new File(path);
        if (file.exists()) {
            return path;
        } else {
            return "";
        }

    }

    public String getSavePath() {
        if (this.haveSD()) {
            return getSDPath() + File.separator + MyConstants.packageName + "/error/";
        } else {
            return mContext.getFilesDir() + File.separator + MyConstants.packageName + "/error/";
        }
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);

        crashPath = getSavePath();
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (thread.getName().equals("FinalizerWatchdogDaemon") && ex instanceof TimeoutException) {
            //ignore it
        } else {
            if (!handleException(ex) && mDefaultHandler != null) {
                // 如果用户没有处理则让系统默认的异常处理器来处理
                mDefaultHandler.uncaughtException(thread, ex);
            } else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "CrashHandler error : ", e);
                }
                //退出程序
                ScreenManager.getScreenManager().currentActivity().finish();
                killProcess(mContext);
            }
        }

    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                ex.printStackTrace();
                try {
                    Looper.prepare();

                    if (mContext != null) {
                        Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出。", Toast.LENGTH_SHORT).show();
                    }
                    Looper.loop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }


    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
                infos.put("MemTotal", Tools.getMemInfoIype(ctx, "MemTotal"));
                infos.put("MemFree", Tools.getMemInfoIype(ctx, "MemFree"));
            }
        } catch (NameNotFoundException e) {
//			Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
//				Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
//				Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //String path = "/sdcard/crash/";
                File dir = new File(crashPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(crashPath + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
//			Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;

    }

    private static void killProcess(Context mAct) {
//        String packageName = mAct.getPackageName();
//        String processId = "";
//        try {
//            Runtime r = Runtime.getRuntime();
//            Process p = r.exec("ps");
//            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            String inline = br.readLine();
//            while (inline != null) {
//                if (inline.contains(packageName)) {
//                    StringTokenizer processInfoTokenizer = new StringTokenizer(inline);
//                    int count = 0;
//                    while (processInfoTokenizer.hasMoreTokens()) {
//                        count++;
//                        processId = processInfoTokenizer.nextToken();
//                        if (count == 2) {
//                            break;
//                        }
//                    }
//                    Process p1 = r.exec("kill -15 " + processId);
//                }
//                inline = br.readLine();
//            }
//
//            br.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
        System.gc();
    }
}
