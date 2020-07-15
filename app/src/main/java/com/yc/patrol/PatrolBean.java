package com.yc.patrol;

import android.net.Uri;

import com.yc.patrol.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class PatrolBean {
    private String id;
    private String patrolTime;
    private String lineId;
    private String todayIsAbnormal;
    //
    private String pointId;
    private String arriveTime;
    private String isAbnormal;
    private String qRcode;
    private String linePlaceName;
    private String patrolImage;
    private List<ProjectResult> projectResults;
    //
    private String photoUrl;
    private Uri uri;
    private Uri uriSy;
    private String photoUrlSy;

    public PatrolBean(){

    }

    public PatrolBean(String linePlaceName) {
        this.patrolTime = DateUtils.getCurrentDate();
        this.arriveTime = DateUtils.gethmsTime();
        this.linePlaceName = linePlaceName;
    }

    public PatrolBean(People people,People.PatrolPoint patrolPoint,List<People.PatrolProject> projectList) {
        this.patrolTime = DateUtils.getCurrentDate();
        this.arriveTime = DateUtils.gethmsTime();
        this.id = people.getId();
        this.lineId = people.getLine().getlId();
        this.pointId = patrolPoint.getPid();
        this.qRcode = patrolPoint.getqRcode();
        this.linePlaceName = patrolPoint.getLinePlaceName();

        this.projectResults = new ArrayList<>();
        if(null != projectList){
            for(People.PatrolProject p : projectList){
                ProjectResult project2 = new ProjectResult();
                project2.objId = p.getObjId();
                project2.objDesc = p.getObjDesc();
                project2.objName = p.getObjName();
                this.projectResults.add(project2);
            }
        }
    }


    public static class ProjectResult {
        private String objId;
        private String result;
        private String isAbnormal;
        private String objDesc;
        private String objName;

        public String getObjDesc() {
            return objDesc;
        }

        public void setObjDesc(String objDesc) {
            this.objDesc = objDesc;
        }

        public String getObjName() {
            return objName;
        }

        public void setObjName(String objName) {
            this.objName = objName;
        }

        public String getObjId() {
            return objId;
        }

        public void setObjId(String objId) {
            this.objId = objId;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getIsAbnormal() {
            return isAbnormal;
        }

        public void setIsAbnormal(String isAbnormal) {
            this.isAbnormal = isAbnormal;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatrolTime() {
        return patrolTime;
    }

    public void setPatrolTime(String patrolTime) {
        this.patrolTime = patrolTime;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getTodayIsAbnormal() {
        return todayIsAbnormal;
    }

    public void setTodayIsAbnormal(String todayIsAbnormal) {
        this.todayIsAbnormal = todayIsAbnormal;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getIsAbnormal() {
        return isAbnormal;
    }

    public void setIsAbnormal(String isAbnormal) {
        this.isAbnormal = isAbnormal;
    }

    public String getqRcode() {
        return qRcode;
    }

    public void setqRcode(String qRcode) {
        this.qRcode = qRcode;
    }

    public String getPatrolImage() {
        return patrolImage;
    }

    public void setPatrolImage(String patrolImage) {
        this.patrolImage = patrolImage;
    }

    public List<ProjectResult> getProjectResults() {
        return projectResults;
    }

    public void setProjectResults(List<ProjectResult> projectResults) {
        this.projectResults = projectResults;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUriSy() {
        return uriSy;
    }

    public void setUriSy(Uri uriSy) {
        this.uriSy = uriSy;
    }

    public String getPhotoUrlSy() {
        return photoUrlSy;
    }

    public void setPhotoUrlSy(String photoUrlSy) {
        this.photoUrlSy = photoUrlSy;
    }

    public String getLinePlaceName() {
        return linePlaceName;
    }

    public void setLinePlaceName(String linePlaceName) {
        this.linePlaceName = linePlaceName;
    }
}
