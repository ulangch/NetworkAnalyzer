package com.ulangch.networkanalyzer.activity;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ulangch.networkanalyzer.R;
import com.ulangch.networkanalyzer.monitor.PacketMonitor;
import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;

/**
 * Created by xyzc on 18-3-5.
 */

public class PacketMonitorActivity extends PreferenceActivity implements ServiceConnection, Preference.OnPreferenceChangeListener{
    private static final String TAG = "PacketMonitorActivity";
    private static final String KEY_PACKET_MONITOR_RUN = "preference_tcpdump_run";
    private static final String KEY_INTERFACE_CONFIGURE = "preference_tcpdump_configure_iface";
    private static final String KEY_FILE_NAME_CONFIGURE = "preference_tcpdump_configure_file";
    private static final String KEY_FILE_DIRECTORY_CONFIGURE = "preference_tcpdump_configure_directory";

    private SwitchPreference mPacketMonitorRunPreference;
    private ListPreference mIfacePreference;
    private EditTextPreference mFileNamePreference;
    private EditTextPreference mFileDirectoryPreference;

    private AnalyzerServiceBinder mAnalyzerService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_packet);
        mPacketMonitorRunPreference = (SwitchPreference) findPreference(KEY_PACKET_MONITOR_RUN);
        mIfacePreference = (ListPreference) findPreference(KEY_INTERFACE_CONFIGURE);
        mFileNamePreference = (EditTextPreference) findPreference(KEY_FILE_NAME_CONFIGURE);
        mFileDirectoryPreference = (EditTextPreference) findPreference(KEY_FILE_DIRECTORY_CONFIGURE);
        mPacketMonitorRunPreference.setOnPreferenceChangeListener(this);
        mIfacePreference.setOnPreferenceChangeListener(this);
        mFileNamePreference.setOnPreferenceChangeListener(this);
        mFileDirectoryPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        mFileDirectoryPreference.setSummary(PacketMonitor.PACKET_DIRECTORY_DEFAULT);
        super.onResume();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mAnalyzerService = (AnalyzerServiceBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();
        if (TextUtils.equals(KEY_PACKET_MONITOR_RUN, key)) {

        } else if (TextUtils.equals(KEY_INTERFACE_CONFIGURE, key)) {
            preference.setSummary((String) value);
        } else if (TextUtils.equals(KEY_FILE_NAME_CONFIGURE, key)) {
            preference.setSummary((String) value);
        } else if (TextUtils.equals(KEY_FILE_DIRECTORY_CONFIGURE, key)) {

        }
        return true;
    }

    private void handlePacketMonitorStatus(boolean running) {
    }
}
