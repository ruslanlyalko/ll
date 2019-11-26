package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.data.models.ContactRecharge;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.data.models.SettingsSalary;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.calendar.adapter.OnLessonClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.adapter.ContactRechargesAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.adapter.LessonsHeaderAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.adapter.OnContactRechargeClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.recharge_edit.RechargeEditActivity;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.edit.ContactEditActivity;
import com.ruslanlyalko.ll.presentation.ui.main.lesson.LessonActivity;
import com.ruslanlyalko.ll.presentation.utils.DateUtils;
import com.ruslanlyalko.ll.presentation.utils.Keys;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class ContactDetailsActivity extends BaseActivity implements OnLessonClickListener, OnContactRechargeClickListener {

    private static final int RC_LESSON = 1001;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.image_avatar) ImageView mImageAvatar;
    @BindView(R.id.text_sub_title) TextView mTextSubTitle;
    @BindView(R.id.text_phone1) TextView mTextPhone1;
    @BindView(R.id.text_phone2) TextView mTextPhone2;
    @BindView(R.id.text_balance) TextView mTextBalance;
    @BindView(R.id.card_phone2) CardView mCardPhone2;
    @BindView(R.id.card_balance) CardView mCardBalance;
    @BindView(R.id.text_description) TextView mTextDescription;
    @BindView(R.id.text_lesson_count) TextView mTextLessonCount;
    @BindView(R.id.list_lessons) RecyclerView mListLessons;
    @BindView(R.id.list_income) RecyclerView mListIncome;
    @BindView(R.id.text_user_name) TextView mTextUserName;
    @BindView(R.id.text_income_placeholder) TextView mTextIncomePlaceholder;
    @BindView(R.id.layout_phones) LinearLayout mLayoutPhones;
    @BindView(R.id.card_description) CardView mCardDescription;
    @BindView(R.id.text_month) TextView mTextMonth;
    @BindView(R.id.calendar_view) CompactCalendarView mCalendarView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.progress_bar_recharge) ProgressBar mProgressBarRecharge;

    private Contact mContact;
    private List<SettingsSalary> mSettingsSalary = new ArrayList<>();
    private LessonsHeaderAdapter mLessonsAdapter = new LessonsHeaderAdapter(this);
    private ContactRechargesAdapter mContactRechargesAdapter;
    private boolean mHasLessonsWithOtherTeachers;
    private int mTotalCharge = 0;
    private List<Lesson> mLessons = new ArrayList<>();
    private Date mCurrentDate;

    public static Intent getLaunchIntent(final Context launchIntent, final Contact contact) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, contact);
        return intent;
    }

    public static Intent getLaunchIntent(final Context launchIntent, final String contactKey) {
        Intent intent = new Intent(launchIntent, ContactDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_CONTACT_KEY, contactKey);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_details;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mContact = (Contact) bundle.getSerializable(Keys.Extras.EXTRA_ITEM_ID);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void setupView() {
        if (isDestroyed()) return;
        setupCalendar();
        setupRecycler();
        setupBalance();
        getDataManager().getAllSettingsSalary().observe(this, settingsSalaries -> mSettingsSalary = settingsSalaries);
        getDataManager().getAllContacts().observe(this, list -> mLessonsAdapter.setContacts(list));
        new Handler().postDelayed(this::loadLessons, 300);
        loadContactRecharges();
        getDB(DC.DB_CONTACTS)
                .child(mContact.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mContact = dataSnapshot.getValue(Contact.class);
                        showContactDetails();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
        showContactDetails();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                startActivity(ContactEditActivity.getLaunchIntent(this, mContact));
                break;
            case R.id.action_delete:
                removeCurrentContact();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCalendar() {
        mCalendarView.setUseThreeLetterAbbreviation(true);
        mCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        mCalendarView.displayOtherMonthDays(true);
        mTextMonth.setText(DateUtils.getMonthWithYear(getResources(), Calendar.getInstance()));
        mCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                mCurrentDate = dateClicked;
                showLessonsOnList();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(firstDayOfNewMonth);
                mTextMonth.setText(DateUtils.getMonthWithYear(getResources(), calendar));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(getCurrentUser().getIsAdmin());
        menu.findItem(R.id.action_edit).setVisible(getCurrentUser().getIsAdmin());
        return true;
    }


    private void setupBalance() {
        mCardBalance.setVisibility(getCurrentUser().getIsAdmin() ? View.VISIBLE : View.GONE);
    }

    private void loadContactRecharges() {
        getDB(DC.DB_CONTACTS_RECHARGE)
                .child(mContact.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        List<ContactRecharge> contactRecharges = new ArrayList<>();
                        mTotalCharge = 0;
                        for (DataSnapshot rechargesSS : dataSnapshot.getChildren()) {
                            ContactRecharge recharge = rechargesSS.getValue(ContactRecharge.class);
                            if (recharge != null) {
                                contactRecharges.add(recharge);
                                mTotalCharge += recharge.getPrice();
                            }
                        }
                        if (isDestroyed()) return;
                        mProgressBarRecharge.setVisibility(View.GONE);
                        mTextIncomePlaceholder.setVisibility(contactRecharges.isEmpty()
                                ? View.VISIBLE : View.GONE);
                        mContactRechargesAdapter.setData(contactRecharges);
                        calcBalance();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }

    private void removeCurrentContact() {
        if (mLessonsAdapter.getItemCount() != 0) {
            Toast.makeText(this, R.string.error_delete_contact, Toast.LENGTH_LONG).show();
            return;
        }
        if (mHasLessonsWithOtherTeachers) {
            Toast.makeText(this, R.string.error_delete_contact_other, Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_remove_contact_title)
                .setMessage(R.string.dialog_remove_contact_message)
                .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                    finish();
                    FirebaseDatabase.getInstance()
                            .getReference(DC.DB_CONTACTS)
                            .child(mContact.getKey()).removeValue();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void setupRecycler() {
        mContactRechargesAdapter = new ContactRechargesAdapter(this, getCurrentUser());
        mListLessons.setLayoutManager(new LinearLayoutManager(this));
        mListLessons.setAdapter(mLessonsAdapter);
        mListIncome.setLayoutManager(new LinearLayoutManager(this));
        mListIncome.setAdapter(mContactRechargesAdapter);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(mLessonsAdapter);
        mListLessons.addItemDecoration(headersDecor);
        mLessonsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });
    }

    private void showContactDetails() {
        if (isDestroyed()) return;
        if (mContact == null) {
            setTitle("");
            return;
        }
        setTitle("");
        mTextUserName.setText(mContact.getName());
        String subtitle = "";
        if (mContact.hasUser())
            subtitle = "[" + mContact.getUserName() + "] \n";
        if (mContact.getBirthDay().getTime() != mContact.getCreatedAt().getTime())
            subtitle += DateUtils.toString(mContact.getBirthDay(), "dd.MM.yyyy");
        subtitle += "\n" + mContact.getEmail();
        mTextSubTitle.setText(subtitle);
        mTextPhone1.setText(mContact.getPhone());
        mTextPhone2.setText(mContact.getPhone2());
        mCardPhone2.setVisibility(mContact.getPhone2() != null && !mContact.getPhone2().isEmpty() ? View.VISIBLE : View.GONE);
        mTextDescription.setText(mContact.getDescription());
        mTextDescription.setVisibility(mContact.getDescription() != null && !mContact.getDescription().isEmpty() ? View.VISIBLE : View.GONE);
        mTextBalance.setText(String.format(getString(R.string.hrn_d), (mContact.getTotalIncome() - mContact.getTotalExpense())));
        mTextBalance.setTextColor(ContextCompat.getColor(this, (mContact.getTotalIncome() - mContact.getTotalExpense()) < 0 ? R.color.colorPrimary : R.color.colorAccent));
    }

    private void loadLessons() {
        getDataManager().getAllLessons().observe(this, allLessons -> {
            mProgressBar.setVisibility(View.GONE);
            mHasLessonsWithOtherTeachers = false;
            mCalendarView.removeAllEvents();
            mLessons.clear();
            for (Lesson lesson : allLessons) {
                if (lesson != null && (lesson.getClients().contains(mContact.getKey()))) {
                    if (getCurrentUser().getIsAdmin() || lesson.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                        mLessons.add(lesson);
                        int color = ContextCompat.getColor(ContactDetailsActivity.this, R.color.colorPrimary);
                        long date = lesson.getDateTime().getTime();
                        String uId = lesson.getUserId();
                        mCalendarView.addEvent(
                                new Event(color, date, uId), true);
                    } else
                        mHasLessonsWithOtherTeachers = true;
                }
            }
            String count = String.format(Locale.US, "[%d]", mLessons.size());
            mTextLessonCount.setText(count);
            showLessonsOnList();
            calcBalance();
        });
    }

    private void showLessonsOnList() {
        if (mCurrentDate == null) {
            mLessonsAdapter.setData(mLessons);
        } else {
            mLessonsAdapter.setData(getLessonsForCurrentDate());
        }
    }

    private List<Lesson> getLessonsForCurrentDate() {
        List<Lesson> list = new ArrayList<>();
        for (Lesson lesson : mLessons) {
            if (DateUtils.dateEquals(mCurrentDate, lesson.getDateTime())) {
                list.add(lesson);
            }
        }
        return list;
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

    private void calcBalance() {
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
        for (Lesson lesson : mLessons) {
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
                            pairTotalIncome += currentSettings.getStudentPair();
                            break;
                        case 2:
                            groupTotalCount += 1;
                            groupTotal += currentSettings.getTeacherGroup();
                            groupTotalIncome += currentSettings.getStudentGroup();
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
                            pairTotalIncome += currentSettings.getStudentPair15();
                            break;
                        case 2:
                            groupTotalCount += 1;
                            groupTotal += currentSettings.getTeacherGroup15();
                            groupTotalIncome += currentSettings.getStudentGroup15();
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
                            pairTotalChildIncome += currentSettings.getStudentPairChild();
                            break;
                        case 2:
                            groupTotalChildCount += 1;
                            groupTotalChild += currentSettings.getTeacherGroupChild();
                            groupTotalChildIncome += currentSettings.getStudentGroupChild();
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
                            pairTotalChildIncome += currentSettings.getStudentPair15Child();
                            break;
                        case 2:
                            groupTotalChildCount += 1;
                            groupTotalChild += currentSettings.getTeacherGroup15Child();
                            groupTotalChildIncome += currentSettings.getStudentGroup15Child();
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
//        total = privateTotal + pairTotal + groupTotal + onlineTotal +
//                privateTotalChild + pairTotalChild + groupTotalChild + onlineTotalChild;
        totalIncome = privateTotalIncome + pairTotalIncome + groupTotalIncome + onlineTotalIncome +
                privateTotalChildIncome + pairTotalChildIncome + groupTotalChildIncome + onlineTotalChildIncome;
        //
//        iPrivate += privateTotalIncome + privateTotalChildIncome;
//        iPair += pairTotalIncome + pairTotalChildIncome;
//        iGroup += groupTotalIncome + groupTotalChildIncome;
//        iOnLine += onlineTotalIncome + onlineTotalChildIncome;
        mContact.setTotalIncome(mTotalCharge);
        mContact.setTotalExpense(totalIncome);
        mTextBalance.setText(String.format(getString(R.string.hrn_d), (mContact.getTotalIncome() - mContact.getTotalExpense())));
        mTextBalance.setTextColor(ContextCompat.getColor(this, (mContact.getTotalIncome() - mContact.getTotalExpense()) < 0 ? R.color.colorPrimary : R.color.colorAccent));
    }


    @OnClick(R.id.button_recharge)
    public void onViewClicked() {
        startActivity(RechargeEditActivity.getLaunchIntent(this, mContact.getKey()));
    }

    @OnClick({R.id.card_phone1, R.id.card_phone2})
    public void onViewClicked(View view) {
        Intent callIntent;
        switch (view.getId()) {
            case R.id.card_phone1:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone()));
                startActivity(callIntent);
                break;
            case R.id.card_phone2:
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhone2()));
                startActivity(callIntent);
                break;
        }
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
        if (getCurrentUser().getIsAdmin() || lesson.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailsActivity.this);
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
        getDB(DC.DB_LESSONS)
                .child(DateUtils.toString(lesson.getDateTime(), "yyyy/MM/dd"))
                .child(lesson.getKey()).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, R.string.toast_lesson_removed, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onRemoveClicked(final ContactRecharge contactRecharge) {
        getDB(DC.DB_CONTACTS_RECHARGE)
                .child(contactRecharge.getContactKey())
                .child(contactRecharge.getKey()).removeValue().addOnCompleteListener(task ->
                Snackbar.make(mListLessons, getString(R.string.toast_deleted), Snackbar.LENGTH_LONG).show());
    }

    @Override
    public void onEditClicked(final ContactRecharge contactRecharge) {
        startActivity(RechargeEditActivity.getLaunchIntent(this, contactRecharge));
    }

    @OnClick(R.id.layout_balance_header)
    public void onClick() {
        if (mListIncome.getVisibility() == View.VISIBLE)
            mListIncome.setVisibility(View.GONE);
        else
            mListIncome.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.layout_month)
    public void onMonthClick() {
        if (mCalendarView.getVisibility() == View.VISIBLE) {
            mCalendarView.setVisibility(View.GONE);
            mCurrentDate = null;
            showLessonsOnList();
        } else {
            mCalendarView.setVisibility(View.VISIBLE);
        }
    }
}
