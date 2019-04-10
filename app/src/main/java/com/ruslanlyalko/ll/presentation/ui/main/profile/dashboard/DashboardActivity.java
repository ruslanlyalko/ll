package com.ruslanlyalko.ll.presentation.ui.main.profile.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Expense;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.data.models.Result;
import com.ruslanlyalko.ll.data.models.SettingsSalary;
import com.ruslanlyalko.ll.data.models.User;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.expenses.ExpensesActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.dashboard.adapter.UsersSalaryAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.profile.salary.SalaryActivity;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;
import com.ruslanlyalko.ll.presentation.utils.ViewUtils;
import com.ruslanlyalko.ll.presentation.widget.OnItemClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DashboardActivity extends BaseActivity implements OnItemClickListener {

    @BindView(R.id.calendar_view) CompactCalendarView mCompactCalendarView;
    @BindView(R.id.text_total) TextView mTextIncomeTotal;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.text_cost_total) TextView mTextExpensesTotal;
    @BindView(R.id.text_expenses_type0) TextView mTextExpensesType0;
    @BindView(R.id.text_expenses_type1) TextView mTextExpensesType1;
    @BindView(R.id.text_birthdays) TextView mTextBirthdays;
    @BindView(R.id.edit_comment) EditText editComment;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.progress_bar_cost) ProgressBar mProgressBarExpenses;
    @BindView(R.id.progress_bar_salary) ProgressBar progressBarSalary;
    @BindView(R.id.list_users_salary) RecyclerView mListUsersSalary;
    @BindView(R.id.layout_collapsing) NestedScrollView mLayoutCollapsing;
    @BindView(R.id.image_expand) ImageView mImageView;
    @BindView(R.id.bar_chart) BarChart mBarChart;
    @BindView(R.id.bar_chart_income) BarChart mBarChartIncome;
    @BindView(R.id.card_expenses) CardView mCardExpenses;
    //--
    @BindView(R.id.text_income_private) TextView mTextIncomePrivate;
    @BindView(R.id.text_income_pair) TextView mTextIncomePair;
    @BindView(R.id.text_income_group) TextView mTextIncomeGroup;
    @BindView(R.id.text_income_online) TextView mTextIncomeOnline;
    @BindView(R.id.text_salary_total) TextView mTextSalaryTotal;
    @BindView(R.id.text_salary_private) TextView mTextSalaryPrivate;
    @BindView(R.id.text_salary_pair) TextView mTextSalaryPair;
    @BindView(R.id.text_salary_group) TextView mTextSalaryGroup;
    @BindView(R.id.text_salary_online) TextView mTextSalaryOnline;
    private UsersSalaryAdapter mUsersSalaryAdapter = new UsersSalaryAdapter(this);
    private List<Lesson> mLessonList = new ArrayList<>();
    private List<Expense> mExpenses = new ArrayList<>();
    private List<User> mUsers = new ArrayList<>();
    private List<Result> mResults;
    private int mIncomeTotal;
    private int mSalaryTotal;
    private int mExpensesTotal;
    private Calendar mCurrentMonth = Calendar.getInstance();
    private List<SettingsSalary> mSettingsSalary = new ArrayList<>();

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity) {
        return new Intent(launchActivity, DashboardActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_dashboard;
    }

    @Override
    protected void setupView() {
        initBarChart();
        initBarChartIncome();
        initRecycler();
        updateMonth();
        loadSettingsSalaries();
        loadResults();
        loadEverything();
    }

    private void initRecycler() {
        mListUsersSalary.setLayoutManager(new LinearLayoutManager(this));
        mListUsersSalary.setAdapter(mUsersSalaryAdapter);
    }

    private void loadResults() {
        getDB(DC.DB_RESULTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<Result> results = new ArrayList<>();
                        for (DataSnapshot year : dataSnapshot.getChildren()) {
                            for (DataSnapshot month : year.getChildren()) {
                                Result result = month.getValue(Result.class);
                                if (result != null) {
                                    results.add(result);
                                }
                            }
                            updateBarChart(results);
                            updateBarChartIncome(results);
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void initBarChart() {
        mBarChart.setDrawGridBackground(false);
        mBarChart.getLegend().setEnabled(false);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.getXAxis().setEnabled(true);
        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        mBarChart.getAxisLeft().enableGridDashedLine(1, 1, 10);
        mBarChart.setMaxVisibleValueCount(150);
        mBarChart.setDrawValueAboveBar(true);
        mBarChart.setDoubleTapToZoomEnabled(false);
        mBarChart.setPinchZoom(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.setTouchEnabled(false);
        mBarChart.setDrawBarShadow(false);
        mBarChart.getAxisLeft().setAxisMinimum(0);
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if ((int) value < mResults.size())
                    return mResults.get((int) value).getMonth();
                return "";
            }
        });
    }

    private void initBarChartIncome() {
        mBarChartIncome.setDrawGridBackground(false);
        mBarChartIncome.getLegend().setEnabled(false);
        mBarChartIncome.getDescription().setEnabled(false);
        mBarChartIncome.getXAxis().setEnabled(true);
        mBarChartIncome.getAxisRight().setEnabled(false);
        mBarChartIncome.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        mBarChartIncome.getAxisLeft().enableGridDashedLine(1, 1, 10);
        mBarChartIncome.setMaxVisibleValueCount(150);
        mBarChartIncome.setDrawValueAboveBar(true);
        mBarChartIncome.setDoubleTapToZoomEnabled(false);
        mBarChartIncome.setPinchZoom(false);
        mBarChartIncome.setScaleEnabled(false);
        mBarChartIncome.setTouchEnabled(false);
        mBarChartIncome.setDrawBarShadow(false);
        mBarChartIncome.getAxisLeft().setAxisMinimum(0);
        XAxis xAxis = mBarChartIncome.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if ((int) value < mResults.size())
                    return mResults.get((int) value).getMonth();
                return "";
            }
        });
    }

    @OnClick(R.id.button_prev)
    void onPrevClicked() {
        mCurrentMonth.add(Calendar.MONTH, -1);
        updateMonth();
        loadEverything();
    }

    @OnClick(R.id.button_next)
    void onNextClicked() {
        mCurrentMonth.add(Calendar.MONTH, 1);
        updateMonth();
        loadEverything();
    }

    private void loadEverything() {
        loadLessons();
        loadExpenses();
        loadUsers();
    }

    private void updateMonth() {
        mTextMonth.setText(DateUtils.getMonthWithYear(getResources(), mCurrentMonth));
    }

    private void calcExpensesTotal() {
        int type0 = 0;
        int type1 = 0;
        for (Expense expense : mExpenses) {
            if (expense.getTypeId() == 0)
                type0 += expense.getPrice();
            if (expense.getTypeId() == 1)
                type1 += expense.getPrice();
        }
        mExpensesTotal = type0 + type1;
        mProgressBarExpenses.setMax(mExpensesTotal);
        mProgressBarExpenses.setProgress(type0);
        mProgressBarExpenses.setSecondaryProgress(type0 + type1);
        mTextExpensesType0.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(type0)));
        mTextExpensesType1.setText(String.format(getString(R.string.hrn), DateUtils.getIntWithSpace(type1)));
        mTextExpensesTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(mExpensesTotal)));
        updateNetIncome();
    }

    private void updateNetIncome() {
        int netIncome = mIncomeTotal - mExpensesTotal - mSalaryTotal;
        setTitle(String.format(getString(R.string.title_activity_dashboard), DateUtils.getIntWithSpace(netIncome)));
        String year = DateUtils.toString(mCurrentMonth.getTime(), "yyyy");
        String month = DateUtils.toString(mCurrentMonth.getTime(), "MM");
        Result result = new Result(mIncomeTotal, mSalaryTotal, mExpensesTotal, netIncome, year, month);
        if (mIncomeTotal != 0 && mSalaryTotal != 0)
            FirebaseDatabase.getInstance()
                    .getReference(DC.DB_RESULTS)
                    .child(year)
                    .child(month)
                    .setValue(result);
        loadResults();
    }

    @OnClick(R.id.panel_action)
    void onExpandClicked() {
        if (mLayoutCollapsing.getVisibility() == View.VISIBLE) {
            mImageView.setImageResource(R.drawable.ic_action_expand_more);
            ViewUtils.collapse(mLayoutCollapsing);
        } else {
            ViewUtils.expand(mLayoutCollapsing);
            mImageView.setImageResource(R.drawable.ic_action_expand_less);
        }
    }

    private void loadLessons() {
        getDB(DC.DB_LESSONS)
                .child(DateUtils.toString(mCurrentMonth.getTime(), "yyyy/MM"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mLessonList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            for (DataSnapshot ds : data.getChildren()) {
                                Lesson lesson = ds.getValue(Lesson.class);
                                if (lesson != null) {
                                    mLessonList.add(lesson);
                                }
                            }
                        }
                        calcSalaryForUsers();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void loadExpenses() {
        getDB(DC.DB_EXPENSES)
                .child(DateUtils.toString(mCurrentMonth.getTime(), "yyyy/M"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mExpenses.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Expense expense = data.getValue(Expense.class);
                            if (expense != null) {
                                mExpenses.add(0, expense);
                            }
                        }
                        calcExpensesTotal();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void loadUsers() {
        getDataManager().getAllUsers().observe(this, list -> {
            if (list == null) return;
            mUsers.clear();
            for (User user : list) {
                if (user != null)
                    mUsers.add(user);
            }
            calcSalaryForUsers();
        });
    }

    private void calcSalaryForUsers() {
        mSalaryTotal = 0;
        mIncomeTotal = 0;
        String birthdays = "";
        //income
        int iPrivate = 0;
        int iPair = 0;
        int iGroup = 0;
        int iOnLine = 0;
        // global
        int aPrivate = 0;
        int aPair = 0;
        int aGroup = 0;
        int aOnLine = 0;
        mUsersSalaryAdapter.clearAll();
        for (int i = 0; i < mUsers.size(); i++) {
            User user = mUsers.get(i);
            // income
            int totalIncome = 0;
            int privateTotalIncome = 0;
            int pairTotalIncome = 0;
            int groupTotalIncome = 0;
            int onlineTotalIncome = 0;
            int privateTotalChildIncome = 0;
            int pairTotalChildIncome = 0;
            int groupTotalChildIncome = 0;
            int onlineTotalChildIncome = 0;
            // local
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
            for (Lesson lesson : mLessonList) {
                if (!lesson.getUserId().equals(user.getId())) continue;
                if (lesson.getStatusType() == 1) continue;
                SettingsSalary currentSettings = getSettingsSalaryForDate(lesson.getDateTime());
                if (lesson.getUserType() == 0) {
                    if (lesson.getLessonLengthId() == 0) {
                        switch (lesson.getLessonType()) {
                            case 0:
                                privateTotalCount += 1;
                                privateTotal += currentSettings.getTeacherPrivate();
                                privateTotalIncome += currentSettings.getStudentPrivate();
                                break;
                            case 1:
                                pairTotalCount += 1;
                                pairTotal += currentSettings.getTeacherPair();
                                pairTotalIncome += currentSettings.getStudentPair() * lesson.getClients().size();
                                break;
                            case 2:
                                groupTotalCount += 1;
                                groupTotal += currentSettings.getTeacherGroup();
                                groupTotalIncome += currentSettings.getStudentGroup() * lesson.getClients().size();
                                break;
                            case 3:
                                onlineTotalCount += 1;
                                onlineTotal += currentSettings.getTeacherOnLine();
                                onlineTotalIncome += currentSettings.getStudentOnLine();
                                break;
                        }
                    } else {
                        switch (lesson.getLessonType()) {
                            case 0:
                                privateTotalCount += 1;
                                privateTotal += currentSettings.getTeacherPrivate15();
                                privateTotalIncome += currentSettings.getStudentPrivate15();
                                break;
                            case 1:
                                pairTotalCount += 1;
                                pairTotal += currentSettings.getTeacherPair15();
                                pairTotalIncome += currentSettings.getStudentPair15() * lesson.getClients().size();
                                break;
                            case 2:
                                groupTotalCount += 1;
                                groupTotal += currentSettings.getTeacherGroup15();
                                groupTotalIncome += currentSettings.getStudentGroup15() * lesson.getClients().size();
                                break;
                            case 3:
                                onlineTotalCount += 1;
                                onlineTotal += currentSettings.getTeacherOnLine15();
                                onlineTotalIncome += currentSettings.getStudentOnLine15();
                                break;
                        }
                    }
                } else {
                    if (lesson.getLessonLengthId() == 0) {
                        switch (lesson.getLessonType()) {
                            case 0:
                                privateTotalChildCount += 1;
                                privateTotalChild += currentSettings.getTeacherPrivateChild();
                                privateTotalChildIncome += currentSettings.getStudentPrivateChild();
                                break;
                            case 1:
                                pairTotalChildCount += 1;
                                pairTotalChild += currentSettings.getTeacherPairChild();
                                pairTotalChildIncome += currentSettings.getStudentPairChild() * lesson.getClients().size();
                                break;
                            case 2:
                                groupTotalChildCount += 1;
                                groupTotalChild += currentSettings.getTeacherGroupChild();
                                groupTotalChildIncome += currentSettings.getStudentGroupChild() * lesson.getClients().size();
                                break;
                            case 3:
                                onlineTotalChildCount += 1;
                                onlineTotalChild += currentSettings.getTeacherOnLineChild();
                                onlineTotalChildIncome += currentSettings.getStudentOnLineChild();
                                break;
                        }
                    } else {
                        switch (lesson.getLessonType()) {
                            case 0:
                                privateTotalChildCount += 1;
                                privateTotalChild += currentSettings.getTeacherPrivate15Child();
                                privateTotalChildIncome += currentSettings.getStudentPrivate15Child();
                                break;
                            case 1:
                                pairTotalChildCount += 1;
                                pairTotalChild += currentSettings.getTeacherPair15Child();
                                pairTotalChildIncome += currentSettings.getStudentPair15Child() * lesson.getClients().size();
                                break;
                            case 2:
                                groupTotalChildCount += 1;
                                groupTotalChild += currentSettings.getTeacherGroup15Child();
                                groupTotalChildIncome += currentSettings.getStudentGroup15Child() * lesson.getClients().size();
                                break;
                            case 3:
                                onlineTotalChildCount += 1;
                                onlineTotalChild += currentSettings.getTeacherOnLine15Child();
                                onlineTotalChildIncome += currentSettings.getStudentOnLine15Child();
                                break;
                        }
                    }
                }
            }//lessons
            total = privateTotal + pairTotal + groupTotal + onlineTotal +
                    privateTotalChild + pairTotalChild + groupTotalChild + onlineTotalChild;
            totalIncome = privateTotalIncome + pairTotalIncome + groupTotalIncome + onlineTotalIncome +
                    privateTotalChildIncome + pairTotalChildIncome + groupTotalChildIncome + onlineTotalChildIncome;
            mSalaryTotal += total;
            mIncomeTotal += totalIncome;
            //
            iPrivate += privateTotalIncome + privateTotalChildIncome;
            iPair += pairTotalIncome + pairTotalChildIncome;
            iGroup += groupTotalIncome + groupTotalChildIncome;
            iOnLine += onlineTotalIncome + onlineTotalChildIncome;
            //
            aPrivate += privateTotal + privateTotalChild;
            aPair += pairTotal + pairTotalChild;
            aGroup += groupTotal + groupTotalChild;
            aOnLine += onlineTotal + onlineTotalChild;
            if (total != 0)
                mUsersSalaryAdapter.add(user, total);
            birthdays = birthdays.concat(user.getBirthdayDate() + " - " + user.getFullName() + "\n");
        }//user
        mTextIncomePrivate.setText(String.format(getString(R.string.hrn_d), iPrivate));
        mTextIncomePair.setText(String.format(getString(R.string.hrn_d), iPair));
        mTextIncomeGroup.setText(String.format(getString(R.string.hrn_d), iGroup));
        mTextIncomeOnline.setText(String.format(getString(R.string.hrn_d), iOnLine));
        mTextSalaryPrivate.setText(String.format(getString(R.string.hrn_d), aPrivate));
        mTextSalaryPair.setText(String.format(getString(R.string.hrn_d), aPair));
        mTextSalaryGroup.setText(String.format(getString(R.string.hrn_d), aGroup));
        mTextSalaryOnline.setText(String.format(getString(R.string.hrn_d), aOnLine));
        mTextSalaryTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(mSalaryTotal)));
        mTextIncomeTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(mIncomeTotal)));
        mTextBirthdays.setText(birthdays);
        updateNetIncome();
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

    private void updateBarChart(List<Result> resultList) {
        mResults = resultList;
        ArrayList<BarEntry> values = new ArrayList<>();
        int[] colors = new int[resultList.size()];
        String yearStr = DateFormat.format("yyyy", Calendar.getInstance()).toString();
        for (int i = 0; i < resultList.size(); i++) {
            Result current = resultList.get(i);
            float val = current.getProfit();
            colors[i] = current.getYear().equals(yearStr)
                    ? ContextCompat.getColor(this, R.color.colorAccent)
                    : ContextCompat.getColor(this, R.color.colorPrimary);
            values.add(new BarEntry(i, val, val));
        }
        BarDataSet set1;
        if (mBarChart.getData() != null &&
                mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
        } else {
            set1 = new BarDataSet(values, "");
            set1.setColors(colors);
            set1.setDrawIcons(true);
            set1.setDrawValues(true);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setBarWidth(0.9f);
            data.setDrawValues(true);
            // data.setValueFormatter(new LabelValueFormatter());
            data.setValueTextColor(Color.BLACK);
            set1.setValueTextSize(10f);
            mBarChart.setData(data);
        }
        mBarChart.getData().notifyDataChanged();
        mBarChart.notifyDataSetChanged();
        mBarChart.invalidate();
    }

    private void updateBarChartIncome(List<Result> resultList) {
        mResults = resultList;
        int[] colors = new int[resultList.size()];
        int[] colorsProfit = new int[resultList.size()];
        ArrayList<BarEntry> values = new ArrayList<>();
        ArrayList<BarEntry> valuesProfit = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++) {
            Result current = resultList.get(i);
            float val = current.getIncome();
            float valProfit = current.getProfit();
            colors[i] = ContextCompat.getColor(this, R.color.colorProfit);
            colorsProfit[i] = ContextCompat.getColor(this, R.color.colorPrimary);
            values.add(new BarEntry(i, val, val));
            valuesProfit.add(new BarEntry(i, valProfit, valProfit));
        }
        BarDataSet set1;
        BarDataSet set1Profit;
        if (mBarChartIncome.getData() != null &&
                mBarChartIncome.getData().getDataSetCount() == 1) {
            set1 = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(0);
            set1.setValues(values);
        } else if (mBarChartIncome.getData() != null &&
                mBarChartIncome.getData().getDataSetCount() == 2) {
            set1 = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1Profit = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(1);
            set1Profit.setValues(valuesProfit);
        } else {
            set1 = new BarDataSet(values, "");
            set1.setColors(colors);
            set1.setDrawIcons(true);
            set1.setDrawValues(true);
            set1Profit = new BarDataSet(valuesProfit, "");
            set1Profit.setColors(colorsProfit);
            set1Profit.setDrawIcons(true);
            set1Profit.setDrawValues(true);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            dataSets.add(set1Profit);
            BarData data = new BarData(dataSets);
            data.setBarWidth(0.90f);
            data.setDrawValues(true);
            // data.setValueFormatter(new LabelValueFormatter());
            data.setValueTextColor(Color.BLACK);
            set1.setValueTextSize(10f);
            set1Profit.setValueTextSize(9f);
            mBarChartIncome.setData(data);
        }
        mBarChartIncome.getData().notifyDataChanged();
        mBarChartIncome.notifyDataSetChanged();
        mBarChartIncome.invalidate();
    }

    @Override
    public void onItemClicked(final int position) {
        User user = mUsersSalaryAdapter.getItemAtPosition(position);
        startActivity(SalaryActivity.getLaunchIntent(this, user));
    }

    @OnClick(R.id.card_expenses)
    public void onExpensesClicked() {
        startActivity(ExpensesActivity.getLaunchIntent(this));
    }
}
