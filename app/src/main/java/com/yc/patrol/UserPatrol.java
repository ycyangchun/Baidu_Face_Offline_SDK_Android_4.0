package com.yc.patrol;

public class UserPatrol {
    private String name = "user";
    private String id;
    private String lineId;
    private String todayIsAbnormal;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
