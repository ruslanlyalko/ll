package com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.presentation.widget.SwipeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.MyViewHolder> {

    private final List<Lesson> mLessons;
    private final OnLessonClickListener mOnLessonClickListener;

    public LessonsAdapter(OnLessonClickListener onLessonClickListener, List<Lesson> lessons) {
        mOnLessonClickListener = onLessonClickListener;
        mLessons = lessons;
    }

    @Override
    public LessonsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_report, parent, false);
        return new LessonsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final LessonsAdapter.MyViewHolder holder, final int position) {
        final Lesson lesson = mLessons.get(position);
        holder.bindData(lesson);
    }

    @Override
    public int getItemCount() {
        return mLessons.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final Resources mResources;
        @BindView(R.id.card_root) CardView mCardRoot;
        @BindView(R.id.text_user_name) TextView textUserName;
        @BindView(R.id.text_total) TextView textTotal;
        @BindView(R.id.text_bday_total) TextView textBdayTotal;
        @BindView(R.id.text_room_total) TextView textRoomTotal;
        @BindView(R.id.text_mk_total) TextView textMkTotal;
        @BindView(R.id.swipe_layout) SwipeLayout swipeLayout;
        @BindView(R.id.progress_bar) ProgressBar progressBar;
        @BindView(R.id.button_comment) ImageButton buttonComment;
        @BindView(R.id.button_mk) ImageButton buttonMk;
        @BindView(R.id.button_edit) ImageButton buttonEdit;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mResources = view.getResources();
        }

        void bindData(final Lesson lesson) {
//            textTotal.setText(mResources.getString(R.string.HRN, "" + lesson.getOrderTotal()));
//            progressBar.setMax(lesson.getOrderTotal());
            progressBar.setSecondaryProgressTintMode(PorterDuff.Mode.OVERLAY);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
            swipeLayout.setRightSwipeEnabled(true);
            swipeLayout.setBottomSwipeEnabled(false);
        }

        @OnClick(R.id.button_comment)
        void onCommentsClicked() {
            if (mOnLessonClickListener != null)
                mOnLessonClickListener.onCommentClicked(mLessons.get(getAdapterPosition()));
        }

        @OnClick(R.id.button_mk)
        void onMkClicked() {
            if (mOnLessonClickListener != null)
                mOnLessonClickListener.onMkClicked(mLessons.get(getAdapterPosition()));
        }

        @OnClick(R.id.button_edit)
        void onEditClicked() {
            if (mOnLessonClickListener != null)
                mOnLessonClickListener.onEditClicked(mLessons.get(getAdapterPosition()));
        }

        @OnLongClick(R.id.card_root)
        boolean onCardLongClicked() {
            if (mOnLessonClickListener != null)
                mOnLessonClickListener.onEditClicked(mLessons.get(getAdapterPosition()));
            return true;
        }
    }
}