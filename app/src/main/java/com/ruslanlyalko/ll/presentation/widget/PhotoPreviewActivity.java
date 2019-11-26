package com.ruslanlyalko.ll.presentation.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.utils.Keys;

import butterknife.BindView;

public class PhotoPreviewActivity extends BaseActivity {

    @BindView(R.id.photo_view) PhotoView mPhotoView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    private String mUri = "";
    private String mUserName = "";
    private String mFolder = "";
    private String mThumbnail = "";

    public static Intent getLaunchIntent(final Activity launchActivity, String uri, String userName) {
        Intent intent = new Intent(launchActivity, PhotoPreviewActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_URI, uri);
        intent.putExtra(Keys.Extras.EXTRA_USER_NAME, userName);
        return intent;
    }

    public static Intent getLaunchIntent(final Activity launchActivity, String uri, String userName, String thumbnail, boolean a) {
        Intent intent = new Intent(launchActivity, PhotoPreviewActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_URI, uri);
        intent.putExtra(Keys.Extras.EXTRA_USER_NAME, userName);
        intent.putExtra(Keys.Extras.EXTRA_THUMBNAIL, thumbnail);
        return intent;
    }

    public static Intent getLaunchIntent(final Activity launchActivity, final String uri, final String userName, final String storage) {
        Intent intent = new Intent(launchActivity, PhotoPreviewActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_URI, uri);
        intent.putExtra(Keys.Extras.EXTRA_USER_NAME, userName);
        intent.putExtra(Keys.Extras.EXTRA_FOLDER, storage);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_show_image;
    }

    @Override
    protected void parseExtras() {
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUri = bundle.getString(Keys.Extras.EXTRA_URI);
            mThumbnail = bundle.getString(Keys.Extras.EXTRA_THUMBNAIL);
            mUserName = bundle.getString(Keys.Extras.EXTRA_USER_NAME);
            mFolder = bundle.getString(Keys.Extras.EXTRA_FOLDER, DC.STORAGE_EXPENSES);
        }
    }

    @Override
    protected void setupView() {
        loadWithGlide(mUri, mThumbnail);
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing, R.anim.fadeout);
    }

    private void loadWithGlide(String uri, final String thumbnail) {
        if (uri == null || uri.isEmpty()) return;
        setTitle(getString(R.string.text_loading));
        if (uri.contains("http")) {
            Glide.with(PhotoPreviewActivity.this)
                    .load(uri)
                    .thumbnail(Glide.with(this).load(thumbnail))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable final GlideException e, final Object model, final Target<Drawable> target, final boolean isFirstResource) {
                            setTitle(R.string.error_loading);
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(final Drawable resource, final Object model, final Target<Drawable> target, final DataSource dataSource, final boolean isFirstResource) {
                            setTitle(getString(R.string.text_author) + mUserName);
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(mPhotoView);
        } else {
            StorageReference ref = FirebaseStorage.getInstance().getReference(mFolder).child(uri);
            //load mPhotoView using Glide
            ref.getDownloadUrl().addOnSuccessListener(uri1 -> Glide.with(
                    PhotoPreviewActivity.this).load(uri1).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable final GlideException e, final Object model, final Target<Drawable> target, final boolean isFirstResource) {
                    setTitle(R.string.error_loading);
                    mProgressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(final Drawable resource, final Object model, final Target<Drawable> target, final DataSource dataSource, final boolean isFirstResource) {
                    setTitle(getString(R.string.text_author) + mUserName);
                    mProgressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(mPhotoView));
        }
    }
}
