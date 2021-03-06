package com.ruslanlyalko.ll.data.models;

import com.ruslanlyalko.ll.presentation.utils.UserType;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ruslan Lyalko
 * on 29.01.2018.
 */

public class Contact implements Serializable {

    private String key;
    private String userId;
    private String userName;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String phone2;
    private int userType;
    private Date createdAt = new Date();
    private Date birthDay = new Date();
    private String description;
    private int totalIncome;
    private int totalExpense;
    private boolean isArchived;

    public boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(final boolean archived) {
        isArchived = archived;
    }

    public Contact() {
        userType = UserType.ADULT;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(final Date birthDay) {
        this.birthDay = birthDay;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(final String phone2) {
        this.phone2 = phone2;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(final int userType) {
        this.userType = userType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(final int totalIncome) {
        this.totalIncome = totalIncome;
    }

    public int getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(final int totalExpense) {
        this.totalExpense = totalExpense;
    }

    public boolean hasUser() {
        return userName != null && !userName.isEmpty();
    }
}