package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.common.UserType;
import com.ruslanlyalko.ll.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;

public class ContactEditActivity extends BaseActivity {

    //@BindView(R.id.tab_type_adult) TabItem mTabTypeAdult;
    @BindView(R.id.tabs_user_type) TabLayout mTabsUserType;
    @BindView(R.id.edit_name) EditText mEditName;
    @BindView(R.id.edit_phone1) EditText mEditPhone1;
    @BindView(R.id.edit_phone2) EditText mEditPhone2;
    @BindView(R.id.edit_description) EditText mEditDescription;
    @BindView(R.id.edit_email) EditText mEditEmail;
    @BindView(R.id.edit_birth_day) EditText mEditBirthDay;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private Contact mContact = new Contact();
    private String mClientName;
    private String mClientPhone;
    private boolean mNeedToSave = false;
    private boolean mIsNew = false;

    public static Intent getLaunchIntent(final Context launchIntent, final Contact contact) {
        Intent intent = new Intent(launchIntent, ContactEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contact);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, String name, String phone) {
        Intent intent = new Intent(launchIntent, ContactEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CLIENT_NAME, name);
        intent.putExtra(Keys.Extras.EXTRA_CLIENT_PHONE, phone);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_edit;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mContact = (Contact) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
            mClientPhone = bundle.getString(Keys.Extras.EXTRA_CLIENT_PHONE);
            mClientName = bundle.getString(Keys.Extras.EXTRA_CLIENT_NAME);
        }
        mIsNew = mContact == null;
        if (mIsNew) {
            mContact = new Contact();
            mContact.setName(mClientName);
            mContact.setPhone(mClientPhone);
        }
    }

    @Override
    protected void setupView() {
        setupChangeWatcher();
        setTitle(mIsNew ? R.string.title_activity_add : R.string.title_activity_edit);
        mEditName.setText(mContact.getName());
        mEditBirthDay.setText(DateUtils.toString(mContact.getBirthDay(), "dd.MM.yyyy"));
        mEditPhone1.setText(mContact.getPhone());
        mEditPhone2.setText(mContact.getPhone2());
        mEditEmail.setText(mContact.getEmail());
        mEditDescription.setText(mContact.getDescription());
        TabLayout.Tab tab = mTabsUserType.getTabAt(mContact.getUserType() == UserType.ADULT ? 0 : 1);
        if (tab != null) tab.select();
        mNeedToSave = false;
    }

    private void setupChangeWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNeedToSave = true;
            }
        };
        mEditName.addTextChangedListener(watcher);
        mEditPhone1.addTextChangedListener(watcher);
        mEditPhone2.addTextChangedListener(watcher);
        mEditEmail.addTextChangedListener(watcher);
        mEditDescription.addTextChangedListener(watcher);
        mTabsUserType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                mNeedToSave = true;
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (mIsNew)
                addClient();
            else
                updateClient();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ContactEditActivity.this);
            builder.setTitle(R.string.dialog_discart_changes)
                    .setPositiveButton(R.string.action_discard, (dialog, which) -> {
                        mNeedToSave = false;
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        } else {
            hideKeyboard();
            super.onBackPressed();
        }
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    private void updateModel() {
        mContact.setName(mEditName.getText().toString().trim());
        mContact.setEmail(mEditEmail.getText().toString().trim());
        mContact.setPhone(mEditPhone1.getText().toString().trim());
        mContact.setPhone2(mEditPhone2.getText().toString().trim());
        mContact.setDescription(mEditDescription.getText().toString().trim());
        mContact.setUserType(mTabsUserType.getTabAt(0).isSelected() ? UserType.ADULT : UserType.CHILD);
    }

    private void addClient() {
        updateModel();
        if (mContact.getName().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_no_name), Toast.LENGTH_LONG).show();
            return;
        }
        mIsNew = false;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_CONTACTS)
                .push();
        mContact.setKey(ref.getKey());
        ref.setValue(mContact).addOnCompleteListener(task -> {
            Snackbar.make(mEditName, getString(R.string.client_added), Snackbar.LENGTH_SHORT).show();
            mNeedToSave = false;
            onBackPressed();
        });
    }

    private void updateClient() {
        updateModel();
        if (mContact.getName().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_no_name), Toast.LENGTH_LONG).show();
            return;
        }
        database.getReference(DefaultConfigurations.DB_CONTACTS)
                .child(mContact.getKey())
                .setValue(mContact)
                .addOnCompleteListener(task -> {
                    Toast.makeText(ContactEditActivity.this, getString(R.string.mk_updated), Toast.LENGTH_SHORT).show();
                    mNeedToSave = false;
                    onBackPressed();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @OnClick(R.id.edit_birth_day)
    public void onDate1Clicked() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mContact.getBirthDay());
        DatePickerDialog dialog = DatePickerDialog.newInstance((datePicker, year, month, day)
                        -> {
                    mContact.setBirthDay(DateUtils.getDate(year, month, day));
                    mEditBirthDay.setText(DateUtils.toString(mContact.getBirthDay(), "dd.MM.yyyy"));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.showYearPickerFirst(true);
        dialog.show(getFragmentManager(), "birthday");
    }
}
