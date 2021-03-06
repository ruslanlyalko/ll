package com.ruslanlyalko.ll.presentation.ui.main.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.User;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.login.LoginActivity;
import com.ruslanlyalko.ll.presentation.ui.login.SignupActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.adapter.UsersAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.profile.dashboard.DashboardActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.reminder.ReminderActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.salary.SalaryActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.salary_edit.SalaryEditActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.settings.ProfileSettingsActivity;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;
import com.ruslanlyalko.ll.presentation.utils.Keys;
import com.ruslanlyalko.ll.presentation.utils.PreferenceHelper;
import com.ruslanlyalko.ll.presentation.widget.OnItemClickListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.EasyPermissions;

public class ProfileActivity extends BaseActivity implements OnItemClickListener, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_IMAGE_PERMISSION = 1;

    @BindView(R.id.text_email) TextView mEmailText;
    @BindView(R.id.text_phone) TextView mPhoneText;
    @BindView(R.id.text_bday) TextView mBDayText;
    @BindView(R.id.text_card) TextView mCardText;
    @BindView(R.id.text_position_title) TextView mTitlePositionText;
    @BindView(R.id.text_time) TextView mTimeText;
    @BindView(R.id.text_first_date) TextView mFirstDateText;
    @BindView(R.id.panel_first_date) LinearLayout mFirsDateLayout;
    @BindView(R.id.panel_phone) LinearLayout mPhoneLayout;
    @BindView(R.id.panel_email) LinearLayout mEmailLayout;
    @BindView(R.id.panel_card) LinearLayout mCardLayout;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.card_friends) CardView mCardView;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.image_view_ava) ImageView mAvaImageView;
    @BindView(R.id.image_view_back) ImageView mBackImageView;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.text_available) TextView mTextAvailable;
    @BindView(R.id.text_last_online) TextView mTextLastOnline;
    @BindView(R.id.progress_bar) ContentLoadingProgressBar mProgressBar;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mUID;
    private User mUser;
    private UsersAdapter mUsersAdapter;
    private boolean mIsCurrentUserPage;
    private Date mLastOnline = new Date();
    private Boolean mConnected;

    public static Intent getLaunchIntent(final Activity launchIntent) {
        return new Intent(launchIntent, ProfileActivity.class);
    }

    public static Intent getLaunchIntent(final Activity launchIntent, final String userId) {
        Intent intent = new Intent(launchIntent, ProfileActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_UID, userId);
        return intent;
    }

    @Override
    protected boolean isLeftView() {
        return true;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile;
    }

    @Override
    public void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mUID = bundle.getString(Keys.Extras.EXTRA_UID, getFirebaseUser().getUid());
        } else
            mUID = getFirebaseUser().getUid();
        mIsCurrentUserPage = mUID.equals(getFirebaseUser().getUid());
    }

    @Override
    protected void setupView() {
        if(mIsCurrentUserPage) {
            mUser = getCurrentUser();
            updateUI();
        } else {
            collapsingToolbar.setTitle(" ");
        }
        initRecycle();
        loadUsers();
        checkConnection();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_user: {
                startActivity(new Intent(this, SignupActivity.class));
                return true;
            }
            case R.id.action_settings: {
                startActivity(ProfileSettingsActivity.getLaunchIntent(this, mUID));
                return true;
            }
            case R.id.action_settings_salary: {
                startActivity(SalaryEditActivity.getLaunchIntent(this));
                return true;
            }
            case R.id.action_dashboard: {
                startActivity(DashboardActivity.getLaunchIntent(this));
                return true;
            }
            case R.id.action_reminder: {
                startActivity(ReminderActivity.getLaunchIntent(this));
                return true;
            }
            case R.id.action_change_ava: {
                choosePhoto();
                return true;
            }
            case R.id.action_logout: {
                logout();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initRecycle() {
        mUsersAdapter = new UsersAdapter(this, getCurrentUser());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mUsersAdapter);
        if(mIsCurrentUserPage) {
            mCardView.setVisibility(View.VISIBLE);
        } else {
            mCardView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                showProgressBarUpload();
                String imageFileName = DateUtils.getCurrentTimeStamp() + "_original" + ".jpg";
                uploadFile(resultUri, imageFileName, 95).addOnSuccessListener(taskSnapshot -> {
                    if(taskSnapshot.getDownloadUrl() != null) {
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("avatar", taskSnapshot.getDownloadUrl().toString());
                        mDatabase.getReference().child(DC.DB_USERS).child(mUID).updateChildren(childUpdates)
                                .addOnCompleteListener(task ->
                                        hideProgressBarUpload());
                    }
                }).addOnFailureListener(exception -> hideProgressBarUpload());
            } else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        switch (requestCode) {
            case REQUEST_IMAGE_PERMISSION:
                EasyImage.openChooserWithGallery(this, getString(R.string.text_choose_images), 0);
                break;
        }
    }

    @Override
    public void onPermissionsDenied(final int requestCode, final List<String> perms) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    private void loadUsers() {
        getDataManager().getAllUsers().observe(this, list -> {
            if(list == null) return;
            boolean isAdmin = getCurrentUser().getIsAdmin();
            List<User> users = new ArrayList<>();
            for (User user : list) {
                if(user.getId().equals(mUID)) {
                    mUser = user;
                    updateUI();
                } else if(mIsCurrentUserPage) {
                    if(isAdmin || !user.getIsBlocked())
                        users.add(user);
                }
            }
            mUsersAdapter.setData(users);
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.action_add_user).setVisible(getCurrentUser().getIsAdmin() && mIsCurrentUserPage);
        menu.findItem(R.id.action_settings_salary).setVisible(getCurrentUser().getIsAdmin() && mIsCurrentUserPage);
        menu.findItem(R.id.action_dashboard).setVisible(getCurrentUser().getIsAdmin() && mIsCurrentUserPage);
        menu.findItem(R.id.action_reminder).setVisible(getCurrentUser().getIsAdmin() && mIsCurrentUserPage);
        menu.findItem(R.id.action_settings).setVisible(getCurrentUser().getIsAdmin() || mIsCurrentUserPage);
        menu.findItem(R.id.action_change_ava).setVisible(mIsCurrentUserPage);
        menu.findItem(R.id.action_logout).setVisible(mIsCurrentUserPage);
        return true;
    }

    private void checkConnection() {
        if(mIsCurrentUserPage) {
            mDatabase.getReference(".info/connected")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            mConnected = snapshot.getValue(Boolean.class);
                            updateLastOnline();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            System.err.println("Listener was cancelled");
                        }
                    });
        }
    }

    private void updateUI() {
        if(isDestroyed()) return;
        if(mUser == null || getFirebaseUser() == null) return;
        // if current mUser is admin or open his friends
        fab.setVisibility(getCurrentUser().getIsAdmin() || mIsCurrentUserPage ? View.VISIBLE : View.GONE);
//        if (mUser.getIsAdmin() && mIsCurrentUserPage)
//            fab.setImageResource(R.drawable.ic_action_money);
        mTitlePositionText.setText(mUser.getPositionTitle());
        collapsingToolbar.setTitle(mUser.getFullName());
        mPhoneText.setText(mUser.getPhone());
        mEmailText.setText(mUser.getEmail());
        mBDayText.setText(mUser.getBirthdayDate());
        mCardText.setText(mUser.getCard());
        String time = mUser.getWorkingStartTime() + " - " + mUser.getWorkingEndTime();
        mTimeText.setText(time);
        mFirstDateText.setText(mUser.getWorkingFromDate());
        final String phone = mUser.getPhone();
        mPhoneLayout.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
        });
        final String email = mUser.getEmail();
        mEmailLayout.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(email, email);
            if(clipboard != null)
                clipboard.setPrimaryClip(clip);
            Toast.makeText(ProfileActivity.this, getString(R.string.toast_text_copied), Toast.LENGTH_SHORT).show();
        });
        final String card = mUser.getCard();
        mCardLayout.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(card, card);
            if(clipboard != null)
                clipboard.setPrimaryClip(clip);
            Toast.makeText(ProfileActivity.this, getString(R.string.toast_text_copied), Toast.LENGTH_SHORT).show();
        });
        if(getCurrentUser().getIsAdmin() && !mIsCurrentUserPage) {
            mFirsDateLayout.setVisibility(View.VISIBLE);
        }
        if(mUser.getAvatar() != null && !mUser.getAvatar().isEmpty()) {
            mAvaImageView.setVisibility(View.VISIBLE);
            mBackImageView.setVisibility(View.VISIBLE);
            if(!isDestroyed())
                Glide.with(this)
                        .load(mUser.getAvatar())
                        .into(mAvaImageView);
        } else {
            mAvaImageView.setVisibility(View.GONE);
            mBackImageView.setVisibility(View.GONE);
        }
        updateLastOnline();
    }

    private void updateLastOnline() {
        if(isDestroyed()) return;
        if(mUser.getIsOnline()) {
            mTextAvailable.setVisibility(View.VISIBLE);
            mTextLastOnline.setText(R.string.online);
        } else {
            mTextAvailable.setVisibility(View.INVISIBLE);
            if(mUser.getLastOnline() != null)
                mTextLastOnline.setText(getString(R.string.text_last_online, DateUtils.getUpdatedAt(mUser.getLastOnline())));
            else
                mTextLastOnline.setText(getString(R.string.text_last_online_long_time_ago));
        }
    }

    private void choosePhoto() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this, perms)) {
            EasyImage.openChooserWithGallery(this, getString(R.string.text_choose_images), 0);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.image_permissions), REQUEST_IMAGE_PERMISSION, perms);
        }
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.action_exit, (dialog, which) -> {
                    FirebaseUtils.clearPushToken();
                    PreferenceHelper.newInstance(this).releaseData();
                    getDataManager().logout();
                    startActivity(LoginActivity.getLaunchIntent(this));
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void onPhotosReturned(final File imageFile) {
        CropImage.activity(Uri.fromFile(imageFile))
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(5, 4)
                .setFixAspectRatio(true)
                .start(this);
    }

    private UploadTask uploadFile(Uri file, String fileName, int quality) {
        Bitmap bitmapOriginal = BitmapFactory.decodeFile(file.getPath());//= imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapOriginal.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        // Meta data for imageView
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("UserName", mUser.getFullName())
                .build();
        // name of file in Storage
        return FirebaseStorage.getInstance()
                .getReference(DC.STORAGE_USERS)
                .child(fileName)
                .putBytes(bytes, metadata);
    }

    private void hideProgressBarUpload() {
        Toast.makeText(ProfileActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show();
        mProgressBar.hide();
    }

    private void showProgressBarUpload() {
        Toast.makeText(ProfileActivity.this, R.string.toast_data_started_updated, Toast.LENGTH_LONG).show();
        mProgressBar.show();
    }

    @Override
    public void onItemClicked(final int position) {
        startActivity(ProfileActivity.getLaunchIntent(this, mUsersAdapter.getItemAtPosition(position).getId()));
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        startActivity(SalaryActivity.getLaunchIntent(ProfileActivity.this, mUser));
    }
}