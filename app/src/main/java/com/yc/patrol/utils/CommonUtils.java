package com.yc.patrol.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * 常用的方法工具类
 * 1、获取常用的全局context
 * 2、获取app的VersionName和VersionCode
 * 3、获取Resource
 */
public class CommonUtils {

    private static Application mApplication;

    private static SimpleActivityLifecycle lifecycle = new SimpleActivityLifecycle();
    public static void init(Application application) {
        mApplication = application;
        mApplication.registerActivityLifecycleCallbacks(lifecycle);
    }

    public static void clear() {
        mApplication = null;
    }

    public static Application getApplication() {
        if (mApplication == null) {
            throw new NullPointerException("mApplication is null,Please initialize the mApplication first");
        }
        return mApplication;
    }

    public static Context getContext() {
        if (mApplication == null) {
            throw new NullPointerException("mApplication is null,Please initialize the mApplication first");
        }
        return mApplication.getApplicationContext();
    }




    /**
     * 获取版本名称
     */
    public static String getAppVersionName() {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getContext().getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }

    /**
     * 获取版本号
     */
    public static int getAppVersionCode() {
        int versioncode = -1;
        try {
            // ---get the package info---
            PackageManager pm = getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getContext().getPackageName(), 0);
            versioncode = pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versioncode;
    }

    /**
     * 获取屏幕宽度
     * @return
     */
    public static int getScreenWidth(){
        return getResources().getDisplayMetrics().widthPixels;
    }
    /**
     * 获取屏幕高度
     * @return
     */
    public static int getScreenHeight(){
        return getResources().getDisplayMetrics().heightPixels;
    }



    public static Resources getResources() {
        return getContext().getResources();
    }

    public static String getString(int stringID){
        return getResources().getString(stringID);
    }

    public static float getDimension(int dimenID){
        return getResources().getDimension(dimenID);
    }

    public static float getDimensionPixelSize(int dimenID){
        return getResources().getDimensionPixelSize(dimenID);
    }


    public static class SimpleActivityLifecycle implements Application.ActivityLifecycleCallbacks {

        private boolean isForeground = false;//应用是否处于前端

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            isForeground = true;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            isForeground = false;
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        public boolean isForeground(Context context) {
            if (Build.VERSION.SDK_INT >= 14) {
                return isForeground;
            } else {
                String packageName = context.getApplicationContext().getPackageName();
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (!tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    if (topActivity.getPackageName().equals(packageName)) {
                        return true;
                    }
                }
                return false;
            }

        }
    }

    public static boolean isForeground() {
        if (lifecycle == null) {
            return false;
        }
        return lifecycle.isForeground(mApplication.getApplicationContext());
    }

    public static void onDestroy(){
        lifecycle = null;
        mApplication = null;
    }

}
