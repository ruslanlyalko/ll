package com.ruslanlyalko.ll.presentation.ui.main.profile.salary_edit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.SettingsSalary;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

public class SalaryEditActivity extends BaseActivity {

    @BindView(R.id.edit_private_student) EditText mEditPrivateStudent;
    @BindView(R.id.edit_private_teacher) EditText mEditPrivateTeacher;
    @BindView(R.id.edit_private_student_15) EditText mEditPrivateStudent15;
    @BindView(R.id.edit_private_teacher_15) EditText mEditPrivateTeacher15;
    @BindView(R.id.edit_pair_student) EditText mEditPairStudent;
    @BindView(R.id.edit_pair_teacher) EditText mEditPairTeacher;
    @BindView(R.id.edit_pair_student_15) EditText mEditPairStudent15;
    @BindView(R.id.edit_pair_teacher_15) EditText mEditPairTeacher15;
    @BindView(R.id.edit_group_student) EditText mEditGroupStudent;
    @BindView(R.id.edit_group_teacher) EditText mEditGroupTeacher;
    @BindView(R.id.edit_group_student_15) EditText mEditGroupStudent15;
    @BindView(R.id.edit_group_teacher_15) EditText mEditGroupTeacher15;
    @BindView(R.id.edit_online_student) EditText mEditOnLineStudent;
    @BindView(R.id.edit_online_teacher) EditText mEditOnLineTeacher;
    @BindView(R.id.edit_online_student_15) EditText mEditOnLineStudent15;
    @BindView(R.id.edit_online_teacher_15) EditText mEditOnLineTeacher15;

    @BindView(R.id.edit_private_student_child) EditText mEditPrivateStudentChild;
    @BindView(R.id.edit_private_teacher_child) EditText mEditPrivateTeacherChild;
    @BindView(R.id.edit_private_student_15_child) EditText mEditPrivateStudent15Child;
    @BindView(R.id.edit_private_teacher_15_child) EditText mEditPrivateTeacher15Child;
    @BindView(R.id.edit_pair_student_child) EditText mEditPairStudentChild;
    @BindView(R.id.edit_pair_teacher_child) EditText mEditPairTeacherChild;
    @BindView(R.id.edit_pair_student_15_child) EditText mEditPairStudent15Child;
    @BindView(R.id.edit_pair_teacher_15_child) EditText mEditPairTeacher15Child;
    @BindView(R.id.edit_group_student_child) EditText mEditGroupStudentChild;
    @BindView(R.id.edit_group_teacher_child) EditText mEditGroupTeacherChild;
    @BindView(R.id.edit_group_student_15_child) EditText mEditGroupStudent15Child;
    @BindView(R.id.edit_group_teacher_15_child) EditText mEditGroupTeacher15Child;
    @BindView(R.id.edit_online_student_child) EditText mEditOnLineStudentChild;
    @BindView(R.id.edit_online_teacher_child) EditText mEditOnLineTeacherChild;
    @BindView(R.id.edit_online_student_15_child) EditText mEditOnLineStudent15Child;
    @BindView(R.id.edit_online_teacher_15_child) EditText mEditOnLineTeacher15Child;
    @BindView(R.id.tabs_tariffs) TabLayout mTabsTariffs;

    private boolean mNeedToSave = false;
    private List<SettingsSalary> mSettingsSalary = new ArrayList<>();
    private int mLastSelectedIndex = 0;

