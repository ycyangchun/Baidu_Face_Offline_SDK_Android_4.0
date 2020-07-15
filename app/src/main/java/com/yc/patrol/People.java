package com.yc.patrol;

import java.util.List;

public class People {
    private String name;
    private String id;
    private String beginTime;
    private String fullName;
    private Line line;
    private List<PatrolProject> patrolProjects;
    private List<PatrolPoint> patrolPoints;

    public static class PatrolProject {
        private String objId;
        private String objName;
        private String objDesc;

        public String getObjId() {
            return objId;
        }

        public void setObjId(String objId) {
            this.objId = objId;
        }

        public String getObjName() {
            return objName;
        }

        public void setObjName(String objName) {
            this.objName = objName;
        }

        public String getObjDesc() {
            return objDesc;
        }

        public void setObjDesc(String objDesc) {
            this.objDesc = objDesc;
        }

        @Override
        public String toString() {
            return "PatrolProject{" +
                    "objId='" + objId + '\'' +
                    ", objName='" + objName + '\'' +
                    ", objDesc='" + objDesc + '\'' +
                    '}';
        }
    }

    public static class PatrolPoint {
        private String pid;
        private String linePlaceName;
        private String arriveTime;
        private String positionDescribe;
        private String qRcode;
        private String num;

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public String getLinePlaceName() {
            return linePlaceName;
        }

        public void setLinePlaceName(String linePlaceName) {
            this.linePlaceName = linePlaceName;
        }

        public String getArriveTime() {
            return arriveTime;
        }

        public void setArriveTime(String arriveTime) {
            this.arriveTime = arriveTime;
        }

        public String getPositionDescribe() {
            return positionDescribe;
        }

        public void setPositionDescribe(String positionDescribe) {
            this.positionDescribe = positionDescribe;
        }

        public String getqRcode() {
            return qRcode;
        }

        public void setqRcode(String qRcode) {
            this.qRcode = qRcode;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        @Override
        public String toString() {
            return "PatrolPoint{" +
                    "pid='" + pid + '\'' +
                    ", linePlaceName='" + linePlaceName + '\'' +
                    ", arriveTime='" + arriveTime + '\'' +
                    ", positionDescribe='" + positionDescribe + '\'' +
                    ", qRcode='" + qRcode + '\'' +
                    ", num='" + num + '\'' +
                    '}';
        }
    }

    public static class Line {
        private String lId;
        private String normal_Offset;
        private String abnormal_Offset;
        private String remark;

        public String getlId() {
            return lId;
        }

        public void setlId(String lId) {
            this.lId = lId;
        }

        public String getNormal_Offset() {
            return normal_Offset;
        }

        public void setNormal_Offset(String normal_Offset) {
            this.normal_Offset = normal_Offset;
        }

        public String getAbnormal_Offset() {
            return abnormal_Offset;
        }

        public void setAbnormal_Offset(String abnormal_Offset) {
            this.abnormal_Offset = abnormal_Offset;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        @Override
        public String toString() {
            return "Line{" +
                    "lId='" + lId + '\'' +
                    ", normal_Offset='" + normal_Offset + '\'' +
                    ", abnormal_Offset='" + abnormal_Offset + '\'' +
                    ", remark='" + remark + '\'' +
                    '}';
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public List<PatrolProject> getPatrolProjects() {
        return patrolProjects;
    }

    public void setPatrolProjects(List<PatrolProject> patrolProjects) {
        this.patrolProjects = patrolProjects;
    }

    public List<PatrolPoint> getPatrolPoints() {
        return patrolPoints;
    }

    public void setPatrolPoints(List<PatrolPoint> patrolPoints) {
        this.patrolPoints = patrolPoints;
    }

    @Override
    public String toString() {
        String str = "",str1 = "";
        for(PatrolProject p :patrolProjects){
            str += p.toString();
        }

        for(PatrolPoint p :patrolPoints){
            str1 += p.toString();
        }
        return "People{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", beginTime='" + beginTime + '\'' +
                ", fullName='" + fullName + '\'' +
                ", line=" + line.toString()+
                ", patrolProjects=" + str +
                ", patrolPoints=" + str1 +
                '}';
    }
}
