package com.yc.patrol.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.idl.face.main.utils.FileUtils;
import com.yc.patrol.MyConstants;
import com.yc.patrol.PatrolBean;
import com.yc.patrol.UserPatrol;

import org.jsoup.Jsoup;
import org.openni.DeviceInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

    public static String getSavePath(Context mContext, String file) {
        String p;
        if (haveSD()) {
            p = getSDPath() + File.separator + MyConstants.packageName + "/" + file + "/";
        } else {
            p = mContext.getFilesDir() + File.separator + MyConstants.packageName + "/" + file + "/";
        }
        File f = new File(p);
        if (!f.exists()) {
            f.mkdirs();
        }
        return p;
    }


    public static void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            deleteFile(f);
        }
    }

    /**
     * 问题在小米3。华为系列手机出现概率较大
     * open failed: EBUSY (Device or resource busy)
     * 删除文件安全方式
     *
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
     *
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

    /**
     * Jsoup - 读取XML数据（本质是document）
     * 直观明了，但是要预加载所有数据，对xml比较大占内存比较多
     */
    private static void ReadXmlFromSdcardByJsoup(String xmlPath) {
        File xmlFile = new File(xmlPath);
        if (xmlFile.exists()) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(" -- Jsoup --\n");

                org.jsoup.nodes.Document document = Jsoup.parse(xmlFile, "UTF-8");
                org.jsoup.select.Elements userE = document.getElementsByTag("user");
                for (int i = 0; i < userE.size(); i++) {
                    sb.append("user.name=" + userE.get(i).attr("name") + "\n");
                }


            } catch (IOException e) {
                e.printStackTrace();
                Log.e("xml", "xml数据导入异常：" + e.getMessage());
            }
        } else {
            Log.e("xml", "xml文件不存在");
        }
    }

    public  static void  createDOMXml(List<PatrolBean> beans, UserPatrol userPatrol) throws Exception{
        //创建一个DocumentBuilderFactory的对象
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //创建一个DocumentBuider的对象
        DocumentBuilder db = dbf.newDocumentBuilder();

        //创建新的文档
        org.w3c.dom.Document document = db.newDocument();
        document.setXmlStandalone(true); //设置xml里面不显示standlone
        //创建文档下的一个根节点
        org.w3c.dom.Element people = document.createElement("People");
        people.setAttribute("id",userPatrol.getId());
        people.setAttribute("PatrolTime",DateUtils.getCurrentDate());
        people.setAttribute("LineId",userPatrol.getLineId());
        people.setAttribute("TodayIsAbnormal",userPatrol.getTodayIsAbnormal());

        //向bookstore添加子节点
        for(int i = 0 ; i < beans.size() ; i++) {
            PatrolBean pb = beans.get(i);
            org.w3c.dom.Element point = document.createElement("PatrolPoint");
            point.setAttribute("id", userPatrol.getId());
            point.setAttribute("ArriveTime", pb.getTime());
            point.setAttribute("IsAbnormal", pb.getIsAbnormal());
            point.setAttribute("QRcode", pb.getPlace());
            point.setAttribute("PatrolImage", pb.getPatrolImage());

            for(int j = 0 ; j < beans.size() ; j++) {
                //添加子节点
                org.w3c.dom.Element pro = document.createElement("PatrolProject");
                pro.setAttribute("objId", j+"");
                pro.setAttribute("Result", "000000000");
//            pro.setTextContent("西游记");
//            pro.appendChild(name);
                point.appendChild(pro);
            }
            //将book添加book
            people.appendChild(point);
        }
        //添加根节点（已经包含了book）
        document.appendChild(people);

        //将dom树保存成xml文件

        //创建TransformerFactory 对象
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer tf = factory.newTransformer();
        //设置换行
        tf.setOutputProperty(OutputKeys.INDENT,"yes");
        //将document转换成xml文件
        String paths = FileUtils2.getCacheFilePath(MyConstants.DATAPATH + File.separator + userPatrol.getName()+".xml");
        tf.transform(new DOMSource(document), new StreamResult(new File(paths)) );

    }
}
