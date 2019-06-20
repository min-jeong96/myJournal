package com.example.myjournal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class NotificationService extends Service {
    NotificationManager notificationManager;
    NotificationServiceThread thread;
    Notification.Builder notification_at_night;
    Notification.Builder notification_at_morning;

    int setSleepHour, setSleepMinute, setWakeHour, setWakeMinute;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        thread.stopForever();
        thread = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new NotificationServiceThread(handler);
        thread.start();

        setSleepHour    = intent.getIntExtra("SLEEP HOUR", 0);
        setSleepMinute  = intent.getIntExtra("SLEEP MINUTE", 0);
        setWakeHour     = intent.getIntExtra("WAKE HOUR", 0);
        setWakeMinute   = intent.getIntExtra("WAKE MINUTE", 0);

        return super.onStartCommand(intent, flags, startId);
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = new Intent(NotificationService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification_at_night = new Notification.Builder(getApplicationContext());
            notification_at_night.setContentIntent(pendingIntent);
            notification_at_night.setSmallIcon(R.drawable.daily_journal_icon);
            notification_at_night.setWhen(setSleepHour*1000*60*60 + (setSleepMinute-30)*1000*60);
            notification_at_night.setContentTitle("오늘 하루에 대해 기록하셨나요?");
            notification_at_night.setContentText("자기 전에 오늘의 기록을 확인하고 입력해주세요.");
            notification_at_night.setAutoCancel(true);
            notification_at_night.setAutoCancel(true);
            notification_at_night.setCategory(Notification.CATEGORY_MESSAGE);
            notification_at_night.setPriority(Notification.PRIORITY_HIGH);
            notification_at_night.setVisibility(Notification.VISIBILITY_PUBLIC);
            notification_at_night.setOngoing(true);

            notification_at_morning = new Notification.Builder(getApplicationContext());
            notification_at_morning.setContentIntent(pendingIntent);
            notification_at_morning.setSmallIcon(R.drawable.daily_journal_icon);
            notification_at_morning.setWhen(setWakeHour*1000*60*60 + (setWakeMinute+30)*1000*60);
            notification_at_morning.setContentTitle("오늘 하루를 계획해보세요!");
            notification_at_morning.setContentText("오늘의 기상시간을 기록하고 계획을 정리하고 확인해주세요.");
            notification_at_morning.setAutoCancel(true);
            notification_at_morning.setCategory(Notification.CATEGORY_MESSAGE);
            notification_at_morning.setPriority(Notification.PRIORITY_HIGH);
            notification_at_morning.setVisibility(Notification.VISIBILITY_PUBLIC);
            notification_at_morning.setOngoing(true);

            notificationManager.notify(0, notification_at_night.build());
            notificationManager.notify(1, notification_at_morning.build());
        }
    }
}
