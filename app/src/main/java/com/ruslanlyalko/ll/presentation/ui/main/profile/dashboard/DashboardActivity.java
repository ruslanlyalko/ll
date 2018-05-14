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
import com.ruslanlyalko.ll.common.DateUtils;
import com.ruslanlyalko.ll.common.ViewUtils;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Expense;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.data.models.Result;
import com.ruslanlyalko.ll.data.models.User;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.expenses.ExpensesActivity;
import com.ruslanlyalko.ll.presentation.ui.main.profile.dashboard.adapter.UsersSalaryAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.profile.salary.SalaryActivity;
import com.ruslanlyalko.ll.presentation.widget.OnItemClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DashboardActivity extends BaseActivity implements OnItemClickListener {

    @BindView(R.id.calendar_view) CompactCalendarView mCompactCalendarView;
    @BindView(R.id.text_total) TextView textTotal;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.text_cost_total) TextView textCostTotal;
    @BindView(R.id.text_expenses_type0) TextView mTextExpensesType0;
    @BindView(R.id.text_expenses_type1) TextView mTextExpensesType1;
    @BindView(R.id.text_birthdays) TextView textBirthdays;
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
    @BindView(R.id.text_salary_private_total) TextView mTextSalaryPrivateTotal;
    @BindView(R.id.text_salary_pair_total) TextView mTextSalaryPairTotal;
    @BindView(R.id.text_salary_group_total) TextView mTextSalaryGroupTotal;
    @BindView(R.id.text_salary_online_total) TextView mTextSalaryOnlineTotal;
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
        loadResults();
        loadEverything();
    }

    private void initRecycler() {
        mListUsersSalary.setLayoutManager(new LinearLayoutManager(this));
        mListUsersSalary.setAdapter(mUsersSalaryAdapter);
    }

    private void loadResults() {
        getDatabase().getReference(DC.DB_RESULTS)
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
                return mResults.get((int) value).getMonth();
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
                return mResults.get((int) value).getMonth();
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
        textCostTotal.setText(String.format(getString(R.string.HRN), DateUtils.getIntWithSpace(mExpensesTotal)));
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
        getDatabase().getReference(DC.DB_LESSONS)
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
        getDatabase().getReference(DC.DB_EXPENSES)
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
        getDatabase().getReference(DC.DB_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mUsers.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            User user = data.getValue(User.class);
                            if (user != null) {
                                mUsers.add(0, user);
                            }
                        }
                        calcSalaryForUsers();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void calcSalaryForUsers() {
        if (mUsers.isEmpty() || mLessonList.isEmpty()) {
            return;
        }
        //todo
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
        ArrayList<BarEntry> values80 = new ArrayList<>();
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
        BarDataSet set180;
        BarDataSet set1Profit;
        if (mBarChartIncome.getData() != null &&
                mBarChartIncome.getData().getDataSetCount() == 1) {
            set1 = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(0);
            set1.setValues(values);
        } else if (mBarChartIncome.getData() != null &&
                mBarChartIncome.getData().getDataSetCount() == 2) {
            set1 = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set180 = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(1);
            set180.setValues(values80);
        } else if (mBarChartIncome.getData() != null &&
                mBarChartIncome.getData().getDataSetCount() == 3) {
            set1 = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set180 = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(1);
            set180.setValues(values80);
            set1Profit = (BarDataSet) mBarChartIncome.getData().getDataSetByIndex(2);
            set1Profit.setValues(valuesProfit);
        } else {
            set1 = new BarDataSet(values, "");
            set1.setColors(colors);
            set1.setDrawIcons(true);
            set1.setDrawValues(true);
            set1Profit = new BarDataSet(values, "");
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
