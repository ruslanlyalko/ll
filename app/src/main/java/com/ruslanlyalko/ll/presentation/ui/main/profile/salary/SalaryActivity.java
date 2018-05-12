package com.ruslanlyalko.ll.presentation.ui.main.profile.salary;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.common.ViewUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.data.models.SettingsSalary;
import com.ruslanlyalko.ll.data.models.User;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SalaryActivity extends BaseActivity {

    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.text_card) TextView mTextCard;
    @BindView(R.id.text_name) TextView mTextName;
    @BindView(R.id.text_salary_expand) TextView mTextExpand;
    @BindView(R.id.image_expand) ImageView mImageView;
    @BindView(R.id.text_total) TextView mTotalSwitcher;
    @BindView(R.id.text_salary_private_total) TextView mTextSalaryPrivateTotal;
    @BindView(R.id.text_salary_pair_total) TextView mTextSalaryPairTotal;
    @BindView(R.id.text_salary_group_total) TextView mTextSalaryGroupTotal;
    @BindView(R.id.text_salary_online_total) TextView mTextSalaryOnlineTotal;
    @BindView(R.id.text_salary_private) TextView mTextSalaryPrivate;
    @BindView(R.id.text_salary_pair) TextView mTextSalaryPair;
    @BindView(R.id.text_salary_group) TextView mTextSalaryGroup;
    @BindView(R.id.text_salary_online) TextView mTextSalaryOnline;
    @BindView(R.id.text_salary_private15) TextView mTextSalaryPrivate15;
    @BindView(R.id.text_salary_pair15) TextView mTextSalaryPair15;
    @BindView(R.id.text_salary_group15) TextView mTextSalaryGroup15;
    @BindView(R.id.text_salary_online15) TextView mTextSalaryOnline15;

    private SettingsSalary mSettingsSalary = new SettingsSalary();
    private List<Lesson> mLessons = new ArrayList<>();
    private Calendar mCurrentMonth = Calendar.getInstance();
    private User mUser;

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity, User user) {
        Intent intent = new Intent(launchActivity, SalaryActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_USER, user);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_salary;
    }

    @Override
    protected void parseExtras() {
        Bundle extras;
        if ((extras = getIntent().getExtras()) != null) {
            mUser = (User) extras.getSerializable(Keys.Extras.EXTRA_USER);
        }
    }

    @Override
    protected void setupView() {
        initUserData();
        updateMonth();
        updateConditionUI();
        loadSettingsSalaries();
        loadLessons();
    }

    private void initUserData() {
        mTextName.setText(mUser.getFullName());
        mTextCard.setText(mUser.getCard());
    }

    private void updateMonth() {
        String[] months = getResources().getStringArray(R.array.months);
        mTextMonth.setText(months[mCurrentMonth.get(Calendar.MONTH)]);
    }

    private void setSwitcherAnim(final boolean right) {
        Animation in;
        Animation out;
        if (right) {
            in = AnimationUtils.loadAnimation(this, R.anim.trans_right_in);
            out = AnimationUtils.loadAnimation(this, R.anim.trans_right_out);
        } else {
            in = AnimationUtils.loadAnimation(this, R.anim.trans_left_in);
            out = AnimationUtils.loadAnimation(this, R.anim.trans_left_out);
        }
//        mTotalSwitcher.setInAnimation(in);
//        mTotalSwitcher.setOutAnimation(out);
    }

    @OnClick(R.id.button_prev)
    void onPrevClicked() {
        setSwitcherAnim(true);
        mCurrentMonth.add(Calendar.MONTH, -1);
        updateMonth();
    }

    @OnClick(R.id.button_next)
    void onNextClicked() {
        setSwitcherAnim(false);
        mCurrentMonth.add(Calendar.MONTH, 1);
        updateMonth();
    }

    @OnClick(R.id.panel_copy)
    void onCardClicked() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(mTextCard.getText().toString(), mTextCard.getText().toString());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(SalaryActivity.this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.panel_action)
    void onExpandClicked() {
        if (mTextExpand.getVisibility() == View.VISIBLE) {
            mImageView.setImageResource(R.drawable.ic_action_expand_more);
            ViewUtils.collapse(mTextExpand);
        } else {
            ViewUtils.expand(mTextExpand);
            mImageView.setImageResource(R.drawable.ic_action_expand_less);
        }
    }

    private void loadSettingsSalaries() {
        getDatabase().getReference(DC.DB_SETTINGS_SALARY).child("first_key")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        SettingsSalary settingsSalary = dataSnapshot.getValue(SettingsSalary.class);
                        if (settingsSalary != null) {
                            mSettingsSalary = settingsSalary;
                            updateConditionUI();
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void updateConditionUI() {
        mTextSalaryPrivate.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherPrivate()));
        mTextSalaryPrivate15.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherPrivate15()));
        //
        mTextSalaryPair.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherPair()));
        mTextSalaryPair15.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherPair15()));
        //
        mTextSalaryGroup.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherGroup()));
        mTextSalaryGroup15.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherGroup15()));
        //
        mTextSalaryOnline.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherOnLine()));
        mTextSalaryOnline15.setText(String.format(getString(R.string.hrn_d), mSettingsSalary.getTeacherOnLine15()));
    }

    private void loadLessons() {
        getDatabase().getReference(DC.DB_LESSONS)
                .child(DateUtils.toString(mCurrentMonth.getTime(), "yyyy/MM"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mLessons.clear();
                        for (DataSnapshot month : dataSnapshot.getChildren()) {
                            for (DataSnapshot day : month.getChildren()) {
                                Lesson lesson = day.getValue(Lesson.class);
                                if (lesson != null && lesson.getUserId().equals(mUser.getId())) {
                                    mLessons.add(lesson);
                                }
                            }
                        }
                        calcSalary();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void calcSalary() {
        int total;
        int privateTotal = 0;
        int pairTotal = 0;
        int groupTotal = 0;
        int onlineTotal = 0;
        if (mLessons == null || mSettingsSalary == null) {
            Log.e("Salary", "Wrong argument!");
            return;
        }
        for (Lesson lesson : mLessons) {
            if (lesson.getLessonLengthId() == 0) {
                switch (lesson.getLessonType()) {
                    case 0:
                        privateTotal += mSettingsSalary.getTeacherPrivate();
                    case 1:
                        pairTotal += mSettingsSalary.getTeacherPair();
                    case 2:
                        groupTotal += mSettingsSalary.getTeacherGroup();
                    case 3:
                        onlineTotal += mSettingsSalary.getTeacherOnLine();
                }
            } else {
                switch (lesson.getLessonType()) {
                    case 0:
                        privateTotal += mSettingsSalary.getTeacherPrivate15();
                    case 1:
                        pairTotal += mSettingsSalary.getTeacherPair15();
                    case 2:
                        groupTotal += mSettingsSalary.getTeacherGroup15();
                    case 3:
                        onlineTotal += mSettingsSalary.getTeacherOnLine15();
                }
            }
        }
        total = privateTotal + pairTotal + groupTotal + onlineTotal;
        mTextSalaryPrivateTotal.setText(String.format(getString(R.string.hrn_d), privateTotal));
        mTextSalaryPairTotal.setText(String.format(getString(R.string.hrn_d), pairTotal));
        mTextSalaryGroupTotal.setText(String.format(getString(R.string.hrn_d), groupTotal));
        mTextSalaryOnlineTotal.setText(String.format(getString(R.string.hrn_d), onlineTotal));
        mTotalSwitcher.setText(String.format(getString(R.string.hrn_d), total));
    }
}
