package com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

//
public class CommentsHeadersViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_date) TextView mTextDate;

    public CommentsHeadersViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    void bindData(final Date date) {
        mTextDate.setText(DateUtils.getHowLongTime(mTextDate.getResources(), date, "dd MMMM yyyy"));
    }
}
