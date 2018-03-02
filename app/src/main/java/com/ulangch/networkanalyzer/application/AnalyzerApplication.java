package com.ulangch.networkanalyzer.application;

import android.app.Application;
import android.content.Intent;

import com.ulangch.networkanalyzer.service.AnalyzerService;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

/**
 * Created by xyzc on 18-3-1.
 */

public class AnalyzerApplication extends Application{
    private static final String TAG = "AnalyzerApplication";

    private Intent mServiceIntent;

    @Override
    public void onCreate() {
        AnalyzerUtils.logd(TAG, "onCreate");
        mServiceIntent = new Intent();
        mServiceIntent.setClass(AnalyzerApplication.this, AnalyzerService.class);
        startService(mServiceIntent);
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        AnalyzerUtils.logd(TAG, "onTerminate");
        stopService(mServiceIntent);
        super.onTerminate();
    }
}
