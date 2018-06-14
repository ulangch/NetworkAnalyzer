package com.ulangch.networkanalyzer.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;

import com.ulangch.networkanalyzer.R;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

public class AnalyzerService extends Service {
    private static final String TAG = "AnalyzerService";

    private Binder mServiceBinder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AnalyzerUtils.logd(TAG, "onStartCommand");
        Notification n = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_running))
                .setSmallIcon(R.mipmap.ic_launcher).build();
        startForeground(Process.myPid(), n);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        AnalyzerUtils.logd(TAG, "onCreate");
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
            ((AnalyzerServiceBinder) mServiceBinder).onDestroy();
            mServiceBinder = null;
        }
        super.onDestroy();
    }

}
