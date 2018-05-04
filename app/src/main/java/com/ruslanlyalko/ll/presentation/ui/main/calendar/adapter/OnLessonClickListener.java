package com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter;

import com.ruslanlyalko.ll.data.models.Lesson;

/**
 * Created by Ruslan Lyalko
 * on 12.11.2017.
 */

public interface OnLessonClickListener {

    void onCommentClicked(Lesson lesson);

    void onMkClicked(Lesson lesson);

    void onEditClicked(Lesson lesson);
}
