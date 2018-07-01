package com.ruslanlyalko.ll.presentation.ui.main.profile.reminder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Reminder;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;

import butterknife.BindView;

public class ReminderActivity extends BaseActivity {

    @BindView(R.id.edit_before_reminder) EditText mEditBeforeReminder;
    @BindView(R.id.edit_after_reminder) EditText mEditAfterReminder;
    private Reminder mReminders = new Reminder();
    private boolean mIsEdited = false;

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, ReminderActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_reminder;
    }

    @Override
    protected void setupView() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mIsEdited = true;
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        };
        mEditBeforeReminder.addTextChangedListener(watcher);
        mEditAfterReminder.addTextChangedListener(watcher);
        getDB(DC.DB_REMINDERS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mReminders = dataSnapshot.getValue(Reminder.class);
                        updateUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            hideKeyboard();
            save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mIsEdited) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReminderActivity.this);
            builder.setTitle(R.string.dialog_discard_changes)
                    .setPositiveButton(R.string.action_discard, (dialog, which) -> {
                        mIsEdited = false;
                        super.onBackPressed();
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        } else {
            hideKeyboard();
            super.onBackPressed();
        }
    }

    private void save() {
        mReminders.setBeforeLessonReminder(mEditBeforeReminder.getText().toString().trim());
        mReminders.setAfterLessonReminder(mEditAfterReminder.getText().toString().trim());
        getDB(DC.DB_REMINDERS).setValue(mReminders).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            mIsEdited = false;
            onBackPressed();
        });
    }

    private void updateUI() {
        if (isDestroyed()) return;
        mEditBeforeReminder.setText(mReminders.getBeforeLessonReminder());
        mEditAfterReminder.setText(mReminders.getAfterLessonReminder());
        mIsEdited = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }
}
