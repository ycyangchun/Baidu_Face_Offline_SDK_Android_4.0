package com.yc.patrol.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.face.main.utils.ToastUtils;
import com.yc.patrol.App;
import com.yc.patrol.MyConstants;
import com.yc.patrol.PatrolBean;
import com.yc.patrol.People;

import org.jsoup.Jsoup;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
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

    public static void ReadXml(String xmlPath) {
        try {
            File xmlFile = new File(xmlPath);
            if (xmlFile.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(xmlFile);
                Element element = document.getDocumentElement();

                List<People> peopleList = new ArrayList<People>();
                NodeList peopleNodes = element.getElementsByTagName("People");
                for (int i = 0; i < peopleNodes.getLength(); i++) {
                    People p = new People();
                    Element pE = (Element) peopleNodes.item(i);
                    p.setId(pE.getAttribute("Id"));
                    p.setName(pE.getAttribute("Name"));
                    p.setFullName(pE.getAttribute("FullName"));
                    p.setBeginTime(pE.getAttribute("Begintime"));
                    NodeList childPeopleNodes = pE.getChildNodes();

                    List<People.PatrolProject> projectList = new ArrayList<>();
                    List<People.PatrolPoint> pointList = new ArrayList<>();
                    People.Line line = new People.Line();
                    for (int j = 0; j < childPeopleNodes.getLength(); j++) {
                        //DOM解析时候注意子节点前面的空格也会被解析
                        if (childPeopleNodes.item(j) instanceof Element) {
                            Element childPeopleElement = (Element) childPeopleNodes.item(j);
                            if (childPeopleElement.getNodeType() == Node.ELEMENT_NODE) {
                                if (childPeopleElement.getNodeName().equals("PatrolProject")) {
                                    People.PatrolProject obj = new People.PatrolProject();
                                    obj.setObjId(childPeopleElement.getAttribute("ObjId"));
                                    obj.setObjName(childPeopleElement.getAttribute("ObjName"));
                                    obj.setObjDesc(childPeopleElement.getAttribute("ObjDesc"));
                                    projectList.add(obj);
                                } else if (childPeopleElement.getNodeName().equals("PatrolPoint")) {
                                    People.PatrolPoint point = new People.PatrolPoint();
                                    point.setPid(childPeopleElement.getAttribute("Id"));
                                    point.setLinePlaceName(childPeopleElement.getAttribute("LinePlaceName"));
                                    point.setArriveTime(childPeopleElement.getAttribute("ArriveTime"));
                                    point.setPositionDescribe(childPeopleElement.getAttribute("PositionDescribe"));
                                    point.setqRcode(childPeopleElement.getAttribute("QRcode"));
                                    point.setNum(childPeopleElement.getAttribute("Num"));

                                    pointList.add(point);

                                } else if (childPeopleElement.getNodeName().equals("Line")) {
                                    line.setlId(childPeopleElement.getAttribute("Id"));
                                    line.setNormal_Offset(childPeopleElement.getAttribute("Normal_Offset"));
                                    line.setAbnormal_Offset(childPeopleElement.getAttribute("Abnormal_Offset"));
                                    line.setRemark(childPeopleElement.getAttribute("Remark"));

                                }

                            }
                        }
                    }
                    p.setPatrolProjects(projectList);
                    p.setPatrolPoints(pointList);
                    p.setLine(line);
                    peopleList.add(p);
//                    System.out.println(p);
                }
                App.setPatrolPlan(peopleList);
            }

        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }


    public static void createDOMXml(List<PatrolBean> userPatrol, Context context) {
        String fn = MyConstants.DATAPATH.replace(File.separator, "");
        String paths = FileUtils2.getCacheFilePath(MyConstants.DATAPATH + File.separator + fn + ".xml");
        createDOMXml(userPatrol, context, paths, true);
    }

    public static void createDOMXml(List<PatrolBean> userPatrol, Context context, String paths, boolean toast) {
        try {
            //创建一个DocumentBuilderFactory的对象
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //创建一个DocumentBuider的对象
            DocumentBuilder db = dbf.newDocumentBuilder();

            //创建新的文档
            Document document = db.newDocument();
            document.setXmlStandalone(true); //设置xml里面不显示standlone
            //创建文档下的一个根节点
            Element people = document.createElement("People");
            if(null != userPatrol && userPatrol.size() > 0) {
                PatrolBean p = userPatrol.get(0);
                people.setAttribute("id", getCtx(p.getId()));
                people.setAttribute("PatrolTime", getCtx(p.getPatrolTime()));
                people.setAttribute("LineId", getCtx(p.getLineId()));
                people.setAttribute("TodayIsAbnormal", getCtx(p.getTodayIsAbnormal()));
                if (!toast) {//区分临时，和导出xml
                    people.setAttribute("name", getCtx(p.getName()));
                    people.setAttribute("fullName", getCtx(p.getFullName()));

                }

                //向bookstore添加子节点
                for (int i = 0; i < userPatrol.size(); i++) {
                    PatrolBean pb = userPatrol.get(i);
                    Element point = document.createElement("PatrolPoint");
                    point.setAttribute("id", getCtx(pb.getPointId()));
                    point.setAttribute("ArriveTime", getCtx(pb.getArriveTime()));
                    point.setAttribute("IsAbnormal", getCtx(pb.getIsAbnormal()));
                    point.setAttribute("QRcode", getCtx(pb.getqRcode()));
                    point.setAttribute("PatrolImage", getCtx(pb.getPatrolImage()));
                    if (!toast) {//区分临时，和导出xml
                        point.setAttribute("isShow", getCtx(pb.getIsShow()));
                        point.setAttribute("photoUrl", getCtx(pb.getPhotoUrl()));
                        point.setAttribute("photoUrlSy", getCtx(pb.getPhotoUrlSy()));
                        point.setAttribute("LinePlaceName", getCtx(pb.getLinePlaceName()));

                    }
                    List<PatrolBean.ProjectResult> pObjs = pb.getProjectResults();
                    if (null != pObjs) {
                        for (int j = 0; j < pObjs.size(); j++) {
                            //添加子节点
                            PatrolBean.ProjectResult obj = pObjs.get(j);
                            Element pro = document.createElement("PatrolProject");
                            pro.setAttribute("objId", getCtx(obj.getObjId()));
                            pro.setAttribute("Result", getCtx(obj.getResult()));
                            if (!toast) {//区分临时，和导出xml
                                pro.setAttribute("objDesc", getCtx(obj.getObjDesc()));
                                pro.setAttribute("objName", getCtx(obj.getObjName()));
                            }
                            point.appendChild(pro);
                        }
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
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                //将document转换成xml文件

                DOMSource domSource = new DOMSource(document);
                tf.transform(domSource, new StreamResult(new File(paths)));
                if (toast) {
                    ToastUtils.toastL(context, "记录保持到目录\n" + paths);
                }
            }

            //输出第二份
//            TransformerFactory factory2 = TransformerFactory.newInstance();
//            Transformer tf2 = factory2.newTransformer();
//            //设置换行
//            tf2.setOutputProperty(OutputKeys.INDENT, "yes");
//            String path2 = Environment.getExternalStorageDirectory().toString() + File.separator + fn + ".xml";
//            tf2.transform(domSource, new StreamResult(new File(path2)));
//            ToastUtils.toastL(context, "导出到目录\n" + path2);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }


    public static void createPatrolBeanXml(Context context, People people, List<PatrolBean> oldBean) {
        String fn = MyConstants.DATAPATH.replace(File.separator, "");
        MyConstants.tempXml = FileUtils2.getCacheFilePath(MyConstants.DATAPATH
                + File.separator+ "tempPic" + File.separator + fn + "_temp.xml");
        File xmlFile = new File(MyConstants.tempXml);
        if (xmlFile.exists()){

            return;// 已有不覆盖数据
        }

        List<People.PatrolProject> projectList;
        List<People.PatrolPoint> pointList;
        People.Line line;
        List<PatrolBean> patrolBeanList = new ArrayList<>();
        if (null != people) {
            projectList = people.getPatrolProjects();
            pointList = people.getPatrolPoints();
            line = people.getLine();

            for (int i = pointList.size() -1 ; i >= 0 ; i--) {
                PatrolBean patrolBean = new PatrolBean();
                People.PatrolPoint point = pointList.get(i);

                patrolBean.setId(people.getId());
                patrolBean.setPatrolTime(DateUtils.getCurrentDate());
                patrolBean.setLineId(line.getlId());
                patrolBean.setTodayIsAbnormal("0");
                patrolBean.setIsShow("0");
                patrolBean.setPointId(point.getPid());
                patrolBean.setIsAbnormal("0");
                patrolBean.setqRcode(point.getqRcode());
                patrolBean.setLinePlaceName(point.getLinePlaceName());
                patrolBean.setName(people.getName());
                patrolBean.setFullName(people.getFullName());

                if (null != oldBean) {
                    patrolBean.setPhotoUrl(oldBean.get(i).getPhotoUrl());
                    patrolBean.setPhotoUrlSy(oldBean.get(i).getPhotoUrlSy());
                    patrolBean.setPatrolImage(oldBean.get(i).getPatrolImage());
                }
                List<PatrolBean.ProjectResult> projectResults = new ArrayList<>();
                for (int j = 0; j < projectList.size(); j++) {
                    People.PatrolProject project = projectList.get(j);

                    PatrolBean.ProjectResult result = new PatrolBean.ProjectResult();
                    result.setObjId(project.getObjId());
                    result.setResult("0");
                    result.setObjDesc(project.getObjDesc());
                    result.setObjName(project.getObjName());
                    projectResults.add(result);
                }
                patrolBean.setProjectResults(projectResults);
                patrolBeanList.add(patrolBean);
            }
            ////
            PatrolBean patrolBean0 = new PatrolBean();
            patrolBean0.setPatrolTime("");
            patrolBean0.setTodayIsAbnormal("0");
            patrolBean0.setIsShow("1");
            patrolBean0.setLinePlaceName("签到");
            if (null != oldBean && oldBean.size() -1 == 0) {
                PatrolBean patrolBeanOld = oldBean.get(oldBean.size() -1 );
                patrolBean0.setPatrolImage(patrolBeanOld.getPatrolImage());
                patrolBean0.setPatrolTime(patrolBeanOld.getPatrolTime());
            }
            patrolBeanList.add(patrolBean0);



            createDOMXml(patrolBeanList, context, MyConstants.tempXml, false);
        }

    }

    public static List<PatrolBean> ReadPatrolBeanXml() {
        List<PatrolBean> peopleList = new ArrayList<>();
        try {
            File xmlFile = new File(MyConstants.tempXml);
            if (xmlFile.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(xmlFile);
                Element element = document.getDocumentElement();

                String id = element.getAttribute("id");
                String name = element.getAttribute("name");
                String today = element.getAttribute("TodayIsAbnormal");
                String lid = element.getAttribute("LineId");
                String pt = element.getAttribute("PatrolTime");
                String fn = element.getAttribute("fullName");

                NodeList nodes = element.getElementsByTagName("PatrolPoint");
                PatrolBean p = null;
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element nE = (Element) nodes.item(i);
                    p = new PatrolBean();
                    p.setId(id);
                    p.setName(name);
                    p.setTodayIsAbnormal(today);
                    p.setLineId(lid);
                    p.setPatrolTime(pt);
                    p.setFullName(fn);

                    p.setIsShow(nE.getAttribute("isShow"));
                    p.setLinePlaceName(nE.getAttribute("LinePlaceName"));
                    p.setPatrolImage(nE.getAttribute("PatrolImage"));
                    p.setqRcode(nE.getAttribute("QRcode"));
                    p.setArriveTime(nE.getAttribute("ArriveTime"));
                    p.setIsAbnormal(nE.getAttribute("IsAbnormal"));
                    p.setPhotoUrl(nE.getAttribute("photoUrl"));
                    p.setPhotoUrlSy(nE.getAttribute("photoUrlSy"));
                    p.setPointId(nE.getAttribute("id"));

                    NodeList childPeopleNodes = nE.getChildNodes();

                    List<PatrolBean.ProjectResult> projectResults = new ArrayList<>();
                    for (int j = 0; j < childPeopleNodes.getLength(); j++) {
                        //DOM解析时候注意子节点前面的空格也会被解析
                        if (childPeopleNodes.item(j) instanceof Element) {
                            Element childPeopleElement = (Element) childPeopleNodes.item(j);
                            if (childPeopleElement.getNodeType() == Node.ELEMENT_NODE) {
                                if (childPeopleElement.getNodeName().equals("PatrolProject")) {
                                    PatrolBean.ProjectResult obj = new PatrolBean.ProjectResult();
                                    obj.setObjId(childPeopleElement.getAttribute("objId"));
                                    obj.setObjName(childPeopleElement.getAttribute("objName"));
                                    obj.setObjDesc(childPeopleElement.getAttribute("objDesc"));
                                    obj.setResult(childPeopleElement.getAttribute("Result"));
                                    projectResults.add(obj);
                                   
                                }
                                
                            }
                        }
                    }
                    p.setProjectResults(projectResults);
                    peopleList.add(p);
                }

            }

        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }finally {
            return  peopleList;
        }
    }

    public static String getCtx(String str) {
        String s = "";
        if (TextUtils.isEmpty(str)) {

        } else {
            s = str;
        }
        return s;
    }
}
