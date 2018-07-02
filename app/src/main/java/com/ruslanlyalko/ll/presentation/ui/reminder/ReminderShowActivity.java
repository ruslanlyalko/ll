package com.ruslanlyalko.ll.presentation.ui.reminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.presentation.utils.Keys;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.splash.SplashActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class ReminderShowActivity extends BaseActivity {

    @BindView(R.id.text_message) TextView mTextMessage;
    private String mMessage;

    public static Intent getLaunchIntent(final Context launchIntent, String text) {
        Intent intent = new Intent(launchIntent, ReminderShowActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_TEXT, text);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_reminder_show;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMessage = bundle.getString(Keys.Extras.EXTRA_TEXT);
        }
    }

    @Override
    protected void setupView() {
        mTextMessage.setText(mMessage);
    }

    @OnClick(R.id.text_message)
    public void onClick() {
        startActivity(SplashActivity.getLaunchIntent(this));
    }
}
