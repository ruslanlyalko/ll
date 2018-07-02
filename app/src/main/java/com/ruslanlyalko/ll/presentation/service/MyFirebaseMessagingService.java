package com.ruslanlyalko.ll.presentation.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.data.FirebaseUtils;
import com.ruslanlyalko.ll.presentation.ui.main.dialogs.details.DialogActivity;
import com.ruslanlyalko.ll.presentation.ui.main.expenses.ExpensesActivity;
import com.ruslanlyalko.ll.presentation.ui.reminder.ReminderShowActivity;
import com.ruslanlyalko.ll.presentation.ui.splash.SplashActivity;

import java.util.Map;
import java.util.Random;

import static com.ruslanlyalko.ll.presentation.service.NotificationType.COMMENT;
import static com.ruslanlyalko.ll.presentation.service.NotificationType.REMINDER;

/**
 * Created by Ruslan Lyalko
 * on 21.01.2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final String GROUP_REMINDERS_ID = "group_reminders_id";
    private static final String GROUP_REMINDERS_NAME = "Нагадування";
    private static final String GROUP_MESSAGES_ID = "group_private_id";
    private static final String GROUP_MESSAGES_NAME = "Повідомлення";
    private static final String GROUP_NOTIFICATIONS_ID = "group_users_id";
    private static final String GROUP_NOTIFICATIONS_NAME = "Сповіщення";
    private static final String CHANEL_COMMENT_ID = "chanel_comment_id";
    private static final String CHANEL_COMMENT_DESC = "Коментарі";
    private static final String CHANEL_EXPENSE_ID = "chanel_expense_id";
    private static final String CHANEL_EXPENSE_DESC = "Витрати";
    private static final String CHANEL_REMINDERS_ID = "chanel_reminders_id";
    private static final String CHANEL_REMINDERS_DESC = "Нагадування";
    private static final String CHANEL_LESSON_ID = "chanel_lesson_id";
    private static final String CHANEL_LESSON_DESC = "Заняття";
    private static final String CHANEL_DEFAULT_ID = "chanel_default_id";
    private static final String CHANEL_DEFAULT_DESC = "Інше";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> payload = remoteMessage.getData();
            showNotification(payload);
        }
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void showNotification(final Map<String, String> payload) {
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(payload.get("title"))
                .setContentText(payload.get("message"))
                .setTicker(payload.get("message"))
                .setSmallIcon(R.drawable.ic_stat_main)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setAutoCancel(true)
                .setVibrate(new long[]{0,
                        200, 200,
                        200, 300,
                        100, 100,
                        100, 100,
                        100, 100
                });
        Intent resultIntent;
        NotificationType notificationType = NotificationType.findByKey(payload.get("type"));
        String chanelId;
        switch (notificationType) {
            case COMMENT:
                resultIntent = DialogActivity.getLaunchIntent(this, payload.get("messageKey"));
                chanelId = CHANEL_COMMENT_ID;
                break;
            case EXPENSE:
                resultIntent = ExpensesActivity.getLaunchIntent(this);
                chanelId = CHANEL_EXPENSE_ID;
                break;
            case LESSON:
                resultIntent = SplashActivity.getLaunchIntent(this);
                chanelId = CHANEL_LESSON_ID;
                break;
            case REMINDER:
                resultIntent = ReminderShowActivity.getLaunchIntent(this, payload.get("message"));
                chanelId = CHANEL_REMINDERS_ID;
                break;
            default:
                resultIntent = SplashActivity.getLaunchIntent(this);
                chanelId = CHANEL_DEFAULT_ID;
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        if (notificationType.equals(REMINDER)) {
            builder.setDeleteIntent(pendingIntent);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerChanelAndGroups();
                builder.setBadgeIconType(Notification.BADGE_ICON_SMALL);
                builder.setNumber(1);
                builder.setChannelId(chanelId);
            }
            if (notificationType.equals(COMMENT)) {
                int pushId = payload.get("messageKey").hashCode();
                if (!payload.get("messageKey").equals(FirebaseUtils.getCurrentDialog()))
                    notificationManager.notify(pushId, builder.build());
            } else {
                notificationManager.notify(new Random().nextInt(), builder.build());
            }
        }
    }

    private void registerChanelAndGroups() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //group
            notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(GROUP_MESSAGES_ID,
                    GROUP_MESSAGES_NAME));
            notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(GROUP_NOTIFICATIONS_ID,
                    GROUP_NOTIFICATIONS_NAME));
            notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(GROUP_REMINDERS_ID,
                    GROUP_REMINDERS_NAME));
            //comment
            createNotificationChannel(CHANEL_COMMENT_ID,
                    CHANEL_COMMENT_DESC, CHANEL_COMMENT_DESC, GROUP_MESSAGES_ID);
            //expense
            createNotificationChannel(CHANEL_EXPENSE_ID,
                    CHANEL_EXPENSE_DESC, CHANEL_EXPENSE_DESC, GROUP_NOTIFICATIONS_ID);
            //lesson
            createNotificationChannel(CHANEL_LESSON_ID,
                    CHANEL_LESSON_DESC, CHANEL_LESSON_DESC, GROUP_NOTIFICATIONS_ID);
            //reminders
            createNotificationChannel(CHANEL_REMINDERS_ID,
                    CHANEL_REMINDERS_DESC, CHANEL_REMINDERS_DESC, GROUP_REMINDERS_ID);
            //default
            createNotificationChannel(CHANEL_DEFAULT_ID,
                    CHANEL_DEFAULT_DESC, CHANEL_DEFAULT_DESC, GROUP_NOTIFICATIONS_ID);
        }
    }

    public void createNotificationChannel(String id, String name, String desc, String groupId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            if (notificationManager.getNotificationChannel(id) != null) return;
            NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationChannel.setShowBadge(true);
            notificationChannel.setBypassDnd(true);
            notificationChannel.setVibrationPattern(new long[]{0, 200, 200, 200, 300, 100, 100, 100, 100, 100, 100});
            notificationChannel.setDescription(desc);
            notificationChannel.setGroup(groupId);
//        notificationChannel.setSound();
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
