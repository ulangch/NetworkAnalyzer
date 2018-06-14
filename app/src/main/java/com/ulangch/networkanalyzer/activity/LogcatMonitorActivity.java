package com.ulangch.networkanalyzer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ulangch.networkanalyzer.R;
import com.ulangch.networkanalyzer.monitor.LogcatMonitor.LogcatMonitorConfiguration;
import com.ulangch.networkanalyzer.service.AnalyzerService;
import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;
import com.ulangch.networkanalyzer.service.AnalyzerServiceCallback;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

/**
 * Created by xyzc on 18-6-14.
 */

public class LogcatMonitorActivity extends PreferenceActivity implements ServiceConnection
        , Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "LogcatMonitorActivity";

    private static final String KEY_LOGCAT_RUN = "preference_logcat_run";
    private static final String KEY_FILE_STORAGE = "preference_logcat_cofigure_store";

    private static final int UHDL_START_MONITOR_RESULT = 0x01;
    private static final int UHDL_STOP_MONITOR_RESULT = 0x02;

    private SwitchPreference mLogcatMonitorRunPreference;
    private Preference mFileStoragePreference;

    private AnalyzerServiceBinder mAnalyzerService;

    private UiHandler mUiHandler;

    private volatile boolean mRefreshed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_logcat);
        mLogcatMonitorRunPreference = (SwitchPreference) findPreference(KEY_LOGCAT_RUN);
        mFileStoragePreference = findPreference(KEY_FILE_STORAGE);
        mLogcatMonitorRunPreference.setOnPreferenceClickListener(this);
        mUiHandler = new UiHandler(getMainLooper());
        Intent service = new Intent();
        service.setClass(getApplicationContext(), AnalyzerService.class);
        bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        mRefreshed = false;
        if (mAnalyzerService != null && mAnalyzerService.isBinderAlive()) {
            refresh();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mAnalyzerService != null && mAnalyzerService.isBinderAlive()) {
            mAnalyzerService.unRegisterCallback(mAnalyzerCallback);
            unbindService(this);
        }
        super.onDestroy();
    }

    private void refresh() {
        boolean running = mAnalyzerService.isLogcatMonitorRunning();
        mLogcatMonitorRunPreference.setChecked(running);
        mLogcatMonitorRunPreference.setSummary(running ? (R.string.running) : (R.string.closed));
        mRefreshed = true;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mAnalyzerService = (AnalyzerServiceBinder) service;
        mAnalyzerService.registerCallback(mAnalyzerCallback);
        refresh();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (TextUtils.equals(KEY_LOGCAT_RUN, key)) {
            if (((SwitchPreference) preference).isChecked()) {
                if (!mAnalyzerService.grantSuperPermission()) {
                    AnalyzerUtils.showToast(this, getString(R.string.info_no_super));
                    return false;
                }
                mLogcatMonitorRunPreference.setSummary(R.string.starting);
                mLogcatMonitorRunPreference.setEnabled(false);
                LogcatMonitorConfiguration conf = new LogcatMonitorConfiguration(
                        (mFileStoragePreference.getSummary()).toString());
                mAnalyzerService.runLogcatMonitor(conf);
            } else {
                mLogcatMonitorRunPreference.setSummary(R.string.stopping);
                mLogcatMonitorRunPreference.setEnabled(false);
                mAnalyzerService.stopLogcatMonitor();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }

    private class UiHandler extends Handler {
        UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            AnalyzerUtils.logd(TAG, "handleMessage: msg.what=" + msg.what
                    + ", msg.arg1=" + msg.arg1 + ", msg.arg2=" + msg.arg2
                    + ", msg.obj=" + msg.obj);
            switch (msg.what) {
                case UHDL_START_MONITOR_RESULT:
                    mLogcatMonitorRunPreference.setEnabled(true);
                    if (msg.arg1 != 1) {
                        mLogcatMonitorRunPreference.setChecked(false);
                        mLogcatMonitorRunPreference.setSummary(R.string.start_failed);
                        if (!TextUtils.isEmpty((String)msg.obj)) {
                            AnalyzerUtils.showToast(LogcatMonitorActivity.this, (String) msg.obj);
                        }
                    } else {
                        mLogcatMonitorRunPreference.setSummary(R.string.running);
                    }
                    break;
                case UHDL_STOP_MONITOR_RESULT:
                    mLogcatMonitorRunPreference.setEnabled(true);
                    if (msg.arg1 != 1) {
                        mLogcatMonitorRunPreference.setSummary(R.string.closed);
                    } else {
                        mLogcatMonitorRunPreference.setSummary(R.string.stop_failed);
                        if (!TextUtils.isEmpty((String) msg.obj)) {
                            AnalyzerUtils.showToast(LogcatMonitorActivity.this, (String) msg.obj);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private AnalyzerServiceCallback mAnalyzerCallback = new AnalyzerServiceCallback() {
        @Override
        public void onLogcatMonitorStart(boolean running, String info) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(
                    UHDL_START_MONITOR_RESULT, running ? 1 : 0, 0, info));
        }

        @Override
        public void onLogcatMonitorStop(boolean running, String info) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(
                    UHDL_STOP_MONITOR_RESULT, running ? 1 : 0, 0, info));
        }
    };
}
