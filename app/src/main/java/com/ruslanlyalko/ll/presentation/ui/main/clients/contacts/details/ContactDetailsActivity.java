package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.LessonsAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.OnLessonClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.edit.ContactEditActivity;
import com.ruslanlyalko.ll.presentation.ui.main.lesson.LessonActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ContactDetailsActivity extends BaseActivity implements OnLessonClickListener {

    private static final int RC_LESSON = 1001;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.image_avatar) ImageView mImageAvatar;
    @BindView(R.id.text_sub_title) TextView mTextSubTitle;
    @BindView(R.id.text_phone1) TextView mTextPhone1;
    @BindView(R.id.text_phone2) TextView mTextPhone2;
    @BindView(R.id.card_phone2) CardView mCardPhone2;
    @BindView(R.id.text_description) TextView mTextDescription;
    @BindView(R.id.button_add_birthday) CardView mButtonAddBirthday;
    @BindView(R.id.list_birthdays) RecyclerView mListBirthdays;
    @BindView(R.id.text_user_name) TextView mTextUserName;
    private Contact mContact;
    private String mContactKey = "";
    private LessonsAdapter mLessonsAdapter = new LessonsAdapter(this);
    private ValueEventListener mValueEventListener;
    private boolean mHasLessonsWithOtherTeachers;

    public static Intent getLaunchIntent(final Context launchIntent, final Contact contact) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contact);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, final String contactKey) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CONTACT_KEY, contactKey);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_details;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mContact = (Contact) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            mContactKey = bundle.getString(Keys.Extras.EXTRA_CONTACT_KEY);
        }
        if (mContact != null)
            mContactKey = mContact.getKey();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void setupView() {
        if (isDestroyed()) return;
        setupToolbar();
        setupRecycler();
        loadDetails();
        showContactDetails();
        loadContacts();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                startActivity(ContactEditActivity.getLaunchIntent(this, mContact));
                break;
            case R.id.action_delete:
                removeCurrentContact();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(this::loadLessons, 300);
    }

    @Override
    protected void onPause() {
        getDatabase().getReference(DC.DB_LESSONS).removeEventListener(mValueEventListener);
        super.onPause();
    }

    private void removeCurrentContact() {
        if (mLessonsAdapter.getItemCount() != 0) {
            Toast.makeText(this, R.string.error_delete_contact, Toast.LENGTH_LONG).show();
            return;
        }
        if (mHasLessonsWithOtherTeachers) {
            Toast.makeText(this, R.string.error_delete_contact_other, Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_remove_contact_title)
                .setMessage(R.string.dialog_remove_contact_message)
                .setPositiveButton("Видалити", (dialog, which) -> {
                    finish();
                    FirebaseDatabase.getInstance()
                            .getReference(DC.DB_CONTACTS)
                            .child(mContact.getKey()).removeValue();
                })
                .setNegativeButton("Повернутись", null)
                .show();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecycler() {
        mListBirthdays.setLayoutManager(new LinearLayoutManager(this));
        mListBirthdays.setAdapter(mLessonsAdapter);
    }

    private void loadDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DC.DB_CONTACTS)
                .child(mContactKey);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mContact = dataSnapshot.getValue(Contact.class);
                showContactDetails();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    private void showContactDetails() {
        if (isDestroyed()) return;
        if (mContact == null) {
            setTitle("");
            return;
        }
        setTitle("");
        mTextUserName.setText(mContact.getName());
        String subtitle = mContact.getEmail() + DateUtils.toString(mContact.getBirthDay(), "   dd.MM.yyyy");
        mTextSubTitle.setText(subtitle);
        mTextPhone1.setText(mContact.getPhone());
        mTextPhone2.setText(mContact.getPhone2());
        mCardPhone2.setVisibility(mContact.getPhone2() != null & !mContact.getPhone2().isEmpty() ? View.VISIBLE : View.GONE);
        mTextDescription.setText(mContact.getDescription());
        mTextDescription.setVisibility(mContact.getDescription() != null & !mContact.getDescription().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadLessons() {
        if (isDestroyed()) return;
        mValueEventListener = getDatabase().getReference(DC.DB_LESSONS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (isDestroyed()) return;
                        mHasLessonsWithOtherTeachers = false;
                        List<Lesson> lessons = new ArrayList<>();
                        for (DataSnapshot datYears : dataSnapshot.getChildren()) {
                            for (DataSnapshot datYear : datYears.getChildren()) {
                                for (DataSnapshot datMonth : datYear.getChildren()) {
                                    for (DataSnapshot datDay : datMonth.getChildren()) {
                                        Lesson lesson = datDay.getValue(Lesson.class);
                                        if (lesson != null && (lesson.getClients().contains(mContact.getKey()))) {
                                            if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                                                lessons.add(lesson);
                                            else
                                                mHasLessonsWithOtherTeachers = true;
                                        }
                                    }
                                }
                            }
                        }
                        Collections.sort(lessons, (o1, o2) ->
                                Long.compare(o2.getDateTime().getTime(), o1.getDateTime().getTime()));
                        mLessonsAdapter.setData(lessons);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void loadContacts() {
        Query ref = FirebaseDatabase.getInstance()
                .getReference(DC.DB_CONTACTS)
                .orderByChild("name");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot clientSS : dataSnapshot.getChildren()) {
                    Contact contact = clientSS.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }
                mLessonsAdapter.setContacts(contacts);
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(FirebaseUtils.isAdmin());
        menu.findItem(R.id.action_edit).setVisible(true);
        return true;
    }

    @OnClick(R.id.button_add_birthday)
    public void onViewClicked() {
//        startActivity(BirthdaysEditActivity.getLaunchIntent(this, mContact.getKey(), mContact.getChildName1()));
    }

    @OnClick({R.id.card_phone1, R.id.card_phone2})
    public void onViewClicked(View view) {
        Intent callIntent;
        switch (view.getId()) {
            case R.id.card_phone1:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone()));
                startActivity(callIntent);
                break;
            case R.id.card_phone2:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone2()));
                startActivity(callIntent);
                break;
        }
    }

    @Override
    public void onCommentClicked(final Lesson lesson) {
        if (lesson.hasDescription())
            Toast.makeText(this, lesson.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditClicked(final Lesson lesson) {
        startActivityForResult(LessonActivity.getLaunchIntent(this, lesson), RC_LESSON);
    }

    @Override
    public void onRemoveClicked(final Lesson lesson) {
        if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ContactDetailsActivity.this);
            builder.setTitle(R.string.dialog_calendar_remove_title)
                    .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                        removeLesson(lesson);
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        } else {
            Toast.makeText(this, R.string.toast_lesson_remove_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void removeLesson(final Lesson lesson) {
        getDatabase()
                .getReference(DC.DB_LESSONS)
                .child(DateUtils.toString(lesson.getDateTime(), "yyyy/MM/dd"))
                .child(lesson.getKey()).removeValue().addOnSuccessListener(aVoid -> {
            loadLessons();
            Toast.makeText(this, R.string.toast_lesson_removed, Toast.LENGTH_LONG).show();
        });
    }
}
