package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.LessonsAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.OnLessonClickListener;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

public class LessonsHeaderAdapter extends LessonsAdapter implements StickyRecyclerHeadersAdapter<LessonsHeadersViewHolder> {

    public LessonsHeaderAdapter(OnLessonClickListener onLessonClickListener) {
        mOnLessonClickListener = onLessonClickListener;
    }

    @Override
    public long getHeaderId(final int position) {
        return DateUtils.getMonthId(mLessons.get(position).getDateTime());
    }

    @Override
    public LessonsHeadersViewHolder onCreateHeaderViewHolder(final ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_lesson_header, parent, false);
        return new LessonsHeadersViewHolder(itemView);
    }

    @Override
    public void onBindHeaderViewHolder(final LessonsHeadersViewHolder holder, final int position) {
        holder.bindData(mLessons.get(position).getDateTime());
    }
}