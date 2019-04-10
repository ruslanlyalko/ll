package com.ruslanlyalko.ll.presentation.ui.main.profile.salary;

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
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.data.models.SettingsSalary;
import com.ruslanlyalko.ll.data.models.User;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;
import com.ruslanlyalko.ll.presentation.utils.Keys;
import com.ruslanlyalko.ll.presentation.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    @BindView(R.id.text_salary_private_child) TextView mTextSalaryPrivateChild;
    @BindView(R.id.text_salary_pair_child) TextView mTextSalaryPairChild;
    @BindView(R.id.text_salary_group_child) TextView mTextSalaryGroupChild;
    @BindView(R.id.text_salary_online_child) TextView mTextSalaryOnlineChild;
    @BindView(R.id.text_salary_private15_child) TextView mTextSalaryPrivate15Child;
    @BindView(R.id.text_salary_pair15_child) TextView mTextSalaryPair15Child;
    @BindView(R.id.text_salary_group15_child) TextView mTextSalaryGroup15Child;
    @BindView(R.id.text_salary_online15_child) TextView mTextSalaryOnline15Child;

    private List<SettingsSalary> mSettingsSalary = new ArrayList<>();
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
        updateConditionUI();
        mTextMonth.setText(DateUtils.getMonthWithYear(getResources(), mCurrentMonth));
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
        loadLessons();
    }

    @OnClick(R.id.button_next)
    void onNextClicked() {
        setSwitcherAnim(false);
        mCurrentMonth.add(Calendar.MONTH, 1);
        updateMonth();
        loadLessons();
    }

    @OnClick(R.id.panel_copy)
    void onCardClicked() {
        String text = mTextCard.getText().toString().replaceAll("[^\\.0123456789]", "");
        copyToClipboard(text, text);
        Toast.makeText(SalaryActivity.this, getString(R.string.toast_text_copied), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.text_total)
    void onTotalClicked() {
        String text = mTotalSwitcher.getText().toString().replaceAll("[^\\.0123456789]", "");
        copyToClipboard(text, text);
        Toast.makeText(SalaryActivity.this, getString(R.string.toast_text_copied), Toast.LENGTH_SHORT).show();
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
        getDB(DC.DB_SETTINGS_SALARY)
                .orderByChild("dateFrom/time")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mSettingsSalary.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            SettingsSalary settingsSalary = data.getValue(SettingsSalary.class);
                            if (settingsSalary != null) {
                                mSettingsSalary.add(settingsSalary);
                                updateConditionUI();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private SettingsSalary getSettingsSalaryForDate(Date date) {
        SettingsSalary result = new SettingsSalary();
        Date longTimeAgo = new Date();
        longTimeAgo.setTime(1);
        result.setDateFrom(longTimeAgo);
        for (SettingsSalary settingsSalary : mSettingsSalary) {
            if (settingsSalary.getDateFrom().after(result.getDateFrom())
                    && settingsSalary.getDateFrom().before(date)) {
                result = settingsSalary;
            }
        }
        return result;
    }

    private void updateConditionUI() {
        SettingsSalary currentSettings = getSettingsSalaryForDate(mCurrentMonth.getTime());
        mTextSalaryPrivate.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPrivate()));
        mTextSalaryPrivate15.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPrivate15()));
        //
        mTextSalaryPair.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPair()));
        mTextSalaryPair15.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPair15()));
        //
        mTextSalaryGroup.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherGroup()));
        mTextSalaryGroup15.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherGroup15()));
        //
        mTextSalaryOnline.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherOnLine()));
        mTextSalaryOnline15.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherOnLine15()));
        //
        mTextSalaryPrivateChild.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPrivateChild()));
        mTextSalaryPrivate15Child.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPrivate15Child()));
        //
        mTextSalaryPairChild.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPairChild()));
        mTextSalaryPair15Child.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherPair15Child()));
        //
        mTextSalaryGroupChild.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherGroupChild()));
        mTextSalaryGroup15Child.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherGroup15Child()));
        //
        mTextSalaryOnlineChild.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherOnLineChild()));
        mTextSalaryOnline15Child.setText(String.format(getString(R.string.hrn_d), currentSettings.getTeacherOnLine15Child()));
    }

    private void loadLessons() {
        getDB(DC.DB_LESSONS)
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
        int privateTotalChild = 0;
        int pairTotalChild = 0;
        int groupTotalChild = 0;
        int onlineTotalChild = 0;
        int privateTotalCount = 0;
        int pairTotalCount = 0;
        int groupTotalCount = 0;
        int onlineTotalCount = 0;
        int privateTotalChildCount = 0;
        int pairTotalChildCount = 0;
        int groupTotalChildCount = 0;
        int onlineTotalChildCount = 0;
        if (mLessons == null || mSettingsSalary == null || mSettingsSalary.isEmpty()) {
            Log.e("Salary", "Wrong argument!");
            return;
        }
        for (Lesson lesson : mLessons) {
            if (lesson.getStatusType() == 1) continue;
            SettingsSalary currentSettings = getSettingsSalaryForDate(lesson.getDateTime());

            if (lesson.getUserType() == 0) {
                if (lesson.getLessonLengthId() == 0) {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalCount += 1;
                            privateTotal += currentSettings.getTeacherPrivate();
                            break;
                        case 1:
                            pairTotalCount += 1;
                            pairTotal += currentSettings.getTeacherPair();
                            break;
                        case 2:
                            groupTotalCount += 1;
                            groupTotal += currentSettings.getTeacherGroup();
                            break;
                        case 3:
                            onlineTotalCount += 1;
                            onlineTotal += currentSettings.getTeacherOnLine();
                            break;
                    }
                } else {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalCount += 1;
                            privateTotal += currentSettings.getTeacherPrivate15();
                            break;
                        case 1:
                            pairTotalCount += 1;
                            pairTotal += currentSettings.getTeacherPair15();
                            break;
                        case 2:
                            groupTotalCount += 1;
                            groupTotal += currentSettings.getTeacherGroup15();
                            break;
                        case 3:
                            onlineTotalCount += 1;
                            onlineTotal += currentSettings.getTeacherOnLine15();
                            break;
                    }
                }
            } else {
                if (lesson.getLessonLengthId() == 0) {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalChildCount += 1;
                            privateTotalChild += currentSettings.getTeacherPrivateChild();
                            break;
                        case 1:
                            pairTotalChildCount += 1;
                            pairTotalChild += currentSettings.getTeacherPairChild();
                            break;
                        case 2:
                            groupTotalChildCount += 1;
                            groupTotalChild += currentSettings.getTeacherGroupChild();
                            break;
                        case 3:
                            onlineTotalChildCount += 1;
                            onlineTotalChild += currentSettings.getTeacherOnLineChild();
                            break;
                    }
                } else {
                    switch (lesson.getLessonType()) {
                        case 0:
                            privateTotalChildCount += 1;
                            privateTotalChild += currentSettings.getTeacherPrivate15Child();
                            break;
                        case 1:
                            pairTotalChildCount += 1;
                            pairTotalChild += currentSettings.getTeacherPair15Child();
                            break;
                        case 2:
                            groupTotalChildCount += 1;
                            groupTotalChild += currentSettings.getTeacherGroup15Child();
                            break;
                        case 3:
                            onlineTotalChildCount += 1;
                            onlineTotalChild += currentSettings.getTeacherOnLine15Child();
                            break;
                    }
                }
            }
        }
        total = privateTotal + pairTotal + groupTotal + onlineTotal;
        total += privateTotalChild + pairTotalChild + groupTotalChild + onlineTotalChild;
        mTextSalaryPrivateTotal.setText(String.format(getString(R.string.hrn_d), privateTotal + privateTotalChild));
        mTextSalaryPairTotal.setText(String.format(getString(R.string.hrn_d), pairTotal + pairTotalChild));
        mTextSalaryGroupTotal.setText(String.format(getString(R.string.hrn_d), groupTotal + groupTotalChild));
        mTextSalaryOnlineTotal.setText(String.format(getString(R.string.hrn_d), onlineTotal + onlineTotalChild));
        mTotalSwitcher.setText(String.format(getString(R.string.hrn_d), total));
        int totalCount = privateTotalCount + pairTotalCount + groupTotalCount + onlineTotalCount
                + privateTotalChildCount + pairTotalChildCount + groupTotalChildCount + onlineTotalChildCount;
        String salaryDescription = String.format(getString(R.string.text_salary_description_d),
                totalCount,
                privateTotalCount, privateTotal,
                pairTotalCount, pairTotal,
                groupTotalCount, groupTotal,
                onlineTotalCount, onlineTotal,
                privateTotalChildCount, privateTotalChild,
                pairTotalChildCount, pairTotalChild,
                groupTotalChildCount, groupTotalChild,
                onlineTotalChildCount, onlineTotalChild);
        mTextExpand.setText(salaryDescription);
    }
}
