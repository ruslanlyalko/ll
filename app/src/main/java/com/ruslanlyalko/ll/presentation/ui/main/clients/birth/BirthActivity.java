package com.ruslanlyalko.ll.presentation.ui.main.clients.birth;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.clients.birth.adapter.BirthContactsAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.adapter.OnContactClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.ContactDetailsActivity;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BirthActivity extends BaseActivity implements OnContactClickListener {

    @BindView(R.id.calendar_view) CompactCalendarView mCalendarView;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.list_kids) RecyclerView mListKids;
    private BirthContactsAdapter mBirthContactsAdapter;
    private Date mLastDate = new Date();

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, BirthActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_birth;
    }

    @Override
    protected void setupView() {
        setupRecycler();
        loadContacts();
    }

    private void setupRecycler() {
        mBirthContactsAdapter = new BirthContactsAdapter(this, this);
        mListKids.setLayoutManager(new LinearLayoutManager(this));
        mListKids.setAdapter(mBirthContactsAdapter);
        onFilterTextChanged(new Date());
        mCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                onFilterTextChanged(firstDayOfNewMonth);
            }
        });
    }

    private void loadContacts() {
        getDataManager().getAllContacts().observe(this, list -> {
            if(list == null) return;
            List<Contact> contacts = new ArrayList<>(list);
            Collections.sort(contacts, (contact, contact1) -> contact.getBirthDay().compareTo(contact1.getBirthDay()));
            mBirthContactsAdapter.setData(contacts);
            onFilterTextChanged(mLastDate);
        });
    }

    private void onFilterTextChanged(Date date) {
        if(isDestroyed()) return;
        mLastDate = date;
        Calendar month = Calendar.getInstance();
        month.setTime(date);
        mTextMonth.setText(DateUtils.getMonthWithYear(getResources(), month));
        mBirthContactsAdapter.getFilter().filter(DateUtils.toString(date, "MM"));
    }

    @OnClick({R.id.button_prev, R.id.button_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_prev:
                mCalendarView.showPreviousMonth();
                break;
            case R.id.button_next:
                mCalendarView.showNextMonth();
                break;
        }
    }

    @Override
    public void onItemClicked(final int position, final ActivityOptionsCompat options) {
        startActivity(ContactDetailsActivity.getLaunchIntent(this, mBirthContactsAdapter.getItem(position)));
    }

    @Override
    public void onItemsCheckedChanged(final List<String> contacts) {
    }
}
