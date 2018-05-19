package com.ruslanlyalko.ll.presentation.ui.alarm;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;
import com.ruslanlyalko.ll.presentation.ui.main.lesson.LessonActivity;

import java.io.IOException;

import butterknife.OnClick;

public class AlarmReceiverActivity extends BaseActivity {

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_alarm_receiver;
    }

    @Override
    protected void setupView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl;
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "wakeup");
            wl.acquire(10 * 60 * 1000L /*10 minutes*/);
            new Handler().postDelayed(wl::release, 10 * 60 * 1000);
        }
        shakeItBaby();
        playSound(this, getAlarmUri());
    }

    private void shakeItBaby() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator == null) return;
        //long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        if (Build.VERSION.SDK_INT >= 26) {
            mVibrator.vibrate(VibrationEffect.createOneShot(120 * 1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            mVibrator.vibrate(120 * 1000);
        }
    }

    private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null && audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println("OOPS");
        }
    }

    //Get an alarm sound. Try for an alarm. If none set, try notification,
    //Otherwise, ringtone.
    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }

    @OnClick(R.id.button_stop_alarm)
    public void onButtonStopClicked() {
        mMediaPlayer.stop();
        mVibrator.cancel();
        startActivity(LessonActivity.getLaunchIntent(this));
        finishAffinity();
    }
}
