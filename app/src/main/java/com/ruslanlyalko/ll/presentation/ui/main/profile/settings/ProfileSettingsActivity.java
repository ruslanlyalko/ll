package com.ruslanlyalko.ll.presentation.ui.main.profile.settings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.ll.data.models.User;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.EasyImage;

public class ProfileSettingsActivity extends BaseActivity {

    @BindView(R.id.text_email) EditText inputEmail;
    @BindView(R.id.text_phone) EditText inputPhone;
    @BindView(R.id.text_bday) EditText inputBDay;
    @BindView(R.id.text_card) EditText inputCard;
    @BindView(R.id.text_first_date) EditText inputFirstDate;
    @BindView(R.id.text_password1) EditText inputPassword1;
    @BindView(R.id.text_password2) EditText inputPassword2;
    @BindView(R.id.text_name) TextView textName;
    @BindView(R.id.panel_first_date) LinearLayout panelFirstDate;
    @BindView(R.id.panel_password) LinearLayout panelPassword;
    @BindView(R.id.button_change_password) Button buttonChangePassword;
    @BindView(R.id.switch_receive_notifications) Switch mSwitchReceiveNotifications;
    @BindView(R.id.switch_show_clients) Switch mSwitchShowClients;
    @BindView(R.id.panel_receive_notifications) LinearLayout mPanelReceiveNotifications;
    @BindView(R.id.panel_show_clients) LinearLayout mPanelShowCLients;

    private Calendar mBirthDay = Calendar.getInstance();
    private Calendar mFirstDate = Calendar.getInstance();

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String mNumber = "";
    private String mUid;
    private boolean isCurrentUser;


    public static Intent getLaunchIntent(final Activity launchIntent, final String uId) {
        Intent intent = new Intent(launchIntent, ProfileSettingsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_UID, uId);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile_settings;
    }

