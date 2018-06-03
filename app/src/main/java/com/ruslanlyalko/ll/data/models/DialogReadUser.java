package com.ruslanlyalko.ll.data.models;

import java.util.Date;

public class DialogReadUser {

    private String id;
    private String fullName;
    private String avatar;
    private Date date;

    public DialogReadUser() {
    }

    public DialogReadUser(final String id, final String fullName, final String avatar) {
        this.id = id;
        this.fullName = fullName;
        this.avatar = avatar;
        this.date = new Date();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
    }
}
