package com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter;

import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.presentation.widget.SwipeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.MyViewHolder> {

    private final List<Lesson> mLessons;
    private final OnLessonClickListener mOnLessonClickListener;

    public LessonsAdapter(OnLessonClickListener onLessonClickListener, List<Lesson> lessons) {
        mOnLessonClickListener = onLessonClickListener;
        mLessons = lessons;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_lesson, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Lesson lesson = mLessons.get(position);
        holder.bindData(lesson);
    }

    @Override
    public int getItemCount() {
        return mLessons.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final Resources mResources;
        @BindView(R.id.button_comment) ImageButton mButtonComment;
        @BindView(R.id.button_edit) ImageButton mButtonEdit;
        @BindView(R.id.button_remove) ImageButton mButtonRemove;
        @BindView(R.id.swipe_menu) LinearLayout mSwipeMenu;
        @BindView(R.id.text_user_name) TextView mTextUserName;
        @BindView(R.id.text_date) TextView mTextDate;
        @BindView(R.id.text_room) TextView mTextRoom;
        @BindView(R.id.text_lesson_type) TextView mTextLessonType;
        @BindView(R.id.text_user_type) TextView mTextUserType;
        @BindView(R.id.panel_card) LinearLayout mPanelCard;
        @BindView(R.id.swipe_layout) SwipeLayout mSwipeLayout;
        @BindView(R.id.card_root) CardView mCardRoot;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mResources = view.getResources();
        }

        void bindData(final Lesson lesson) {
            mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
            mSwipeLayout.setRightSwipeEnabled(true);
            mSwipeLayout.setBottomSwipeEnabled(false);
            mTextUserName.setText(lesson.getUserName());
            String date = DateUtils.toString(lesson.getDateTime(), "dd.MM  EEEE  HH:mm  ") +
                    mResources.getString(lesson.getLessonLengthId() == 0
                            ? R.string.lesson_length_one_hour : R.string.lesson_length_one_half_hour);
            mTextDate.setText(date);
            String roomName;
            switch (lesson.getRoomType()) {
                case 0:
                    roomName = mResources.getString(R.string.tab_room_1);
                    break;
                case 1:
                    roomName = mResources.getString(R.string.tab_room_2);
                    break;
                case 2:
                    roomName = mResources.getString(R.string.tab_room_3);
                    break;
                default:
                    roomName = mResources.getString(R.string.tab_room_4);
                    break;
            }
            mTextRoom.setText(roomName);
            String lessonName;
            switch (lesson.getLessonType()) {
                case 0:
                    lessonName = mResources.getString(R.string.lesson_type_1);
                    break;
                case 1:
                    lessonName = mResources.getString(R.string.lesson_type_2);
                    break;
                case 2:
                    lessonName = mResources.getString(R.string.lesson_type_3);
                    break;
                default:
                    lessonName = mResources.getString(R.string.lesson_type_4);
                    break;
            }
            mTextLessonType.setText(lessonName);
            mTextUserType.setText(lesson.getUserType() == 0 ? R.string.user_type_adult : R.string.user_type_child);
        }

        @OnClick(R.id.button_comment)
        void onCommentsClicked() {
            if (mOnLessonClickListener != null)
                mOnLessonClickListener.onCommentClicked(mLessons.get(getAdapterPosition()));
        }

        @OnClick(R.id.button_edit)
        void onEditClicked() {
            if (mOnLessonClickListener != null)
                mOnLessonClickListener.onEditClicked(mLessons.get(getAdapterPosition()));
        }

        @OnClick(R.id.button_remove)
        void onMkClicked() {
            if (mOnLessonClickListener != null)
                mOnLessonClickListener.onRemoveClicked(mLessons.get(getAdapterPosition()));
        }
    }
}