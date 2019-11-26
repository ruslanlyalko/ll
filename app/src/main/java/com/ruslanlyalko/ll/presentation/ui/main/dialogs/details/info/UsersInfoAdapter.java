package com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.info;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.models.DialogReadUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UsersInfoAdapter extends RecyclerView.Adapter<UsersInfoAdapter.MyViewHolder> {

    private List<DialogReadUser> mDataSource = new ArrayList<>();

    UsersInfoAdapter() {
    }

    @NonNull
    @Override
    public UsersInfoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_user_info, parent, false);
        return new UsersInfoAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersInfoAdapter.MyViewHolder holder, final int position) {
        final DialogReadUser user = mDataSource.get(position);
        holder.bindData(user);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    public void setData(final List<DialogReadUser> users) {
        mDataSource.clear();
        mDataSource.addAll(users);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_user_name) TextView mTextName;
        @BindView(R.id.image_avatar) ImageView mImageAvatar;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(final DialogReadUser user) {
            mTextName.setText(user.getFullName());
            Glide.with(mImageAvatar).load(user.getAvatar()).into(mImageAvatar);
        }
    }
}