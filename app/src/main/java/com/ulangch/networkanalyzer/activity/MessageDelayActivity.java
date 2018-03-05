package com.ulangch.networkanalyzer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ulangch.networkanalyzer.R;

/**
 * Created by ulangch on 18-3-4.
 */

public class MessageDelayActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener
        , Preference.OnPreferenceChangeListener{
    private static final String TAG = "MessageDelayActivity";

    private static final String KEY_MESSAGE_DELAY_RUN = "preference_message_delay_run";
    private static final String KEY_FILE_STORAGE = "preference_message_delay_configure_store";
    private static final String KEY_INTERESTED_APP = "preference_message_delay_configure_app";
    private static final String KEY_PACKET_MONITOR = "preference_message_delay_configure_tcpdump";
    private static final String KEY_TCP_CONN_MONITOR = "preference_message_delay_configure_tcp_connection";
    private static final String KEY_LOGCAT_MONITOR = "preference_message_delay_configure_log";

    private SwitchPreference mMessageDelayRunPreference;
    private Preference mFileStoragePreference;
    private Preference mInterestedAppPreference;
    private CheckBoxPreference mPacketMonitorPreference;
    private CheckBoxPreference mTcpConnectionPreference;
    private CheckBoxPreference mLogcatMonitorPreference;

    private UiHandler mUiHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_message);
        mMessageDelayRunPreference = (SwitchPreference) findPreference(KEY_MESSAGE_DELAY_RUN);
        mFileStoragePreference = findPreference(KEY_FILE_STORAGE);
        mInterestedAppPreference = findPreference(KEY_INTERESTED_APP);
        mPacketMonitorPreference = (CheckBoxPreference) findPreference(KEY_PACKET_MONITOR);
        mTcpConnectionPreference = (CheckBoxPreference) findPreference(KEY_TCP_CONN_MONITOR);
        mLogcatMonitorPreference = (CheckBoxPreference) findPreference(KEY_LOGCAT_MONITOR);
        mMessageDelayRunPreference.setOnPreferenceClickListener(this);
        mUiHandler = new UiHandler(this.getMainLooper());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (TextUtils.equals(KEY_MESSAGE_DELAY_RUN, key)) {
            handleMessageDelayMonitorStatus(((SwitchPreference) preference).isChecked());
        } else if (TextUtils.equals(KEY_FILE_STORAGE, key)) {

        } else if (TextUtils.equals(KEY_INTERESTED_APP, key)) {

        } else if (TextUtils.equals(KEY_PACKET_MONITOR, key)) {

        } else if (TextUtils.equals(KEY_TCP_CONN_MONITOR, key)) {

        } else if (TextUtils.equals(KEY_LOGCAT_MONITOR, key)) {

        }
        return false;
    }

    private void handleMessageDelayMonitorStatus(boolean running) {
        mFileStoragePreference.setEnabled(!running);
        mInterestedAppPreference.setEnabled(!running);
        mPacketMonitorPreference.setEnabled(!running);
        mTcpConnectionPreference.setEnabled(!running);
        mLogcatMonitorPreference.setEnabled(!running);
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
            super.handleMessage(msg);
        }
    }
}
