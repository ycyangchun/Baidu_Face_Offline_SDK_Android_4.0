package com.yc.patrol;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.yc.patrol.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class PatrolBean implements Parcelable {
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
        this.todayIsAbnormal = "1";// 默认正常
        this.isAbnormal = "1";

        this.projectResults = new ArrayList<>();
        if(null != projectList){
            for(People.PatrolProject p : projectList){
                ProjectResult result = new ProjectResult();
                result.objId = p.getObjId();
                result.objDesc = p.getObjDesc();
                result.objName = p.getObjName();
                result.result = "1";
                this.projectResults.add(result);
            }
        }
    }



    public static class ProjectResult implements Parcelable {
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.objId);
            dest.writeString(this.result);
            dest.writeString(this.isAbnormal);
            dest.writeString(this.objDesc);
            dest.writeString(this.objName);
        }

        public ProjectResult() {
        }

        protected ProjectResult(Parcel in) {
            this.objId = in.readString();
            this.result = in.readString();
            this.isAbnormal = in.readString();
            this.objDesc = in.readString();
            this.objName = in.readString();
        }

        public static final Creator<ProjectResult> CREATOR = new Creator<ProjectResult>() {
            @Override
            public ProjectResult createFromParcel(Parcel source) {
                return new ProjectResult(source);
            }

            @Override
            public ProjectResult[] newArray(int size) {
                return new ProjectResult[size];
            }
        };
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.patrolTime);
        dest.writeString(this.lineId);
        dest.writeString(this.todayIsAbnormal);
        dest.writeString(this.pointId);
        dest.writeString(this.arriveTime);
        dest.writeString(this.isAbnormal);
        dest.writeString(this.qRcode);
        dest.writeString(this.linePlaceName);
        dest.writeString(this.patrolImage);
        dest.writeList(this.projectResults);
        dest.writeString(this.photoUrl);
        dest.writeParcelable(this.uri, flags);
        dest.writeParcelable(this.uriSy, flags);
        dest.writeString(this.photoUrlSy);
    }

    protected PatrolBean(Parcel in) {
        this.id = in.readString();
        this.patrolTime = in.readString();
        this.lineId = in.readString();
        this.todayIsAbnormal = in.readString();
        this.pointId = in.readString();
        this.arriveTime = in.readString();
        this.isAbnormal = in.readString();
        this.qRcode = in.readString();
        this.linePlaceName = in.readString();
        this.patrolImage = in.readString();
        this.projectResults = new ArrayList<ProjectResult>();
        in.readList(this.projectResults, ProjectResult.class.getClassLoader());
        this.photoUrl = in.readString();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.uriSy = in.readParcelable(Uri.class.getClassLoader());
        this.photoUrlSy = in.readString();
    }

    public static final Creator<PatrolBean> CREATOR = new Creator<PatrolBean>() {
        @Override
        public PatrolBean createFromParcel(Parcel source) {
            return new PatrolBean(source);
        }

        @Override
        public PatrolBean[] newArray(int size) {
            return new PatrolBean[size];
        }
    };
}
