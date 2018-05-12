package com.ruslanlyalko.ll.data.models;

import java.util.Date;

/**
 * Created by Ruslan Lyalko
 * on 11.05.2018.
 */
public class SettingsSalary {

    private String key;
    private Date dateFrom = new Date();
    private int studentPrivate;
    private int teacherPrivate;
    private int studentPair;
    private int teacherPair;
    private int studentOnLine;
    private int teacherOnLine;
    private int studentGroup;
    private int teacherGroup;
    private int studentPrivate15;
    private int teacherPrivate15;
    private int studentPair15;
    private int teacherPair15;
    private int studentOnLine15;
    private int teacherOnLine15;
    private int studentGroup15;
    private int teacherGroup15;

    public SettingsSalary() {
        key = "first_key";
        dateFrom.setMonth(1);
        dateFrom.setDate(1);
    }

    public int getStudentPrivate15() {
        return studentPrivate15;
    }

    public void setStudentPrivate15(final int studentPrivate15) {
        this.studentPrivate15 = studentPrivate15;
    }

    public int getTeacherPrivate15() {
        return teacherPrivate15;
    }

    public void setTeacherPrivate15(final int teacherPrivate15) {
        this.teacherPrivate15 = teacherPrivate15;
    }

    public int getStudentPair15() {
        return studentPair15;
    }

    public void setStudentPair15(final int studentPair15) {
        this.studentPair15 = studentPair15;
    }

    public int getTeacherPair15() {
        return teacherPair15;
    }

    public void setTeacherPair15(final int teacherPair15) {
        this.teacherPair15 = teacherPair15;
    }

    public int getStudentOnLine15() {
        return studentOnLine15;
    }

    public void setStudentOnLine15(final int studentOnLine15) {
        this.studentOnLine15 = studentOnLine15;
    }

    public int getTeacherOnLine15() {
        return teacherOnLine15;
    }

    public void setTeacherOnLine15(final int teacherOnLine15) {
        this.teacherOnLine15 = teacherOnLine15;
    }

    public int getStudentGroup15() {
        return studentGroup15;
    }

    public void setStudentGroup15(final int studentGroup15) {
        this.studentGroup15 = studentGroup15;
    }

    public int getTeacherGroup15() {
        return teacherGroup15;
    }

    public void setTeacherGroup15(final int teacherGroup15) {
        this.teacherGroup15 = teacherGroup15;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(final Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public int getStudentPrivate() {
        return studentPrivate;
    }

    public void setStudentPrivate(final int studentPrivate) {
        this.studentPrivate = studentPrivate;
    }

    public int getTeacherPrivate() {
        return teacherPrivate;
    }

    public void setTeacherPrivate(final int teacherPrivate) {
        this.teacherPrivate = teacherPrivate;
    }

    public int getStudentPair() {
        return studentPair;
    }

    public void setStudentPair(final int studentPair) {
        this.studentPair = studentPair;
    }

    public int getTeacherPair() {
        return teacherPair;
    }

    public void setTeacherPair(final int teacherPair) {
        this.teacherPair = teacherPair;
    }

    public int getStudentOnLine() {
        return studentOnLine;
    }

    public void setStudentOnLine(final int studentOnLine) {
        this.studentOnLine = studentOnLine;
    }

    public int getTeacherOnLine() {
        return teacherOnLine;
    }

    public void setTeacherOnLine(final int teacherOnLine) {
        this.teacherOnLine = teacherOnLine;
    }

    public int getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(final int studentGroup) {
        this.studentGroup = studentGroup;
    }

    public int getTeacherGroup() {
        return teacherGroup;
    }

    public void setTeacherGroup(final int teacherGroup) {
        this.teacherGroup = teacherGroup;
    }

    public boolean hasKey() {
        return key != null && !key.isEmpty();
    }
}
