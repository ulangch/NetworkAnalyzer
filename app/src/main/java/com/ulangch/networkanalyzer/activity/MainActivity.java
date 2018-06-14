package com.ulangch.networkanalyzer.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.ulangch.networkanalyzer.R;
import com.ulangch.networkanalyzer.monitor.PacketMonitor;
import com.ulangch.networkanalyzer.service.AnalyzerService;
import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;
import com.ulangch.networkanalyzer.service.AnalyzerServiceCallback;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

public class MainActivity extends PreferenceActivity implements ServiceConnection
        , Preference.OnPreferenceClickListener {
    private static final String TAG = "MainActivity";

    private static final String PACKAGE_ROOT_CENTER = "com.miui.securitycenter";
    private static final String ACTIVITY_ROOT_CENTER = "com.miui.permcenter.MainAcitivty";

    private static final String KEY_GRANT_SUPER = "preference_super";
    private static final String KEY_HACK_WECHAT = "preference_xlog";
    private static final String KEY_LOGCAT_MONITOR = "preference_logcat";
    private static final String KEY_MESSAGE_DELAY_MONITOR = "preference_message_delay";
    private static final String KEY_TCPDUMP_MONITOR = "preference_tcpdump";
    private static final String KEY_TCP_CONNECTION = "preference_tcp_connection";

    private static final int UHDL_SUPER_PERMISSION_RESULT = 0x01;
    private static final int UHDL_HACK_WECHAT_RESULT = 0x02;
    private static final int UHDL_HACK_WECHAT_CHECK_RESULT = 0x03;

    private UiHandler mUiHandler;
    private AnalyzerServiceBinder mAnalyzerService;

    private Preference mSuperPreference;
    private Preference mWechatPreference;
    private Preference mLogcatPreference;
    private Preference mMessageDelayPreference;
    private Preference mTcpdumpPreference;
    private Preference mTcpConnPreference;

    private boolean mSuperGranted = false;
    private boolean mWechatHacked = false;
    private boolean mLogcatMonitorRunning = false;
    private boolean mMessageDelayMonitorRunning = false;
    private boolean mPacketMonitorRunning = false;
    private boolean mTcpConnMonitorRunning = false;

    private volatile boolean mRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_main);
        mSuperPreference = findPreference(KEY_GRANT_SUPER);
        mWechatPreference = findPreference(KEY_HACK_WECHAT);
        mLogcatPreference = findPreference(KEY_LOGCAT_MONITOR);
        mMessageDelayPreference = findPreference(KEY_MESSAGE_DELAY_MONITOR);
        mTcpdumpPreference = findPreference(KEY_TCPDUMP_MONITOR);
        mTcpConnPreference = findPreference(KEY_TCP_CONNECTION);
        mSuperPreference.setOnPreferenceClickListener(this);
        mWechatPreference.setOnPreferenceClickListener(this);
        mLogcatPreference.setOnPreferenceClickListener(this);
        mMessageDelayPreference.setOnPreferenceClickListener(this);
        mTcpdumpPreference.setOnPreferenceClickListener(this);
        mTcpConnPreference.setOnPreferenceClickListener(this);
        mUiHandler = new UiHandler(this.getMainLooper());
        Intent service = new Intent();
        service.setClass(getApplicationContext(), AnalyzerService.class);
        bindService(service, this, Context.BIND_AUTO_CREATE);
        grantNormalPermission();
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mAnalyzerService = (AnalyzerServiceBinder) service;
        mAnalyzerService.registerCallback(mAnalyzerCallback);
        refresh();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mAnalyzerService.unRegisterCallback(mAnalyzerCallback);
    }

    private void refresh() {
        grantSuperPermission();
        if (mSuperGranted) {
            mAnalyzerService.checkWechatXLogHacked();
            if (mLogcatMonitorRunning != mAnalyzerService.isLogcatMonitorRunning()) {
                mLogcatMonitorRunning = !mLogcatMonitorRunning;
                mLogcatPreference.setSummary(mLogcatMonitorRunning
                        ? (R.string.summary_logcat_monitor_running) : (R.string.summary_logcat_monitor));
            }
            if (mMessageDelayMonitorRunning != mAnalyzerService.isMessageDelayMonitorRunning()) {
                mMessageDelayMonitorRunning = !mMessageDelayMonitorRunning;
                mMessageDelayPreference.setSummary(mMessageDelayMonitorRunning
                        ? (R.string.summary_message_delay_running) : (R.string.summary_message_delay));
            }
            if (mPacketMonitorRunning != mAnalyzerService.isPacketMonitorRunning()) {
                mPacketMonitorRunning = !mPacketMonitorRunning;
                mTcpdumpPreference.setSummary(mPacketMonitorRunning
                        ? (R.string.summary_tcpdump_running) : (R.string.summary_tcpdump));
            }
            if (mTcpConnMonitorRunning != mAnalyzerService.isTcpConnMonitorRunning()) {
                mTcpConnMonitorRunning = !mTcpConnMonitorRunning;
                mTcpConnPreference.setSummary(mTcpConnMonitorRunning
                        ? (R.string.summary_tcp_connection_running) : (R.string.summary_tcp_connection));
            }
        }
        mRefreshed = true;
    }

    private void grantNormalPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void grantSuperPermission() {
        mSuperGranted = mAnalyzerService.grantSuperPermission();
        mUiHandler.sendMessage(mUiHandler.obtainMessage(UHDL_SUPER_PERMISSION_RESULT, mSuperGranted ? 1 : 0, 0));
    }

    private void handleGrantSuperPermissionResult(boolean granted) {
        if (granted) {
            mSuperPreference.setSummary(R.string.summary_super_granted);
            mSuperPreference.setEnabled(false);
        } else {
            mSuperPreference.setSummary(R.string.summary_grant_super);
            mSuperPreference.setEnabled(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dlg_title_grant_super_failed))
                    .setMessage(getString(R.string.dlg_msg_grant_super_failed))
                    .setPositiveButton(R.string.dlg_go_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName(PACKAGE_ROOT_CENTER
                                    , ACTIVITY_ROOT_CENTER));
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.create().show();
        }
    }

    private boolean ensureSuperGranted() {
        if (!mSuperGranted) {
            AnalyzerUtils.showToast(this, getString(R.string.info_no_super));
            return false;
        }
        return true;
    }

    private void hackWechatXLog() {
        if (ensureSuperGranted()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dlg_title_xlog_hack))
                    .setMessage(getString(R.string.dlg_msg_xlog_hack_warning))
                    .setPositiveButton(R.string.dlg_continue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mWechatPreference != null) {
                                mWechatPreference.setSummary(getString(R.string.summary_xlog_hack_running));
                                mWechatPreference.setEnabled(false);
                            }
                            dialogInterface.dismiss();
                            mAnalyzerService.hackWechatXLog();
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.create().show();
        }
    }

    private void handleHackWechatXLogResult(boolean isCheck, boolean hacked) {
        mWechatHacked = hacked;
        if (hacked) {
            mWechatPreference.setSummary(getString(R.string.summary_xlog_hack_hacked));
            mWechatPreference.setEnabled(false);
        } else {
            if (isCheck) {
                mWechatPreference.setSummary(getString(R.string.summary_xlog_hack));
            } else {
                mWechatPreference.setSummary(getString(R.string.summary_xlog_hack_failed));
            }
            mWechatPreference.setEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        Intent intent = new Intent();
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        if (KEY_GRANT_SUPER.equals(key)) {
            grantSuperPermission();
        } else if (KEY_HACK_WECHAT.equals(key)) {
            hackWechatXLog();
        } else if (KEY_LOGCAT_MONITOR.equals(key)){
            intent.setClass(this, LogcatMonitorActivity.class);
            startActivity(intent);
        } else if (KEY_MESSAGE_DELAY_MONITOR.equals(key)) {
            intent.setClass(this, MessageDelayActivity.class);
            startActivity(intent);
        } else if (KEY_TCPDUMP_MONITOR.equals(key)) {
            /*PacketMonitor.PacketMonitorConfiguration conf = new PacketMonitor.PacketMonitorConfiguration();
            conf.enable = true;
            conf.iface = "rmnet_data0";
            mAnalyzerService.runPacketMonitor(conf);*/
            intent.setClass(this, PacketMonitorActivity.class);
            startActivity(intent);
        } else if (KEY_TCP_CONNECTION.equals(key)) {

        }
        return true;
    }

    private class UiHandler extends Handler {
        UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UHDL_SUPER_PERMISSION_RESULT:
                    handleGrantSuperPermissionResult(msg.arg1 == 1);
                    break;
                case UHDL_HACK_WECHAT_RESULT:
                    handleHackWechatXLogResult(false, msg.arg1 == 1);
                    break;
                case UHDL_HACK_WECHAT_CHECK_RESULT:
                    handleHackWechatXLogResult(true, msg.arg1 == 1);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private AnalyzerServiceCallback mAnalyzerCallback = new AnalyzerServiceCallback() {
        @Override
        public void onWechatXLogHacked(boolean hack) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(UHDL_HACK_WECHAT_RESULT, hack ? 1 : 0, 0));
        }

        @Override
        public void onWechatXLogHackCheck(boolean hack) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(UHDL_HACK_WECHAT_CHECK_RESULT, hack ? 1 : 0, 0));
        }
    };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
