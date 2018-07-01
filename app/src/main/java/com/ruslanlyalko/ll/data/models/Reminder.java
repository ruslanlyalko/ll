package com.ruslanlyalko.ll.data.models;

import java.io.Serializable;

/**
 * Created by Ruslan Lyalko
 * on 01.07.2018.
 */

public class Reminder implements Serializable {

    private String beforeLessonReminder;
    private String afterLessonReminder;

    public Reminder() {
    }

    public String getBeforeLessonReminder() {
        return beforeLessonReminder;
    }

    public void setBeforeLessonReminder(final String beforeLessonReminder) {
        this.beforeLessonReminder = beforeLessonReminder;
    }

    public String getAfterLessonReminder() {
        return afterLessonReminder;
    }

    public void setAfterLessonReminder(final String afterLessonReminder) {
        this.afterLessonReminder = afterLessonReminder;
    }
}
