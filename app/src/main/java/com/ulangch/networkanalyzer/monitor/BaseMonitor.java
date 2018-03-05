package com.ulangch.networkanalyzer.monitor;

import android.os.Parcelable;

import com.ulangch.networkanalyzer.executor.SuperExecutor;
import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;

/**
 * Created by xyzc on 18-3-5.
 */

public abstract class BaseMonitor {
    private String mStoragePath = null;
    private String mOwnerMessage = null;

    public AnalyzerServiceBinder mAnalyzerService;
    public SuperExecutor mSuperExecutor;

    public BaseMonitor() {}

    public BaseMonitor(AnalyzerServiceBinder service) {
        mAnalyzerService = service;
        mSuperExecutor = mAnalyzerService.getSuperExecutor();
    }

    public abstract boolean status();

    /**
     * @return Error message
     */
    public abstract String monitor(MonitorConfiguration configuration);

    public abstract String stop();

    public void setStoragePath(String path) {
        mStoragePath = path;
    }

    public String getStoragePath() {
        return mStoragePath;
    }

    public static class MonitorConfiguration{
        public boolean enable;
        public String storagePath;
    }

}
