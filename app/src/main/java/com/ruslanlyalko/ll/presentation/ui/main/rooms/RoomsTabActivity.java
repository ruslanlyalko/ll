package com.ruslanlyalko.ll.presentation.ui.main.rooms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.LessonsAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.OnLessonClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.lesson.LessonActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RoomsTabActivity extends BaseActivity {

    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    private Date mCurrentDate = new Date();
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, RoomsTabActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_room_tab;
    }

    @Override
    protected void setupView() {
        initToolbar();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        setTitle(getString(R.string.title_activity_rooms_tab, DateUtils.toString(mCurrentDate, "dd.MM  EEEE")));
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rooms_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_plan) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mCurrentDate);
            DatePickerDialog dialog = DatePickerDialog.newInstance((datePicker, year, month, day)
                            -> {
                        mCurrentDate = DateUtils.getDate(mCurrentDate, year, month, day);
                        setTitle(getString(R.string.title_activity_rooms_tab, DateUtils.toString(mCurrentDate, "dd.MM  EEEE")));
                        mSectionsPagerAdapter.setDate(mCurrentDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show(getFragmentManager(), "date");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment implements OnLessonClickListener {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final int RC_LESSON = 1001;
        @BindView(R.id.recycler_view) RecyclerView mReportsList;
        private int mCurrentRoomType;
        private Date mCurrentDate = new Date();
        private String mUserId;
        private LessonsAdapter mLessonsAdapter = new LessonsAdapter(this);

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_room, container, false);
            ButterKnife.bind(this, rootView);
            parseArgs();
            return rootView;
        }

        private void parseArgs() {
            if (getArguments() != null)
                mCurrentRoomType = getArguments().getInt(ARG_SECTION_NUMBER, 0);
        }

        public void setDate(Date date) {
            mCurrentDate = date;
            loadLessons();
        }

        @Override
        public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mUserId = FirebaseAuth.getInstance().getUid();
            initRecycler();
            loadLessons();
        }

        private void initRecycler() {
            mReportsList.setLayoutManager(new LinearLayoutManager(getContext()));
            mReportsList.setAdapter(mLessonsAdapter);
        }

        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            reloadLessons();
        }

        private void loadLessons() {
            if (getActivity() != null && getActivity().isDestroyed()) return;
            String aDate = DateFormat.format("yyyy/MM/dd", mCurrentDate).toString();
            FirebaseDatabase.getInstance().getReference(DC.DB_LESSONS)
                    .child(aDate)
                    .orderByChild("dateTime/time")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            List<Lesson> lessons = new ArrayList<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                Lesson lesson = data.getValue(Lesson.class);
                                if (lesson != null) {
                                    if ((FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId))
                                            && lesson.getRoomType() == mCurrentRoomType) {
                                        lessons.add(lesson);
                                    }
                                }
                            }
                            mLessonsAdapter.setData(lessons);
                        }

                        @Override
                        public void onCancelled(final DatabaseError databaseError) {
                        }
                    });
        }

        @Override
        public void onCommentClicked(final Lesson lesson) {
            final boolean commentsExist = lesson.getDescription() != null & !lesson.getDescription().isEmpty();
            if (commentsExist)
                Toast.makeText(getContext(), lesson.getDescription(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onEditClicked(final Lesson lesson) {
            startActivityForResult(LessonActivity.getLaunchIntent(getContext(), lesson), RC_LESSON);
        }

        @Override
        public void onRemoveClicked(final Lesson lesson) {
            if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.dialog_calendar_remove_title)
                        .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                            removeLesson(lesson);
                        })
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            } else {
                Toast.makeText(getContext(), R.string.toast_lesson_remove_unavailable, Toast.LENGTH_LONG).show();
            }
        }

        private void removeLesson(final Lesson lesson) {
            FirebaseDatabase.getInstance()
                    .getReference(DC.DB_LESSONS)
                    .child(DateUtils.toString(lesson.getDateTime(), "yyyy/MM/dd"))
                    .child(lesson.getKey()).removeValue().addOnSuccessListener(aVoid -> {
                reloadLessons();
                Toast.makeText(getContext(), R.string.toast_lesson_removed, Toast.LENGTH_LONG).show();
            });
        }

        private void reloadLessons() {
            loadLessons();
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        PlaceholderFragment room0;
        PlaceholderFragment room1;
        PlaceholderFragment room2;
        PlaceholderFragment room3;

        public PlaceholderFragment getRoom0() {
            if (room0 == null)
                room0 = PlaceholderFragment.newInstance(0);
            return room0;
        }

        public PlaceholderFragment getRoom1() {
            if (room1 == null)
                room1 = PlaceholderFragment.newInstance(1);
            return room1;
        }

        public PlaceholderFragment getRoom2() {
            if (room2 == null)
                room2 = PlaceholderFragment.newInstance(2);
            return room2;
        }

        public PlaceholderFragment getRoom3() {
            if (room3 == null)
                room3 = PlaceholderFragment.newInstance(3);
            return room3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getRoom0();
                case 1:
                    return getRoom1();
                case 2:
                    return getRoom2();
                default:
                    return getRoom3();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_room_1);
                case 1:
                    return getString(R.string.tab_room_2);
                case 2:
                    return getString(R.string.tab_room_3);
                default:
                    return getString(R.string.tab_room_4);
            }
        }

        public void setDate(final Date currentDate) {
            getRoom0().setDate(currentDate);
            getRoom1().setDate(currentDate);
            getRoom2().setDate(currentDate);
            getRoom3().setDate(currentDate);
        }
    }
}
