package com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.models.MessageComment;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsViewHolder> implements StickyRecyclerHeadersAdapter<CommentsHeadersViewHolder> {

    private static final int VIEW_TYPE_MY = 0;
    private static final int VIEW_TYPE_ANOTHER = 1;
    private static final int VIEW_TYPE_PHOTO_MY = 2;
    private static final int VIEW_TYPE_PHOTO_ANOTHER = 3;

    private List<MessageComment> mDataSource = new ArrayList<>();
    private OnCommentClickListener mOnItemClickListener;

    public CommentsAdapter(OnCommentClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_ANOTHER:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_commet, parent, false);
                return new CommentsViewHolder(itemView, mOnItemClickListener);
            case VIEW_TYPE_PHOTO_ANOTHER:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_commet_photo, parent, false);
                return new CommentsViewHolder(itemView, mOnItemClickListener);
            case VIEW_TYPE_PHOTO_MY:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_commet_photo_my, parent, false);
                return new CommentsViewHolder(itemView, mOnItemClickListener);
            case VIEW_TYPE_MY:
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_commet_my, parent, false);
                return new CommentsViewHolder(itemView, mOnItemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(final CommentsViewHolder holder, final int position) {
        final MessageComment user = mDataSource.get(position);
        holder.bindData(user);
    }

    @Override
    public int getItemViewType(final int position) {
        MessageComment item = mDataSource.get(position);
        if (item.getFile() != null && !item.getFile().isEmpty() && !item.getRemoved()) {
            return item.getUserId().equals(FirebaseAuth.getInstance().getUid())
                    ? VIEW_TYPE_PHOTO_MY
                    : VIEW_TYPE_PHOTO_ANOTHER;
        } else {
            return item.getUserId().equals(FirebaseAuth.getInstance().getUid())
                    ? VIEW_TYPE_MY
                    : VIEW_TYPE_ANOTHER;
        }
    }

    @Override
    public long getHeaderId(final int position) {
        return DateUtils.getDateId(mDataSource.get(position).getDate());
    }

    @Override
    public CommentsHeadersViewHolder onCreateHeaderViewHolder(final ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_commet_header, parent, false);
        return new CommentsHeadersViewHolder(itemView);
    }

    @Override
    public void onBindHeaderViewHolder(final CommentsHeadersViewHolder holder, final int position) {
        holder.bindData(mDataSource.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    public MessageComment getItemAtPosition(final int position) {
        return mDataSource.get(position);
    }

    public void clearAll() {
        mDataSource.clear();
        notifyDataSetChanged();
    }

    public void add(final MessageComment messageComment) {
        if (mDataSource.contains(messageComment)) return;
        mDataSource.add(0, messageComment);
        notifyItemInserted(0);
    }

    public void addAll(final List<MessageComment> messageComments) {
        mDataSource.addAll(messageComments);
        notifyDataSetChanged();
    }

    public void setData(final List<MessageComment> messageComments) {
        mDataSource.clear();
        mDataSource.addAll(messageComments);
        notifyDataSetChanged();
    }

    public void update(final MessageComment messageComment) {
        for (int i = 0; i < mDataSource.size(); i++) {
            MessageComment current = mDataSource.get(i);
            if (messageComment.getKey().equals(current.getKey())) {
                mDataSource.set(i, messageComment);
                notifyItemChanged(i);
                return;
            }
        }
    }
}