    public static Intent getLaunchIntent(final BaseActivity activity) {
        return new Intent(activity, SalaryEditActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_salary_edit;
    }

    @Override
    protected void setupView() {
        mTabsTariffs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                updateUI();
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {
            }
        });
        loadSalaries();
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            save();
        }
        if (id == R.id.action_duplicate) {
            duplicate();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SalaryEditActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_mk_edit_text)
                    .setPositiveButton(R.string.action_save, (dialog, which) -> {
                        save();
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                        mNeedToSave = false;
                        onBackPressed();
                    })
                    .show();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.nothing, R.anim.fadeout);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_salary_edit, menu);
        return true;
    }

    private void updateUI() {
        setTitle(R.string.title_activity_edit);
        SettingsSalary currentSettings = mSettingsSalary.get(mTabsTariffs.getSelectedTabPosition());
        //
        mEditPrivateStudent.setText(String.valueOf(currentSettings.getStudentPrivate()));
        mEditPrivateStudent15.setText(String.valueOf(currentSettings.getStudentPrivate15()));
        mEditPrivateTeacher.setText(String.valueOf(currentSettings.getTeacherPrivate()));
        mEditPrivateTeacher15.setText(String.valueOf(currentSettings.getTeacherPrivate15()));
        //
        mEditPairStudent.setText(String.valueOf(currentSettings.getStudentPair()));
        mEditPairStudent15.setText(String.valueOf(currentSettings.getStudentPair15()));
        mEditPairTeacher.setText(String.valueOf(currentSettings.getTeacherPair()));
        mEditPairTeacher15.setText(String.valueOf(currentSettings.getTeacherPair15()));
        //
        mEditGroupStudent.setText(String.valueOf(currentSettings.getStudentGroup()));
        mEditGroupStudent15.setText(String.valueOf(currentSettings.getStudentGroup15()));
        mEditGroupTeacher.setText(String.valueOf(currentSettings.getTeacherGroup()));
        mEditGroupTeacher15.setText(String.valueOf(currentSettings.getTeacherGroup15()));
        //
        mEditOnLineStudent.setText(String.valueOf(currentSettings.getStudentOnLine()));
        mEditOnLineStudent15.setText(String.valueOf(currentSettings.getStudentOnLine15()));
        mEditOnLineTeacher.setText(String.valueOf(currentSettings.getTeacherOnLine()));
        mEditOnLineTeacher15.setText(String.valueOf(currentSettings.getTeacherOnLine15()));
        // ---child
        mEditPrivateStudentChild.setText(String.valueOf(currentSettings.getStudentPrivateChild()));
        mEditPrivateStudent15Child.setText(String.valueOf(currentSettings.getStudentPrivate15Child()));
        mEditPrivateTeacherChild.setText(String.valueOf(currentSettings.getTeacherPrivateChild()));
        mEditPrivateTeacher15Child.setText(String.valueOf(currentSettings.getTeacherPrivate15Child()));
        //
        mEditPairStudentChild.setText(String.valueOf(currentSettings.getStudentPairChild()));
        mEditPairStudent15Child.setText(String.valueOf(currentSettings.getStudentPair15Child()));
        mEditPairTeacherChild.setText(String.valueOf(currentSettings.getTeacherPairChild()));
        mEditPairTeacher15Child.setText(String.valueOf(currentSettings.getTeacherPair15Child()));
        //
        mEditGroupStudentChild.setText(String.valueOf(currentSettings.getStudentGroupChild()));
        mEditGroupStudent15Child.setText(String.valueOf(currentSettings.getStudentGroup15Child()));
        mEditGroupTeacherChild.setText(String.valueOf(currentSettings.getTeacherGroupChild()));
        mEditGroupTeacher15Child.setText(String.valueOf(currentSettings.getTeacherGroup15Child()));
        //
        mEditOnLineStudentChild.setText(String.valueOf(currentSettings.getStudentOnLineChild()));
        mEditOnLineStudent15Child.setText(String.valueOf(currentSettings.getStudentOnLine15Child()));
        mEditOnLineTeacherChild.setText(String.valueOf(currentSettings.getTeacherOnLineChild()));
        mEditOnLineTeacher15Child.setText(String.valueOf(currentSettings.getTeacherOnLine15Child()));
        mNeedToSave = false;
    }

    private void duplicate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getMaxDate());
        DatePickerDialog dialog = new DatePickerDialog(SalaryEditActivity.this, (datePicker, year, month, day)
                -> {
            SettingsSalary currentSettings = mSettingsSalary.get(mTabsTariffs.getSelectedTabPosition());
            currentSettings.setDateFrom(DateUtils.getDate(year, month, day));
            currentSettings.setKey(null);
            saveToDB(currentSettings);
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(calendar.getTime().getTime());
        dialog.show();
    }

    private Date getMaxDate() {
        Calendar calendar = Calendar.getInstance();
        for (SettingsSalary settingsSalary : mSettingsSalary) {
            if (settingsSalary.getDateFrom().after(calendar.getTime())) {
                calendar.setTime(settingsSalary.getDateFrom());
            }
        }
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private void loadSalaries() {
        getDB(DC.DB_SETTINGS_SALARY)
                .orderByChild("dateFrom/time")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (isDestroyed()) {
                            return;
                        }
                        if (mTabsTariffs.getTabCount() > 0) {
                            mLastSelectedIndex = mTabsTariffs.getSelectedTabPosition();
                        }
                        mSettingsSalary.clear();
                        mTabsTariffs.removeAllTabs();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            SettingsSalary settingsSalary = data.getValue(SettingsSalary.class);
                            if (settingsSalary != null) {
                                mSettingsSalary.add(settingsSalary);
                                mTabsTariffs.addTab(mTabsTariffs.newTab().setText(DateUtils.toString(settingsSalary.getDateFrom(), "MM yyyy")));
                            }
                        }
                        if (mLastSelectedIndex < mTabsTariffs.getTabCount()) {
                            mTabsTariffs.getTabAt(mLastSelectedIndex).select();
                        }
                        updateUI();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void save() {
        SettingsSalary currentSettings = mSettingsSalary.get(mTabsTariffs.getSelectedTabPosition());
        try {
            currentSettings.setStudentPrivate(Integer.valueOf(mEditPrivateStudent.getText().toString()));
            currentSettings.setTeacherPrivate(Integer.valueOf(mEditPrivateTeacher.getText().toString()));
            currentSettings.setStudentPrivate15(Integer.valueOf(mEditPrivateStudent15.getText().toString()));
            currentSettings.setTeacherPrivate15(Integer.valueOf(mEditPrivateTeacher15.getText().toString()));
            //
            currentSettings.setStudentPair(Integer.valueOf(mEditPairStudent.getText().toString()));
            currentSettings.setTeacherPair(Integer.valueOf(mEditPairTeacher.getText().toString()));
            currentSettings.setStudentPair15(Integer.valueOf(mEditPairStudent15.getText().toString()));
            currentSettings.setTeacherPair15(Integer.valueOf(mEditPairTeacher15.getText().toString()));
            //
            currentSettings.setStudentGroup(Integer.valueOf(mEditGroupStudent.getText().toString()));
            currentSettings.setTeacherGroup(Integer.valueOf(mEditGroupTeacher.getText().toString()));
            currentSettings.setStudentGroup15(Integer.valueOf(mEditGroupStudent15.getText().toString()));
            currentSettings.setTeacherGroup15(Integer.valueOf(mEditGroupTeacher15.getText().toString()));
            //
            currentSettings.setStudentOnLine(Integer.valueOf(mEditOnLineStudent.getText().toString()));
            currentSettings.setTeacherOnLine(Integer.valueOf(mEditOnLineTeacher.getText().toString()));
            currentSettings.setStudentOnLine15(Integer.valueOf(mEditOnLineStudent15.getText().toString()));
            currentSettings.setTeacherOnLine15(Integer.valueOf(mEditOnLineTeacher15.getText().toString()));
            //****
            currentSettings.setStudentPrivateChild(Integer.valueOf(mEditPrivateStudentChild.getText().toString()));
            currentSettings.setTeacherPrivateChild(Integer.valueOf(mEditPrivateTeacherChild.getText().toString()));
            currentSettings.setStudentPrivate15Child(Integer.valueOf(mEditPrivateStudent15Child.getText().toString()));
            currentSettings.setTeacherPrivate15Child(Integer.valueOf(mEditPrivateTeacher15Child.getText().toString()));
            //
            currentSettings.setStudentPairChild(Integer.valueOf(mEditPairStudentChild.getText().toString()));
            currentSettings.setTeacherPairChild(Integer.valueOf(mEditPairTeacherChild.getText().toString()));
            currentSettings.setStudentPair15Child(Integer.valueOf(mEditPairStudent15Child.getText().toString()));
            currentSettings.setTeacherPair15Child(Integer.valueOf(mEditPairTeacher15Child.getText().toString()));
            //
            currentSettings.setStudentGroupChild(Integer.valueOf(mEditGroupStudentChild.getText().toString()));
            currentSettings.setTeacherGroupChild(Integer.valueOf(mEditGroupTeacherChild.getText().toString()));
            currentSettings.setStudentGroup15Child(Integer.valueOf(mEditGroupStudent15Child.getText().toString()));
            currentSettings.setTeacherGroup15Child(Integer.valueOf(mEditGroupTeacher15Child.getText().toString()));
            //
            currentSettings.setStudentOnLineChild(Integer.valueOf(mEditOnLineStudentChild.getText().toString()));
            currentSettings.setTeacherOnLineChild(Integer.valueOf(mEditOnLineTeacherChild.getText().toString()));
            currentSettings.setStudentOnLine15Child(Integer.valueOf(mEditOnLineStudent15Child.getText().toString()));
            currentSettings.setTeacherOnLine15Child(Integer.valueOf(mEditOnLineTeacher15Child.getText().toString()));
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_empty_error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
        saveToDB(currentSettings);
    }

    private void saveToDB(SettingsSalary settingsSalary) {
        if (!settingsSalary.hasKey()) {
            String key = getDB(DC.DB_SETTINGS_SALARY).push().getKey();
            settingsSalary.setKey(key);
        }
        getDB(DC.DB_SETTINGS_SALARY)
                .child(settingsSalary.getKey())
                .setValue(settingsSalary)
                .addOnCompleteListener(task -> {
                    Toast.makeText(SalaryEditActivity.this, getString(R.string.toast_updated), Toast.LENGTH_SHORT).show();
                    mNeedToSave = false;
                });
    }
}
