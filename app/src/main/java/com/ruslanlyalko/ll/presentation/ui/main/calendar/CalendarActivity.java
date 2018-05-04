package com.ruslanlyalko.ll.presentation.ui.main.calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.Constants;
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
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

    @BindView(R.id.recycler_view) RecyclerView mReportsList;
    @BindView(R.id.calendar_view) CompactCalendarView mCompactCalendarView;
    @BindView(R.id.swipere_fresh) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.button_prev) ImageButton mButtonPrev;
    @BindView(R.id.button_next) ImageButton mButtonNext;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.button_add_report) TextView mTextAddReport;

    private LessonsAdapter mLessonsAdapter;
    private List<Lesson> mLessonList = new ArrayList<>();
    private ArrayList<String> mUsersList = new ArrayList<>();
    private Date mCurrentDate;
    private String mUserId;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    //private String mDay, mMonth, mYear;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

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
        showReportsOnCalendar();
        Date today = Calendar.getInstance().getTime();
        showReportsForDate(today);
    }

    private void initRecycle() {
        mLessonList = new ArrayList<>();
        mLessonsAdapter = new LessonsAdapter(this, mLessonList);
        mReportsList.setLayoutManager(new LinearLayoutManager(this));
        mReportsList.setAdapter(mLessonsAdapter);
    }

    private void initCalendarView() {
        mCompactCalendarView.setUseThreeLetterAbbreviation(true);
        mCompactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        mCompactCalendarView.shouldScrollMonth(false);
        mCompactCalendarView.displayOtherMonthDays(true);
        Calendar month = Calendar.getInstance();
        mTextMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);
        // define a listener to receive callbacks when certain events happen.
        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                showReportsForDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Calendar month = Calendar.getInstance();
                month.setTime(firstDayOfNewMonth);
                String yearSimple = new SimpleDateFormat("yy", Locale.US).format(firstDayOfNewMonth);
                String str = Constants.MONTH_FULL[month.get(Calendar.MONTH)];
                if (!DateUtils.isCurrentYear(firstDayOfNewMonth))
                    str = str + "'" + yearSimple;
                mTextMonth.setText(str);
            }
        });
        mSwipeRefresh.setOnRefreshListener(() -> {
            showReportsOnCalendar();
            reloadReportsForDate();
        });
    }

    private void showReportsOnCalendar() {
        mSwipeRefresh.setRefreshing(true);
        mDatabase.getReference(DC.DB_LESSONS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mUsersList.clear();
                        mCompactCalendarView.removeAllEvents();
                        for (DataSnapshot datYears : dataSnapshot.getChildren()) {
                            for (DataSnapshot datYear : datYears.getChildren()) {
                                for (DataSnapshot datMonth : datYear.getChildren()) {
                                    for (DataSnapshot datDay : datMonth.getChildren()) {
                                        Lesson lesson = datDay.getValue(Lesson.class);
                                        if (lesson != null && (FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId) || DateUtils.future(lesson.getDateTime()))) {
//                                            int color = getUserColor(lesson.getUserId());
//                                            long date = getDateLongFromStr(lesson.getDateTime());
//                                            String uId = lesson.getUserId();
//                                            mCompactCalendarView.addEvent(
//                                                    new Event(color, date, uId), true);
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

    private void showReportsForDate(Date date) {
        mCurrentDate = date;
        mTextAddReport.setVisibility((FirebaseUtils.isAdmin() || DateUtils.isTodayOrFuture(date))
                ? View.VISIBLE : View.GONE);
        String mDay = DateFormat.format("d", date).toString();
        String mMonth = DateFormat.format("M", date).toString();
        String mYear = DateFormat.format("yyyy", date).toString();
        mDatabase.getReference(DC.DB_LESSONS)
                .child(mYear)
                .child(mMonth)
                .child(mDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mLessonList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Lesson lesson = data.getValue(Lesson.class);
                            if (lesson != null) {
//                                if (FirebaseUtils.isAdmin() || lesson.getUserId().equals(mUserId)) {
//                                    mLessonList.add(lesson);
//                                }
                            }
                        }
                        mLessonsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void reloadReportsForDate() {
        showReportsForDate(mCurrentDate);
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
        showReportsOnCalendar();
        reloadReportsForDate();
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
//        final boolean commentsExist = lesson.getComment() != null & !lesson.getComment().isEmpty();
//        if (commentsExist)
//            Toast.makeText(this, lesson.getComment(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMkClicked(final Lesson lesson) {
//        if (lesson.getMkRef() != null && !lesson.getMkRef().isEmpty()) {
//            Intent intent = new Intent(this, MkDetailsActivity.class);
//            intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, lesson.getMkRef());
//            startActivity(intent);
//        } else {
//            Toast.makeText(this, R.string.toast_mk_not_set, Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onEditClicked(final Lesson lesson) {
        //todo
    }

    @OnClick(R.id.button_add_report)
    void onAddReportClicked() {
        startActivity(LessonActivity.getLaunchIntent(this, new Lesson(new Date(), "-LBft-4dcBhTlDpEDEyu")));
    }
}
