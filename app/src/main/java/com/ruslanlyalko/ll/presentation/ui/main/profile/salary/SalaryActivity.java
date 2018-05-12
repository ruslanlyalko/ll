package com.ruslanlyalko.ll.presentation.ui.main.profile.salary;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
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
    @BindView(R.id.text_total) TextSwitcher mTotalSwitcher;

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
        mTotalSwitcher.setInAnimation(in);
        mTotalSwitcher.setOutAnimation(out);
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

    private void loadLessons() {
        getDatabase().getReference(DC.DB_LESSONS)
                .child(DateUtils.toString(mCurrentMonth.getTime(), "yyyy/MM/dd"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        mLessons.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Lesson lesson = data.getValue(Lesson.class);
                            if (lesson != null) {
                                mLessons.add(lesson);
                            }
                        }
                        calcSalary();
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
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

    private void updateConditionUI() {
    }

    private void calcSalary() {}
}