    @Override
    public void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUid = bundle.getString(Keys.Extras.EXTRA_UID, mCurrentUser.getUid());
        }
    }

    @Override
    protected void setupView() {
        isCurrentUser = mUid.equals(mCurrentUser.getUid());
        // user can change only they own emails
        inputEmail.setEnabled(isCurrentUser);
        inputPassword1.setEnabled(isCurrentUser);
        inputPassword2.setEnabled(isCurrentUser);
        panelPassword.setEnabled(isCurrentUser);
        buttonChangePassword.setEnabled(isCurrentUser);
        panelFirstDate.setVisibility(FirebaseUtils.isAdmin() && !isCurrentUser ? View.VISIBLE : View.GONE);
        mPanelReceiveNotifications.setVisibility(FirebaseUtils.isAdmin() ? View.VISIBLE : View.GONE);
        mPanelShowCLients.setVisibility(FirebaseUtils.isAdmin() && !isCurrentUser ? View.VISIBLE : View.GONE);
        inputCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mNumber.length() < s.length()) {
                    switch (s.length()) {
                        case 5:
                            s.insert(4, " ");
                            break;
                        case 10:
                            s.insert(9, " ");
                            break;
                        case 15:
                            s.insert(14, " ");
                            break;
                    }
                }
                mNumber = s.toString();
            }
        });
        initCurrentUserData();
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        final String phone = inputPhone.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        final String birthday = inputBDay.getText().toString().trim();
        final String card = inputCard.getText().toString().trim();
        final String firstDate = inputFirstDate.getText().toString().trim();
        final String tPhone = inputPhone.getTag().toString().trim();
        final String tEmail = inputEmail.getTag().toString().trim();
        final String tBirthday = inputBDay.getTag().toString().trim();
        final String tCard = inputCard.getTag().toString().trim();
        final String tFirstDate = inputFirstDate.getTag().toString().trim();
        final boolean receiveNotifications = mSwitchReceiveNotifications.isChecked();
        final boolean tReceiveNotifications = (boolean) mSwitchReceiveNotifications.getTag();
        final boolean showClients = mSwitchShowClients.isChecked();
        final boolean tShowClients = (boolean) mSwitchShowClients.getTag();
        Map<String, Object> childUpdates = new HashMap<>();
        boolean needUpdate = false;
        if (!phone.equals(tPhone)) {
            childUpdates.put("phone", phone);
            needUpdate = true;
        }
        if (!birthday.equals(tBirthday)) {
            childUpdates.put("birthdayDate", birthday);
            needUpdate = true;
        }
        if (!card.equals(tCard)) {
            childUpdates.put("card", card);
            needUpdate = true;
        }
        if (!email.equals(tEmail)) {
            childUpdates.put("email", email);
            mCurrentUser.updateEmail(email);
            needUpdate = true;
        }
        if (!firstDate.equals(tFirstDate)) {
            childUpdates.put("workingFirstDate", firstDate);
            needUpdate = true;
        }
        if (receiveNotifications != tReceiveNotifications) {
            childUpdates.put("isReceiveNotifications", receiveNotifications);
            needUpdate = true;
        }
        if (showClients != tShowClients) {
            childUpdates.put("isAllowViewExpenses", showClients);
            needUpdate = true;
        }
        if (needUpdate) {
            if (isCurrentUser && !textName.getText().toString().isEmpty() && !textName.getText().toString().equals(mCurrentUser.getDisplayName())) {
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setDisplayName(textName.getText().toString())
                        .build();
                mCurrentUser.updateProfile(profileUpdate);
            }
            mDatabase.getReference(DefaultConfigurations.DB_USERS).child(mUid).updateChildren(childUpdates).addOnCompleteListener(task -> {
                Toast.makeText(ProfileSettingsActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show();
                onBackPressed();
            });
        } else
            Toast.makeText(ProfileSettingsActivity.this, R.string.toast_nothing_to_change, Toast.LENGTH_SHORT).show();
    }

    private void initCurrentUserData() {
        DatabaseReference ref = mDatabase.getReference(DefaultConfigurations.DB_USERS).child(mUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) return;
                textName.setText(user.getFullName());
                inputPhone.setText(user.getPhone());
                inputPhone.setTag(user.getPhone());
                inputEmail.setText(user.getEmail());
                inputEmail.setTag(user.getEmail());
                inputBDay.setText(user.getBirthdayDate());
                inputBDay.setTag(user.getBirthdayDate());
                inputCard.setText(user.getCard());
                inputCard.setTag(user.getCard());
                inputFirstDate.setText(user.getWorkingFromDate());
                inputFirstDate.setTag(user.getWorkingFromDate());
                mSwitchReceiveNotifications.setChecked(user.getIsReceiveNotifications());
                mSwitchReceiveNotifications.setTag(user.getIsReceiveNotifications());
                mSwitchShowClients.setChecked(user.getIsAllowViewExpenses());
                mSwitchShowClients.setTag(user.getIsAllowViewExpenses());
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                Date dt = new Date();
                try {
                    dt = sdf.parse(user.getBirthdayDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mBirthDay.setTime(dt);
                try {
                    dt = sdf.parse(user.getWorkingFromDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mFirstDate.setTime(dt);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    @OnClick(R.id.button_change_password)
    void onChangePasswordClicked() {
        final String password1 = inputPassword1.getText().toString().trim();
        final String password2 = inputPassword2.getText().toString().trim();
        inputPassword1.setError(null);
        inputPassword2.setError(null);
        if (password1.length() <= 0) {
            return;
        }
        if (password1.length() < 6) {
            inputPassword1.setError(getString(R.string.toast_minimum_password));
            inputPassword1.requestFocus();
            return;
        }
        if (!password1.equals(password2)) {
            inputPassword2.setError(getString(R.string.toast_different_password));
            inputPassword2.requestFocus();
            return;
        }
        mCurrentUser.updatePassword(password1);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("password", password1);
        mDatabase.getReference(DefaultConfigurations.DB_USERS).child(mUid).updateChildren(childUpdates).addOnCompleteListener(task ->
                Toast.makeText(ProfileSettingsActivity.this, R.string.toast_data_updated, Toast.LENGTH_SHORT).show());
    }

    @OnClick(R.id.text_bday)
    void onBdayClicked() {
        new DatePickerDialog(ProfileSettingsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            mBirthDay.set(Calendar.YEAR, year);
            mBirthDay.set(Calendar.MONTH, monthOfYear);
            mBirthDay.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            inputBDay.setText(sdf.format(mBirthDay.getTime()));
        },
                mBirthDay.get(Calendar.YEAR), mBirthDay.get(Calendar.MONTH),
                mBirthDay.get(Calendar.DAY_OF_MONTH)).show();
    }

    @OnClick(R.id.text_first_date)
    void onFirstDateClicked() {
        new DatePickerDialog(ProfileSettingsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            mFirstDate.set(Calendar.YEAR, year);
            mFirstDate.set(Calendar.MONTH, monthOfYear);
            mFirstDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            inputFirstDate.setText(sdf.format(mFirstDate.getTime()));
        },
                mFirstDate.get(Calendar.YEAR), mFirstDate.get(Calendar.MONTH),
                mFirstDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }
}