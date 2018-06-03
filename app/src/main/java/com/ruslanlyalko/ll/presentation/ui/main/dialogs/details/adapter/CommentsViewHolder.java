package com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.models.MessageComment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

//
public class CommentsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_comment_time) TextView mTextCommentTime;
    @BindView(R.id.image_user) ImageView mImageUser;
    @Nullable
    @BindView(R.id.text_user_name)
    TextView mTextUserName;
    @Nullable
    @BindView(R.id.text_comment)
    TextView mTextComment;
    @Nullable
    @BindView(R.id.image_view)
    ImageView mImageView;

    private OnCommentClickListener mOnCommentClickListener;

    public CommentsViewHolder(View view, final OnCommentClickListener onCommentClickListener) {
        super(view);
        mOnCommentClickListener = onCommentClickListener;
        ButterKnife.bind(this, view);
    }

    void bindData(final MessageComment messageComment) {
        if (mTextUserName != null)
            mTextUserName.setText(messageComment.getUserName());
        if (mTextComment != null) {
            mTextComment.setText(messageComment.getRemoved() ? "Повідомлення видалено" : messageComment.getMessage());
            mTextComment.setTextColor(ContextCompat.getColor(mTextComment.getContext(),
                    messageComment.getRemoved() ? R.color.colorComment : R.color.colorBlack));
        }
        mTextCommentTime.setText(DateUtils.toString(messageComment.getDate(), "HH:mm"));
        try {
            if (messageComment.getThumbnail() != null && !messageComment.getThumbnail().isEmpty() && mImageView != null) {
                Glide.with(mTextCommentTime.getContext())
                        .load(messageComment.getThumbnail())
                        .into(mImageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (messageComment.getUserAvatar() != null && !messageComment.getUserAvatar().isEmpty()) {
                Glide.with(mTextCommentTime.getContext())
                        .load(messageComment.getUserAvatar())
                        .into(mImageUser);
            } else {
                mImageUser.setImageResource(R.drawable.ic_user_name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.linear_root)
    void onItemCLick() {
        if (mOnCommentClickListener != null)
            mOnCommentClickListener.onItemClicked(mImageView, getAdapterPosition());
    }

    @OnClick(R.id.card_user)
    void onUserCLick() {
        if (mOnCommentClickListener != null)
            mOnCommentClickListener.onUserClicked(getAdapterPosition());
    }

    @OnLongClick(R.id.linear_root)
    boolean onItemLongCLick() {
        if (mOnCommentClickListener == null) return false;
        mOnCommentClickListener.onItemLongClicked(mImageUser,getAdapterPosition());
        return true;
    }
}
