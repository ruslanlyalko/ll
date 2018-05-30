package com.ruslanlyalko.ll.presentation.ui.main.dialogs.details;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.Constants;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Message;
import com.ruslanlyalko.ll.data.models.MessageComment;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.adapter.CommentsAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.adapter.OnCommentClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.dialogs.edit.DialogEditActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.ProfileActivity;
import com.ruslanlyalko.ll.presentation.widget.PhotoPreviewActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.EasyPermissions;

public class DialogActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, OnCommentClickListener {

    private static final int REQUEST_IMAGE_PERMISSION = 1001;
    @BindView(R.id.text_description) TextView textDescription;
    @BindView(R.id.list_comments) RecyclerView mListComments;
    @BindView(R.id.card_comments_send) CardView mCardCommentsSend;
    @BindView(R.id.edit_comment) EditText mEditComment;
    @BindView(R.id.button_send) FloatingActionButton mButtonSend;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.button_attachments) ImageView mButtonAttachments;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private CommentsAdapter mCommentsAdapter = new CommentsAdapter(this);
    private String mMessageKey;
    private Message mMessage;

    public static Intent getLaunchIntent(final Context launchIntent, final String messageId) {
        Intent intent = new Intent(launchIntent, DialogActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_DIALOG_ID, messageId);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_dialog;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMessageKey = bundle.getString(Keys.Extras.EXTRA_DIALOG_ID);
        }
    }

    @Override
    protected void setupView() {
        setupRecycler();
        FirebaseUtils.markNotificationsAsRead(mMessageKey);
        loadDetailsFromDB();
        loadCommentsFromDB();
        mListComments.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                mListComments.postDelayed(() -> mListComments.smoothScrollToPosition(mCommentsAdapter.getItemCount()), 500);
            }
        });
        mCommentsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (isDestroyed()) return;
                mListComments.smoothScrollToPosition(mCommentsAdapter.getItemCount());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_edit:
                if (FirebaseUtils.isAdmin()
                        || mMessage.getUserId().equals(mUser.getUid())) {
                    editMk();
                }
                break;
            case R.id.action_delete:
                if (FirebaseUtils.isAdmin()
                        || mMessage.getUserId().equals(mUser.getUid())) {
                    deleteMk();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecycler() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);//true
        mLayoutManager.setReverseLayout(false);//false
        mListComments.setLayoutManager(mLayoutManager);
        mListComments.setAdapter(mCommentsAdapter);
    }

    private void loadDetailsFromDB() {
        if (mMessageKey == null || mMessageKey.isEmpty()) return;
        database.getReference(DC.DB_DIALOGS)
                .child(mMessageKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMessage = dataSnapshot.getValue(Message.class);
                        updateUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void loadCommentsFromDB() {
        if (mMessageKey == null || mMessageKey.isEmpty()) return;
        database.getReference(DC.DB_MESSAGES)
                .child(mMessageKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<MessageComment> list = new ArrayList<>();
                        for (DataSnapshot commentSS : dataSnapshot.getChildren()) {
                            MessageComment messageComment = commentSS.getValue(MessageComment.class);
                            if (messageComment != null) {
                                list.add(messageComment);
                            }
                        }
                        mCommentsAdapter.setData(list);
                        new Handler().postDelayed(() -> {
                            if (isDestroyed() || mMessage == null) return;
                            if (mMessage.getCommentsEnabled()) {
                                loadMoreCommentsFromDB();
                                mListComments.smoothScrollToPosition(list.size());
                            }
                        }, 120);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void updateUI() {
        if (mMessage != null) {
            invalidateOptionsMenu();
            mCardCommentsSend.setVisibility(mMessage.getCommentsEnabled() ? View.VISIBLE : View.GONE);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(mMessage.getTitle1());
            if (mMessage.getDescription() != null & !mMessage.getDescription().isEmpty()) {
                textDescription.setVisibility(View.VISIBLE);
                textDescription.setText(mMessage.getDescription());
            } else {
                textDescription.setVisibility(View.GONE);
            }
        } else {
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.title_activity_notification_item);
        }
    }

    private void loadMoreCommentsFromDB() {
        database.getReference(DC.DB_MESSAGES)
                .child(mMessageKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, final String s) {
                MessageComment messageComment = dataSnapshot.getValue(MessageComment.class);
                if (messageComment != null) {
                    if (!isDestroyed()) {
                        mCommentsAdapter.add(messageComment);//todo
                        FirebaseUtils.markNotificationsAsRead(mMessageKey);
                    }
                }
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, final String s) {
                MessageComment messageComment = dataSnapshot.getValue(MessageComment.class);
                if (messageComment != null) {
                    mCommentsAdapter.update(messageComment);
                    FirebaseUtils.markNotificationsAsRead(mMessageKey);
                }
            }

            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(final DataSnapshot dataSnapshot, final String s) {
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    private void editMk() {
        Intent intent = new Intent(DialogActivity.this, DialogEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, mMessageKey);
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
    }

    private void deleteMk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DialogActivity.this);
        AlertDialog dialog = builder.setTitle(R.string.dialog_delete_notification_title)
                .setMessage(R.string.dialog_delete_notification_message)
                .setPositiveButton("Видалити", (dialog1, which) -> {
                    database.getReference(DC.DB_DIALOGS)
                            .child(mMessage.getKey())
                            .removeValue();
                    FirebaseUtils.clearNotificationsForAllUsers(mMessage.getKey());
                    onBackPressed();
                })
                .setNegativeButton("Повернутись", null).create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mMessage != null) {
            menu.findItem(R.id.action_delete).setVisible(FirebaseUtils.isAdmin()
                    || mMessage.getUserId().equals(mUser.getUid()));
            menu.findItem(R.id.action_edit).setVisible(FirebaseUtils.isAdmin()
                    || mMessage.getUserId().equals(mUser.getUid()));
        }
        return true;
    }

    @Override
    public void onItemClicked(View view, final int position) {
        MessageComment item = mCommentsAdapter.getItemAtPosition(position);
        if (item.getFile() != null && !item.getFile().isEmpty() && !item.getRemoved()) {
//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "image");
            startActivity(PhotoPreviewActivity.getLaunchIntent(this, item.getFile(), item.getUserName()));
        } else
            Toast.makeText(this, DateUtils.toString(mCommentsAdapter.getItemAtPosition(position).getDate(),
                    "EEEE dd.MM.yyyy").toUpperCase(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserClicked(final int position) {
        MessageComment item = mCommentsAdapter.getItemAtPosition(position);
        startActivity(ProfileActivity.getLaunchIntent(this, item.getUserId()));
    }

    @Override
    public void onItemLongClicked(final int position) {
        MessageComment item = mCommentsAdapter.getItemAtPosition(position);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_remove_title)
                .setMessage(item.getMessage())
                .setPositiveButton("Видалити", (dialog, which) -> {
                    removeMessage(item);
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }

    private void removeMessage(final MessageComment item) {
        database.getReference(DC.DB_MESSAGES)
                .child(mMessageKey)
                .child(item.getKey())
                .child(DC.DIALOG_MESSAGE_REMOVED)
                .setValue(true);
    }

    @OnClick(R.id.fab)
    public void onDownButtonClicked() {
        mListComments.smoothScrollToPosition(mCommentsAdapter.getItemCount());
        mFab.hide();
    }

    @OnClick(R.id.button_send)
    public void onSendButtonClicked() {
        String comment = mEditComment.getText().toString().trim();
        mEditComment.setText("");
        if (comment.isEmpty()) return;
        if (mMessageKey.isEmpty()) return;
        sendComment(comment);
    }

    private void sendComment(String comment) {
        DatabaseReference ref = database.getReference(DC.DB_MESSAGES)
                .child(mMessageKey)
                .push();
        ref.setValue(new MessageComment(ref.getKey(), comment, FirebaseUtils.getUser()));
    }

    @OnClick(R.id.button_attachments)
    public void onAttachmentsViewClicked() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            chooseFileToUpload();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.image_permissions), REQUEST_IMAGE_PERMISSION, perms);
        }
    }

    void chooseFileToUpload() {
        EasyImage.openChooserWithGallery(this, getString(R.string.text_choose_images), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_EDIT) {
            loadDetailsFromDB();
        }
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(final File imageFile, final EasyImage.ImageSource source, final int type) {
                onPhotosReturned(imageFile);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private static final int HIDE_THRESHOLD = 20;
            private int scrolledDistance = 0;
            private boolean controlsVisible = true;

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    mFab.hide();
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    mFab.show();
                    controlsVisible = true;
                    scrolledDistance = 0;
                }
                if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                    scrolledDistance += dy;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void onPhotosReturned(final File imageFile) {
        try {
            showProgress(true);
            final String filenameOriginal = DateUtils.getCurrentTimeStamp() + "_original" + ".jpg";
            final String filenameThumbnail = DateUtils.getCurrentTimeStamp() + "_thumbnail" + ".jpg";
            uploadFile(imageFile, filenameOriginal, 95).addOnSuccessListener(taskSnapshot ->
                    uploadFile(imageFile, filenameThumbnail, 30)
                            .addOnSuccessListener(taskSnapshot1 -> {
                                showProgress(false);
                                if (taskSnapshot.getDownloadUrl() == null) return;
                                if (taskSnapshot1.getDownloadUrl() == null) return;
                                String origin = taskSnapshot.getDownloadUrl().toString();
                                String thumbnail = taskSnapshot1.getDownloadUrl().toString();
                                sendCommentFile(origin, thumbnail);
                            }).addOnFailureListener(exception -> showProgress(false)))
                    .addOnFailureListener(exception -> showProgress(false));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }

    private void showProgress(final boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mButtonAttachments.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private UploadTask uploadFile(File file, String fileName, int quality) {
        Bitmap bitmapOriginal = BitmapFactory.decodeFile(file.toString());//= imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapOriginal.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        // Meta data for imageView
        // name of file in Storage
        return FirebaseStorage.getInstance()
                .getReference(DC.STORAGE_DIALOGS_MESSAGES)
                .child(fileName)
                .putBytes(bytes);
    }

    private void sendCommentFile(String file, String thumbnail) {
        DatabaseReference ref = database.getReference(DC.DB_MESSAGES)
                .child(mMessageKey)
                .push();
        ref.setValue(new MessageComment(ref.getKey(), "фото", file, thumbnail, FirebaseUtils.getUser()));
    }

    @Override
    public void onPermissionsGranted(final int requestCode, final List<String> perms) {
        chooseFileToUpload();
    }

    @Override
    public void onPermissionsDenied(final int requestCode, final List<String> perms) {
    }
}
