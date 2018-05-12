package com.ruslanlyalko.ll.presentation.ui.main.profile.salary_edit;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.SettingsSalary;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;

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
    private boolean mNeedToSave = false;
    private SettingsSalary mSettingsSalary = new SettingsSalary();

    public static Intent getLaunchIntent(final BaseActivity activity) {
        return new Intent(activity, SalaryEditActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_salary_edit;
    }

    @Override
    protected void setupView() {
        loadSalaries();
        updateUI();
    }

    @Override
    protected boolean isModalView() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveToDb();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SalaryEditActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_mk_edit_text)
                    .setPositiveButton(R.string.action_save_changes, (dialog, which) -> {
                        saveToDb();
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_no, (dialog, which) -> {
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
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    private void updateUI() {
        setTitle(R.string.title_activity_edit);
        //
        mEditPrivateStudent.setText(String.valueOf(mSettingsSalary.getStudentPrivate()));
        mEditPrivateStudent15.setText(String.valueOf(mSettingsSalary.getStudentPrivate15()));
        mEditPrivateTeacher.setText(String.valueOf(mSettingsSalary.getTeacherPrivate()));
        mEditPrivateTeacher15.setText(String.valueOf(mSettingsSalary.getTeacherPrivate15()));
        //
        mEditPairStudent.setText(String.valueOf(mSettingsSalary.getStudentPair()));
        mEditPairStudent15.setText(String.valueOf(mSettingsSalary.getStudentPair15()));
        mEditPairTeacher.setText(String.valueOf(mSettingsSalary.getTeacherPair()));
        mEditPairTeacher15.setText(String.valueOf(mSettingsSalary.getTeacherPair15()));
        //
        mEditGroupStudent.setText(String.valueOf(mSettingsSalary.getStudentGroup()));
        mEditGroupStudent15.setText(String.valueOf(mSettingsSalary.getStudentGroup15()));
        mEditGroupTeacher.setText(String.valueOf(mSettingsSalary.getTeacherGroup()));
        mEditGroupTeacher15.setText(String.valueOf(mSettingsSalary.getTeacherGroup15()));
        //
        mEditOnLineStudent.setText(String.valueOf(mSettingsSalary.getStudentOnLine()));
        mEditOnLineStudent15.setText(String.valueOf(mSettingsSalary.getStudentOnLine15()));
        mEditOnLineTeacher.setText(String.valueOf(mSettingsSalary.getTeacherOnLine()));
        mEditOnLineTeacher15.setText(String.valueOf(mSettingsSalary.getTeacherOnLine15()));
        mNeedToSave = false;
    }

    private void loadSalaries() {
        getDatabase().getReference(DC.DB_SETTINGS_SALARY).child("first_key")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        SettingsSalary settingsSalary = dataSnapshot.getValue(SettingsSalary.class);
                        if (settingsSalary != null) {
                            mSettingsSalary = settingsSalary;
                            updateUI();
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void saveToDb() {
        mSettingsSalary.setStudentPrivate(Integer.valueOf(mEditPrivateStudent.getText().toString()));
        mSettingsSalary.setTeacherPrivate(Integer.valueOf(mEditPrivateTeacher.getText().toString()));
        mSettingsSalary.setStudentPrivate15(Integer.valueOf(mEditPrivateStudent15.getText().toString()));
        mSettingsSalary.setTeacherPrivate15(Integer.valueOf(mEditPrivateTeacher15.getText().toString()));
        //
        mSettingsSalary.setStudentPair(Integer.valueOf(mEditPairStudent.getText().toString()));
        mSettingsSalary.setTeacherPair(Integer.valueOf(mEditPairTeacher.getText().toString()));
        mSettingsSalary.setStudentPair15(Integer.valueOf(mEditPairStudent15.getText().toString()));
        mSettingsSalary.setTeacherPair15(Integer.valueOf(mEditPairTeacher15.getText().toString()));
        //
        mSettingsSalary.setStudentGroup(Integer.valueOf(mEditGroupStudent.getText().toString()));
        mSettingsSalary.setTeacherGroup(Integer.valueOf(mEditGroupTeacher.getText().toString()));
        mSettingsSalary.setStudentGroup15(Integer.valueOf(mEditGroupStudent15.getText().toString()));
        mSettingsSalary.setTeacherGroup15(Integer.valueOf(mEditGroupTeacher15.getText().toString()));
        //
        mSettingsSalary.setStudentOnLine(Integer.valueOf(mEditOnLineStudent.getText().toString()));
        mSettingsSalary.setTeacherOnLine(Integer.valueOf(mEditOnLineTeacher.getText().toString()));
        mSettingsSalary.setStudentOnLine15(Integer.valueOf(mEditOnLineStudent15.getText().toString()));
        mSettingsSalary.setTeacherOnLine15(Integer.valueOf(mEditOnLineTeacher15.getText().toString()));
        if (!mSettingsSalary.hasKey()) {
            String key = getDatabase().getReference(DC.DB_SETTINGS_SALARY).push().getKey();
            mSettingsSalary.setKey(key);
        }
        getDatabase().getReference(DC.DB_SETTINGS_SALARY)
                .child(mSettingsSalary.getKey())
                .setValue(mSettingsSalary)
                .addOnCompleteListener(task -> {
                    Toast.makeText(SalaryEditActivity.this, getString(R.string.mk_updated), Toast.LENGTH_SHORT).show();
                    mNeedToSave = false;
                    onBackPressed();
                });
    }
}
