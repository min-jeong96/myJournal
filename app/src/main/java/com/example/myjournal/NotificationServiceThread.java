package com.example.myjournal;

import android.os.Handler;

public class NotificationServiceThread extends Thread {
    Handler handler;
    boolean isRun = true;

    public NotificationServiceThread(Handler handler) {
        this.handler = handler;
    }

    public void stopForever() {
        synchronized(this) {
            this.isRun = false;
        }
    }

    public void run() {
        while(isRun) {
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(10000);
            } catch (Exception e) {}
        }
    }
}
