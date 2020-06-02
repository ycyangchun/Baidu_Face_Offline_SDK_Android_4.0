package com.yc.patrol;

public class PatrolBean {
    private String time;
    private String place;
    private String photoUrl;

    public PatrolBean(String time, String place, String photoUrl) {
        this.time = time;
        this.place = place;
        this.photoUrl = photoUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
