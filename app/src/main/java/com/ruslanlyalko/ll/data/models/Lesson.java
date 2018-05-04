package com.ruslanlyalko.ll.data.models;

import com.ruslanlyalko.ll.common.UserType;

import java.util.Date;

public class Lesson {

    private String key;
    private String description;
    private UserType userType;
    private int roomId;
    private int lessonId;
    private int lessonLengthId;
    private Date dateTime = new Date();

    public Lesson() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(final UserType userType) {
        this.userType = userType;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(final int roomId) {
        this.roomId = roomId;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(final int lessonId) {
        this.lessonId = lessonId;
    }

    public int getLessonLengthId() {
        return lessonLengthId;
    }

    public void setLessonLengthId(final int lessonLengthId) {
        this.lessonLengthId = lessonLengthId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(final Date dateTime) {
        this.dateTime = dateTime;
    }
}
