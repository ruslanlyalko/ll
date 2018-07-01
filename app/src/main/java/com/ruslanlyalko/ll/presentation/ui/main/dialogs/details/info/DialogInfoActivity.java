package com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.DialogReadUser;
import com.ruslanlyalko.ll.data.models.MessageComment;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class DialogInfoActivity extends BaseActivity {

    @BindView(R.id.list_users) RecyclerView mListUsers;
    private UsersInfoAdapter mUsersInfoAdapter = new UsersInfoAdapter();
    private MessageComment mMessageComment = new MessageComment();
    private String mDialogId = "";

    public static Intent getLaunchIntent(final Context launchIntent, String dialogId, final MessageComment messageComment) {
        Intent intent = new Intent(launchIntent, DialogInfoActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(Keys.Extras.EXTRA_COMMENT_MODEL, messageComment);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_dialog_info;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mDialogId = bundle.getString(Keys.Extras.EXTRA_DIALOG_ID);
            mMessageComment = (MessageComment) bundle.getSerializable(Keys.Extras.EXTRA_COMMENT_MODEL);
        }
    }

    @Override
    protected void setupView() {
        setTitle(mMessageComment.getMessage());
        setupRecycler();
        loadDetailsFromDB();
    }

    private void setupRecycler() {
        mListUsers.setLayoutManager(new LinearLayoutManager(this));
        mListUsers.setAdapter(mUsersInfoAdapter);
    }

    private void loadDetailsFromDB() {
        getDB(DC.DB_DIALOGS_READ_DATE)
                .child(mDialogId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<DialogReadUser> list = new ArrayList<>();
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            DialogReadUser user = item.getValue(DialogReadUser.class);
                            if (user != null && mMessageComment.getDate().before(user.getDate()))
                                list.add(user);
                        }
                        mUsersInfoAdapter.setData(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}
