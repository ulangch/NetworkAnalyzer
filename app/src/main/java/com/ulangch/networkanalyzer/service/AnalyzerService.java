package com.ulangch.networkanalyzer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

/**
 * Created by xyzc on 18-2-28.
 */

public class AnalyzerService extends Service{
    private static final String TAG = "AnalyzerService";

    private Binder mServiceBinder;
    private Notification mNoticication;
    private NotificationManager mNotificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AnalyzerUtils.logd(TAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        /*AnalyzerUtils.logd(TAG, "onCreate");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Analyzer Service")
                .setContentText("Analyzer service is running")
                .setSmallIcon(R.mipmap.ic_launcher);
        mNoticication = builder.build();*/
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        AnalyzerUtils.logd(TAG, "onBind");
        if (mServiceBinder == null) {
            mServiceBinder = new AnalyzerServiceBinder(getApplicationContext());
        }
        return mServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AnalyzerUtils.logd(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        AnalyzerUtils.logd(TAG, "onDestroy");
        if (mServiceBinder != null) {
            mServiceBinder = null;
        }
        super.onDestroy();
    }

}
