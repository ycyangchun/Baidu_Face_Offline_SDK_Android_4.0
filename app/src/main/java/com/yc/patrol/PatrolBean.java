package com.yc.patrol;

import android.net.Uri;

public class PatrolBean {
    private String time;
    private String place;
    private String photoUrl;
    private Uri uri;
    private Uri uriSy;
    private String photoUrlSy;

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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

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
