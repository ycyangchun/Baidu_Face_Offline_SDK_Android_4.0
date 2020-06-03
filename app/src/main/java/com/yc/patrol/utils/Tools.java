package com.yc.patrol.utils;

import android.content.Context;
import android.os.Environment;

import com.yc.patrol.MyConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 存放共用的方法， 每个方法都需要增加方法说明
 *
 * @author nomen
 */
public class Tools {

    public static String getMemInfoIype(Context context, String type) {
        try {
            FileReader fileReader = new FileReader("/proc/meminfo");
            BufferedReader bufferedReader = new BufferedReader(fileReader, 4 * 1024);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                if (str.contains(type)) {
                    break;
                }
            }
            bufferedReader.close();
            /* \\s表示   空格,回车,换行等空白符,
            +号表示一个或多个的意思     */
            String[] array = str.split("\\s+");
            // 获得系统总内存，单位是KB，乘以1024转换为Byte
            long length = Long.valueOf(array[1]).longValue() * 1024;
            return android.text.format.Formatter.formatFileSize(context, length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean haveSD() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getSDPath() {
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

    public static String getSavePath(Context mContext,String file) {
        String p;
        if (haveSD()) {
            p = getSDPath() + File.separator + MyConstants.packageName + "/" +file+ "/";
        } else {
            p =  mContext.getFilesDir() + File.separator + MyConstants.packageName + "/"+file+ "/";
        }
        File f = new File(p);
        if(!f.exists()){
            f.mkdirs();
        }
        return  p;
    }


    public static void  deleteFile(String filename) {
        File f = new File(filename);
        if(f.exists()){
            deleteFile(f);
        }
    }
    /**
     * 问题在小米3。华为系列手机出现概率较大
     * open failed: EBUSY (Device or resource busy)
     * 删除文件安全方式
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            deleteFileSafely(file);
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                deleteFileSafely(file);
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                deleteFile(childFiles[i]);
            }
            deleteFileSafely(file);
        }
    }


    /**
     * 安全删除文件
     * @param file
     * @return
     */
    public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }
}
