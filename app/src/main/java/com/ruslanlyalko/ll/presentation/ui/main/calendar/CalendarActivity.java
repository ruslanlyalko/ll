package com.ruslanlyalko.ll.presentation.ui.main.calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.LessonsAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.OnLessonClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.lesson.LessonActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class CalendarActivity extends BaseActivity implements OnLessonClickListener {

    private static final int RC_LESSON = 1001;
    @BindView(R.id.recycler_view) RecyclerView mReportsList;
    @BindView(R.id.calendar_view) CompactCalendarView mCompactCalendarView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.button_prev) ImageButton mButtonPrev;
    @BindView(R.id.button_next) ImageButton mButtonNext;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.fab) FloatingActionButton mFab;

    private LessonsAdapter mLessonsAdapter;
    private ArrayList<String> mUsersList = new ArrayList<>();
    private Date mCurrentDate;
    private String mUserId;

    public static Intent getLaunchIntent(final Activity launchIntent) {
        return new Intent(launchIntent, CalendarActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_calendar;
    }

    @Override
    protected void setupView() {
        mUserId = FirebaseAuth.getInstance().getUid();
        initRecycle();
        initCalendarView();
        showLessonsOnCalendar();
        Date today = Calendar.getInstance().getTime();
        showLessonsForDate(today);
        loadContacts();
    }

    private void initRecycle() {
        mLessonsAdapter = new LessonsAdapter(this);
        mReportsList.setLayoutManager(new LinearLayoutManager(this));
        mReportsList.setAdapter(mLessonsAdapter);
    }

    private void initCalendarView() {
        mCompactCalendarView.setUseThreeLetterAbbreviation(true);
        mCompactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        mCompactCalendarView.shouldScrollMonth(false);
        mCompactCalendarView.displayOtherMonthDays(true);
        mTextMonth.setText(DateUtils.getMonth(getResources(), Calendar.getInstance()));
        // define a listener to receive callbacks when certain events happen.
        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                showLessonsForDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Calendar month = Calendar.getInstance();
                month.setTime(firstDayOfNewMonth);
                String yearSimple = new SimpleDateFormat("yy", Locale.US).format(firstDayOfNewMonth);
                String str = DateUtils.getMonth(getResources(), month);
                if (!DateUtils.isCurrentYear(firstDayOfNewMonth))
                    str = str + "'" + yearSimple;
                mTextMonth.setText(str);
                showLessonsForDate(firstDayOfNewMonth);
            }
        });
        mSwipeRefresh.setOnRefreshListener(() -> {
            showLessonsOnCalendar();
            reloadLessons();
        });
    }

    @OnClick(R.id.button_prev)
    void onPrevClicked() {
        mCompactCalendarView.showPreviousMonth();
    }

    @OnClick(R.id.button_next)
    void onNextClicked() {
        mCompactCalendarView.showNextMonth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        reloadLessons();
    }

    private void reloadLessons() {
        showLessonsForDate(mCurrentDate);
    }

    private void showLessonsForDate(Date date) {
        if (isDestroyed()) return;
        mCurrentDate = date;
        mFab.setVisibility((FirebaseUtils.isAdmin() || DateUtils.isTodayOrFuture(date))
                ? View.VISIBLE : View.GONE);
        String aDate = DateFormat.format("yyyy/MM/dd", date).toString();
        getDatabase().getReference(DC.DB_LESSONS)
                .child(aDate)
                .orderByChild("dateTime/time")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Lesson> lessons = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Lesson lesson = data.getValue(Lesson.class);
                            if (lesson != null) {
                                if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId)) {
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

    private void showLessonsOnCalendar() {
        mSwipeRefresh.setRefreshing(true);
        getDatabase().getReference(DC.DB_LESSONS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (isDestroyed()) return;
                        mUsersList.clear();
                        mCompactCalendarView.removeAllEvents();
                        for (DataSnapshot datYears : dataSnapshot.getChildren()) {
                            for (DataSnapshot datYear : datYears.getChildren()) {
                                for (DataSnapshot datMonth : datYear.getChildren()) {
                                    for (DataSnapshot datDay : datMonth.getChildren()) {
                                        Lesson lesson = datDay.getValue(Lesson.class);
                                        if (lesson != null && (FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId))) {
                                            int color = getUserColor(lesson.getUserId());
                                            long date = lesson.getDateTime().getTime();
                                            String uId = lesson.getUserId();
                                            mCompactCalendarView.addEvent(
                                                    new Event(color, date, uId), true);
                                        }
                                    }
                                }
                            }
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private int getUserColor(String userId) {
        if (!mUsersList.contains(userId)) {
            mUsersList.add(userId);
        }
        int index = mUsersList.indexOf(userId);
        int[] colors = getResources().getIntArray(R.array.colors);
        if (index < 6)
            return colors[index];
        else
            return Color.GREEN;
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
        if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
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
            reloadLessons();
            Toast.makeText(this, R.string.toast_lesson_removed, Toast.LENGTH_LONG).show();
        });
    }

    @OnClick(R.id.fab)
    void onAddReportClicked() {
        startActivityForResult(LessonActivity.getLaunchIntent(this, mCurrentDate), RC_LESSON);
    }
}
