package com.ruslanlyalko.ll.data.models;

import com.ruslanlyalko.ll.common.UserType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Lesson implements Serializable {

    private String key;
    private String description;
    private UserType userType;
    private int roomId;
    private int lessonId;
    private int lessonLengthId;
    private String userId;
    private String userName;
    private Date dateTime = new Date();
    private List<String> clients = new ArrayList<>();

    public Lesson(final String userId, final String userName) {
        this.key = "";
        this.userId = userId;
        this.userName = userName;
    }

    public Lesson() {
        key = "";
    }

    public Lesson(final Date date, final String key) {
        this.key = key;
        this.dateTime = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public List<String> getClients() {
        return clients;
    }

    public void setClients(final List<String> clients) {
        this.clients = clients;
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
