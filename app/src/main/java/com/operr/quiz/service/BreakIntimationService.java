package com.operr.quiz.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.operr.quiz.BuildConfig;
import com.operr.quiz.MainActivity;
import com.operr.quiz.R;

import java.util.Timer;
import java.util.TimerTask;

public class BreakIntimationService extends Service {

    private int TIMER_DURATION;

    public static boolean SERVICE_ENABLED = false;

    private NotificationManagerCompat notificationManagerCompat;

    private final int NOTIFICATION_ID = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SERVICE_ENABLED = true;

        //initialize the timer duration
        TIMER_DURATION = BuildConfig.TIMER_DURATION_IN_MIN * 60;

        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManagerCompat.createNotificationChannel(notificationChannel);
        }

        startService();

        return START_STICKY;
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
         * when service destroy need change running flag.
         */
        SERVICE_ENABLED = false;
    }

    /**
     * Start foreground service
     */
    private void startService() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        /*
         * Setting pending intent for when user click notification need to go
         */
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        /*
         *Create th notification for foreground service
         */
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setContentTitle(getString(R.string.operr_driver))
                .setContentText(getString(R.string.on_break))
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification_icon))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setChannelId(getString(R.string.app_name));

        // Start foreground service
        startForeground(NOTIFICATION_ID, notification.build());

        //call timer initialize and stop method
        initiateTimer(notification);
    }

    /**
     * 1. Start the timer for set remaining time
     * 2. Stop the timer when time ended
     *
     * @param notification {@link NotificationCompat.Builder}
     */
    private void initiateTimer(final NotificationCompat.Builder notification) {
        final Timer timer = new Timer();

        /*
         * Create timer task for notify the remaining time and stop service
         */
        final TimerTask timerTask = new TimerTask() {
            public void run() {
                updateTimerAndStopServiceIfTimeEnded(notification, timer);
            }
        };
        // scheduling the timer for seconds.
        timer.schedule(timerTask, 0, 1000);
    }

    /**
     * Calculate remaining timer duration and notify notification
     *
     * @param notification {@link NotificationCompat.Builder}
     * @param timer        {@link Timer}
     */
    private void updateTimerAndStopServiceIfTimeEnded(NotificationCompat.Builder notification, Timer timer) {
        //decrease one second
        long remainingSeconds = TIMER_DURATION--;

        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds - minutes * 60;
        notification.setSubText(getString(R.string.time_remaining, minutes, seconds));
        notificationManagerCompat.notify(NOTIFICATION_ID, notification.build());
        if (seconds == 0 && minutes == 0)
            stopForegroundService(notification, timer);
    }

    /**
     * Stop foreground service and update notification content then make it closeable.
     *
     * @param notification {@link NotificationCompat.Builder}
     * @param timer        {@link Timer}
     */
    private void stopForegroundService(NotificationCompat.Builder notification, Timer timer) {
        stopForeground(true);
        stopSelf();
        notification.setOngoing(false);
        notification.setAutoCancel(true);
        notification.setContentText(getString(R.string.break_end));
        notification.setSubText(getString(R.string.time_end));
        notificationManagerCompat.notify(NOTIFICATION_ID, notification.build());
        timer.cancel();
    }
}
