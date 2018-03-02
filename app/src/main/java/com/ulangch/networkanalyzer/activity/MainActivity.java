package com.ulangch.networkanalyzer.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ulangch.networkanalyzer.R;
import com.ulangch.networkanalyzer.service.AnalyzerService;
import com.ulangch.networkanalyzer.service.IAnalyzerServiceBinder;
import com.ulangch.networkanalyzer.service.IAnalyzerServiceCallback;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

public class MainActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener{
    private static final String TAG = "MainActivity";

    private static final String PACKAGE_ROOT_CENTER = "com.miui.securitycenter";
    private static final String ACTIVITY_ROOT_CENTER = "com.miui.permcenter.MainAcitivty";


    private static final int UHDL_SUPER_PERMISSION_RESULT = 0x01;
    private static final int UHDL_HACK_WECHAT_RESULT = 0x02;
    private static final int UHDL_HACK_WECHAT_CHECK_RESULT = 0x03;

    private UiHandler mUiHandler;
    private IAnalyzerServiceBinder mAnalyzerService;

    private Button mSuperGrantButton;
    private Button mHackWechatButton;
    private Button mNetworkMonitorButton;
    private Button mTcpdumpButton;
    private Button mConnectionMonitorButton;

    private boolean mSuperGranted = false;
    private boolean mWechatHacked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSuperGrantButton = (Button) findViewById(R.id.btn_grant_super);
        mHackWechatButton = (Button) findViewById(R.id.btn_hack_wechat);
        mNetworkMonitorButton = (Button) findViewById(R.id.btn_network_monitor);
        mTcpdumpButton = (Button) findViewById(R.id.btn_capture_tcpdump);
        mConnectionMonitorButton = (Button) findViewById(R.id.btn_connection_monitor);
        mHackWechatButton.setOnClickListener(this);
        mNetworkMonitorButton.setOnClickListener(this);
        mTcpdumpButton.setOnClickListener(this);
        mConnectionMonitorButton.setOnClickListener(this);
        mUiHandler = new UiHandler(this.getMainLooper());
        Intent service = new Intent();
        service.setClass(getApplicationContext(), AnalyzerService.class);
        bindService(service, this, Context.BIND_AUTO_CREATE);
        grantNormalPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mAnalyzerService = IAnalyzerServiceBinder.Stub.asInterface(service);
        grantSuperPermission();
        try {
            mAnalyzerService.registerCallback(mAnalyzerCallback);
            if (mSuperGranted) {
                mAnalyzerService.checkWechatXLogHacked();
            }
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        try {
            mAnalyzerService.unRegisterCallback(mAnalyzerCallback);
        } catch (RemoteException e) {
        }
    }

    private void grantNormalPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void grantSuperPermission() {
        try {
            mSuperGranted = mAnalyzerService.grantSuperPermission();
        } catch (RemoteException e) {
            AnalyzerUtils.loge(TAG, "Exception occur while grant super permission", e);
            mSuperGranted = false;
        }
        mUiHandler.sendMessage(mUiHandler.obtainMessage(UHDL_SUPER_PERMISSION_RESULT, mSuperGranted ? 1 : 0, 0));
    }

    private void handleGrantSuperPermissionResult(boolean granted) {
        if (granted) {
            mSuperGrantButton.setText(getString(R.string.super_granted));
            mSuperGrantButton.setTextColor(getColor(R.color.colorPrimary));
            mSuperGrantButton.setEnabled(false);
        } else {
            mSuperGrantButton.setText(getString(R.string.grant_super));
            mSuperGrantButton.setTextColor(getColor(R.color.colorRed));
            mSuperGrantButton.setEnabled(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.title_grant_super_failed))
                    .setMessage(getString(R.string.msg_grant_super_failed))
                    .setPositiveButton(R.string.go_now, new DialogInterface.OnClickListener() {
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
                    .setTitle(getString(R.string.xlog_hack_dlg_title))
                    .setMessage(getString(R.string.xlog_hack_dlg_warning))
                    .setPositiveButton(R.string.dlg_continue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mHackWechatButton != null) {
                                mHackWechatButton.setText(getString(R.string.xlog_hack_running));
                                mHackWechatButton.setEnabled(false);
                            }
                            dialogInterface.dismiss();
                            try {
                                mAnalyzerService.hackWechatXLog();
                            } catch (RemoteException e) {
                                AnalyzerUtils.loge(TAG, "Exception occur while hack wechat xlog", e);
                                mUiHandler.sendMessage(mUiHandler.obtainMessage(UHDL_HACK_WECHAT_RESULT, 0, 0));
                            }
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.create().show();
        }
    }

    private void handleHackWechatXLogResult(boolean isCheck, boolean hacked) {
        if (hacked) {
            mHackWechatButton.setText(getString(R.string.xlog_hack_hacked));
            mHackWechatButton.setTextColor(getColor(R.color.colorPrimary));
            mHackWechatButton.setEnabled(false);
        } else {
            if (isCheck) {
                mHackWechatButton.setText(getString(R.string.xlog_hack));
            } else {
                mHackWechatButton.setText(getString(R.string.xlog_hack_failed));
                mHackWechatButton.setTextColor(getColor(R.color.colorRed));
            }
            mHackWechatButton.setEnabled(true);
            mHackWechatButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_grant_super:
                grantSuperPermission();
                break;
            case R.id.btn_hack_wechat:
                hackWechatXLog();
                break;
            case R.id.btn_network_monitor:
                break;
            case R.id.btn_capture_tcpdump:
                break;
            case R.id.btn_connection_monitor:
                break;
            default:
                break;
        }
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

    private IAnalyzerServiceCallback mAnalyzerCallback = new IAnalyzerServiceCallback.Stub() {
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
