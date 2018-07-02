package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

//
public class LessonsHeadersViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_date) TextView mTextDate;

    public LessonsHeadersViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    void bindData(final Date date) {
        Calendar month = Calendar.getInstance();
        month.setTime(date);
        mTextDate.setText(DateUtils.getMonthWithYear(mTextDate.getResources(), month));
    }
}
