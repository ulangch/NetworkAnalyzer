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
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ulangch.networkanalyzer.R;
import com.ulangch.networkanalyzer.monitor.PacketMonitor;
import com.ulangch.networkanalyzer.monitor.PacketMonitor.PacketMonitorConfiguration;
import com.ulangch.networkanalyzer.service.AnalyzerService;
import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;
import com.ulangch.networkanalyzer.service.AnalyzerServiceCallback;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

/**
 * Created by xyzc on 18-3-5.
 */

public class PacketMonitorActivity extends PreferenceActivity implements ServiceConnection
        , Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = "PacketMonitorActivity";
    private static final String KEY_PACKET_MONITOR_RUN = "preference_tcpdump_run";
    private static final String KEY_INTERFACE_CONFIGURE = "preference_tcpdump_configure_iface";
    private static final String KEY_FILE_NAME_CONFIGURE = "preference_tcpdump_configure_file";
    private static final String KEY_FILE_DIRECTORY_CONFIGURE = "preference_tcpdump_configure_directory";

    private static final int UHDL_START_MONITOR_RESULT = 0x01;
    private static final int UHDL_STOP_MONITOR_RESULT = 0x02;

    private SwitchPreference mPacketMonitorRunPreference;
    private ListPreference mIfacePreference;
    private EditTextPreference mFileNamePreference;
    private EditTextPreference mFileDirectoryPreference;

    private AnalyzerServiceBinder mAnalyzerService;

    private UiHandler mUiHandler;

    private volatile boolean mRefresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_packet);
        mPacketMonitorRunPreference = (SwitchPreference) findPreference(KEY_PACKET_MONITOR_RUN);
        mIfacePreference = (ListPreference) findPreference(KEY_INTERFACE_CONFIGURE);
        mFileNamePreference = (EditTextPreference) findPreference(KEY_FILE_NAME_CONFIGURE);
        mFileDirectoryPreference = (EditTextPreference) findPreference(KEY_FILE_DIRECTORY_CONFIGURE);
        mPacketMonitorRunPreference.setOnPreferenceClickListener(this);
        mIfacePreference.setOnPreferenceChangeListener(this);
        mFileNamePreference.setOnPreferenceChangeListener(this);
        mFileDirectoryPreference.setOnPreferenceChangeListener(this);
        mUiHandler = new UiHandler(this.getMainLooper());
        Intent service = new Intent();
        service.setClass(getApplicationContext(), AnalyzerService.class);
        bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        mIfacePreference.setSummary((!TextUtils.isEmpty(mIfacePreference.getValue())
                ? mIfacePreference.getValue() : PacketMonitor.PACKET_DIRECTORY_DEFAULT));
        mFileNamePreference.setSummary((!TextUtils.isEmpty(mFileNamePreference.getText())
                ? mFileNamePreference.getText() : PacketMonitor.PACKET_FILE_NAME_DEFAULT));
        mFileDirectoryPreference.setSummary(PacketMonitor.PACKET_DIRECTORY_DEFAULT);

        mRefresh = false;
        if (mAnalyzerService != null && mAnalyzerService.isBinderAlive()) {
            refresh();
        }
        super.onResume();
    }

    private void refresh() {
        boolean running = mAnalyzerService.isPacketMonitorRunning();
        mPacketMonitorRunPreference.setChecked(running);
        enableConfigurePreference(!running);
        mRefresh = true;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mAnalyzerService = (AnalyzerServiceBinder) service;
        if (!mRefresh) {
            refresh();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();
        if (TextUtils.equals(KEY_INTERFACE_CONFIGURE, key)) {
            preference.setSummary((String) value);
        } else if (TextUtils.equals(KEY_FILE_NAME_CONFIGURE, key)) {
            preference.setSummary((String) value);
        } else if (TextUtils.equals(KEY_FILE_DIRECTORY_CONFIGURE, key)) {

        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (TextUtils.equals(KEY_PACKET_MONITOR_RUN, key)) {
            if (((SwitchPreference) preference).isChecked()) {
                if (!mAnalyzerService.grantSuperPermission()) {
                    AnalyzerUtils.showToast(this, getString(R.string.info_no_super));
                    return false;
                }
                mPacketMonitorRunPreference.setSummary(R.string.starting);
                mPacketMonitorRunPreference.setEnabled(false);
                enableConfigurePreference(false);
                PacketMonitorConfiguration conf = new PacketMonitorConfiguration();
                conf.iface = String.valueOf(mIfacePreference.getSummary());
                conf.fileName = String.valueOf(mFileNamePreference.getSummary());
                AnalyzerUtils.logd("wuliang", "fileName=" + conf.fileName);
                mAnalyzerService.runPacketMonitor(conf);
            } else {
                mPacketMonitorRunPreference.setSummary(R.string.stopping);
                mPacketMonitorRunPreference.setEnabled(false);
                mAnalyzerService.stopPacketMonitor();
            }
        }
        return true;
    }

    private void enableConfigurePreference(boolean enable) {
        mIfacePreference.setEnabled(enable);
        mFileNamePreference.setEnabled(enable);
    }

    private class UiHandler extends Handler {
        UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            AnalyzerUtils.logd(TAG, "handleMessage: msg.what=" + msg.what
                    + ", msg.arg1=" + msg.arg1 + ", msg.obj=" + msg.obj.toString());
            boolean running = msg.arg1 == 1;
            String info = (String) msg.obj;
            switch (msg.what) {
                case UHDL_START_MONITOR_RESULT:
                    mPacketMonitorRunPreference.setEnabled(true);
                    if (!running) {
                        mPacketMonitorRunPreference.setChecked(false);
                        mPacketMonitorRunPreference.setSummary(R.string.start_failed);
                        enableConfigurePreference(true);
                        if (!TextUtils.isEmpty(info)) {
                            AnalyzerUtils.showToast(PacketMonitorActivity.this, info);
                        }
                    } else {
                        mPacketMonitorRunPreference.setSummary(R.string.running);
                    }
                    break;
                case UHDL_STOP_MONITOR_RESULT:
                    mPacketMonitorRunPreference.setEnabled(true);
                    if (!running) {
                        mPacketMonitorRunPreference.setSummary(R.string.closed);
                        enableConfigurePreference(true);
                    } else {
                        mPacketMonitorRunPreference.setSummary(R.string.stop_failed);
                        if (!TextUtils.isEmpty(info)) {
                            AnalyzerUtils.showToast(PacketMonitorActivity.this, info);
                        }
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private AnalyzerServiceCallback mAnalyzerCallback = new AnalyzerServiceCallback() {
        @Override
        public void onPacketMonitorStart(boolean running, String info) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(UHDL_START_MONITOR_RESULT, running ? 1 : 0, 0, info));
        }

        @Override
        public void onPacketMonitorStop(boolean running, String info) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(UHDL_STOP_MONITOR_RESULT, running ? 1 : 0, 0, info));
        }
    };
}
