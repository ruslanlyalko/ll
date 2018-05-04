package com.ruslanlyalko.ll.presentation.ui.main.lesson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.common.LessonLength;
import com.ruslanlyalko.ll.common.UserType;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.clients.OnFilterListener;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.ContactsFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class LessonActivity extends BaseActivity implements OnFilterListener {

    private static final int TAB_ADULT = 0;
    private static final int TAB_CHILD = 1;
    @BindView(R.id.text_date) TextView mTextDate;
    @BindView(R.id.text_time) TextView mTextTime;
    @BindView(R.id.text_lesson_length) TextView mTextLessonLength;
    @BindView(R.id.tabs_room) TabLayout mTabsRoom;
    @BindView(R.id.tabs_lesson) TabLayout mTabsLesson;
    @BindView(R.id.tabs_user) TabLayout mTabsUser;
    @BindView(R.id.container) ViewPager mContainer;
    private boolean mIsChanged;
    private List<Contact> mSelectedContactsAdults = new ArrayList<>();
    private List<Contact> mSelectedContactsChildren = new ArrayList<>();
    private String mKey = "";
    private LessonLength mLessonLength = LessonLength.ONE_HOUR;
    private Lesson mLesson = new Lesson();

    public static Intent getLaunchIntent(final Activity launchIntent) {
        return new Intent(launchIntent, LessonActivity.class);
    }

    public static Intent getLaunchIntent(final Context context, String key) {
        Intent intent = new Intent(context, LessonActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, key);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_lesson;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mKey = bundle.getString(Keys.Extras.EXTRA_ITEM_ID, "");
        }
    }

    @Override
    protected void setupView() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mContainer.setAdapter(sectionsPagerAdapter);
        mContainer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabsUser));
        mTabsUser.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mContainer));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add: {
                saveReportToDB();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mIsChanged) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LessonActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_report_save_before_close_text)
                    .setPositiveButton(R.string.action_save, (dialog, which) -> {
                        saveReportToDB();
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.action_no, (dialog, which) -> {
                        super.onBackPressed();
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void saveReportToDB() {
        //todo
    }

    @Override
    public void onFilterChanged(final String name, final String phone) {
        //not used
    }

    @Override
    public void onCheckedChanged(final List<Contact> selected, final UserType userType) {
        if (userType == UserType.ADULT) {
            mSelectedContactsAdults.clear();
            mSelectedContactsAdults.addAll(selected);
        } else {
            mSelectedContactsChildren.clear();
            mSelectedContactsChildren.addAll(selected);
        }
    }

    @OnClick({R.id.text_date, R.id.text_time, R.id.text_lesson_length})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_date:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mLesson.getDateTime());
                DatePickerDialog dialog = DatePickerDialog.newInstance((datePicker, year, month, day)
                                -> {
                            mLesson.setDateTime(DateUtils.getDate(year, month, day));
                            mTextDate.setText(DateUtils.toString(mLesson.getDateTime(), "dd.MM  EEEE"));
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                dialog.show(getFragmentManager(), "date");
                break;
            case R.id.text_time:
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(mLesson.getDateTime());
                TimePickerDialog dialog1 = TimePickerDialog.newInstance((datePicker, hours, minutes, seconds)
                                -> {
                            mLesson.setDateTime(DateUtils.getDate(mLesson.getDateTime(), hours, minutes));
                            mTextTime.setText(DateUtils.toString(mLesson.getDateTime(), "HH:mm"));
                        },
                        calendar1.get(Calendar.HOUR_OF_DAY),
                        calendar1.get(Calendar.MINUTE), true);
                dialog1.show(getFragmentManager(), "time");
                break;
            case R.id.text_lesson_length:
                if (mLessonLength == LessonLength.ONE_HOUR) {
                    mLessonLength = LessonLength.ONE_HALF_HOUR;
                    mTextLessonLength.setText(R.string.lesson_length_one_half_hour);
                } else {
                    mLessonLength = LessonLength.ONE_HOUR;
                    mTextLessonLength.setText(R.string.lesson_length_one_hour);
                }
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_ADULT:
                    return ContactsFragment.newInstance(position, true);
                case TAB_CHILD:
                    return ContactsFragment.newInstance(position, true);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
