package com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.adapter;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.models.MessageComment;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Optional;

//
public class CommentsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_comment_time) TextView mTextCommentTime;
    @Nullable
    @BindView(R.id.image_user)
    ImageView mImageUser;
    @Nullable
    @BindView(R.id.text_user_name)
    TextView mTextUserName;
    @Nullable
    @BindView(R.id.text_comment)
    TextView mTextComment;
    @Nullable
    @BindView(R.id.image_view)
    ImageView mImageView;
    @Nullable
    @BindView(R.id.image_edit)
    ImageView mImageEdit;

    private final OnCommentClickListener mOnCommentClickListener;
    private final Resources mResources;

    public CommentsViewHolder(View view, final OnCommentClickListener onCommentClickListener) {
        super(view);
        mOnCommentClickListener = onCommentClickListener;
        mResources = view.getResources();
        ButterKnife.bind(this, view);
    }

    void bindData(final MessageComment messageComment) {
        if (mTextUserName != null)
            mTextUserName.setText(messageComment.getUserName());
        if (mTextComment != null) {
            mTextComment.setText(messageComment.getRemoved() ? mResources.getString(R.string.text_message_removed) : messageComment.getMessage());
            mTextComment.setTextColor(ContextCompat.getColor(mTextComment.getContext(),
                    messageComment.getRemoved() ? R.color.colorComment : R.color.colorBlack));
        }
        if (mImageEdit != null)
            mImageEdit.setVisibility(messageComment.getEdited() ? View.VISIBLE : View.GONE);
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
        if (mImageUser != null)
            try {
                if (messageComment.getUserAvatar() != null && !messageComment.getUserAvatar().isEmpty()) {
                    Glide.with(mTextCommentTime.getContext())
                            .load(messageComment.getUserAvatar())
                            .apply(new RequestOptions().circleCrop())
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

    @Optional
    @OnClick(R.id.image_user)
    void onUserCLick() {
        if (mOnCommentClickListener != null)
            mOnCommentClickListener.onUserClicked(getAdapterPosition());
    }

    @OnLongClick(R.id.linear_root)
    boolean onItemLongCLick() {
        if (mOnCommentClickListener == null) return false;
        mOnCommentClickListener.onItemLongClicked(mTextCommentTime, getAdapterPosition());
        return true;
    }
}
