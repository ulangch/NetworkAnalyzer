package com.ulangch.networkanalyzer.service;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.ulangch.networkanalyzer.executor.ExecutorFactory;
import com.ulangch.networkanalyzer.executor.SuperExecutor;
import com.ulangch.networkanalyzer.hacker.XLogHacker;
import com.ulangch.networkanalyzer.monitor.MessageDelayMonitor;
import com.ulangch.networkanalyzer.monitor.PacketMonitor;
import com.ulangch.networkanalyzer.monitor.TcpConnectionMonitor;
import com.ulangch.networkanalyzer.monitor.MessageDelayMonitor.MessageDelayMonitorConfiguration;
import com.ulangch.networkanalyzer.monitor.PacketMonitor.PacketMonitorConfiguration;
import com.ulangch.networkanalyzer.monitor.TcpConnectionMonitor.TcpConnMonitorConfiguration;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyzc on 18-3-1.
 */

public class AnalyzerServiceBinder extends Binder {
    private static final String TAG = "AnalyzerServiceBinder";

    private static final int HDL_GRANT_SUPER_PERMISSION = 0x01;
    private static final int HDL_WECHAT_HACK = 0x02;
    private static final int HDL_WECHAT_HACK_CHECK = 0x03;
    private static final int HDL_START_PACKET_MONITOR = 0x04;
    private static final int HDL_START_TCP_CONNECTION_MONITOR = 0x05;
    private static final int HDL_START_MESSAGE_DELAY_MONITOR = 0x06;
    private static final int HDL_STOP_PACKET_MONITOR = 0x07;
    private static final int HDL_STOP_TCP_CONNECTION_MONITOR = 0x08;
    private static final int HDL_STOP_MESSAGE_DELAY_MONITOR = 0x09;

    private Context mContext;

    private List<AnalyzerServiceCallback> mCallbacks;
    private HandlerThread mWorkThread;
    private WorkHandler mWorkHandler;
    private SuperExecutor mSuperExecutor;
    private XLogHacker mXLogHacker;

    private PacketMonitor mPacketMonitor;
    private TcpConnectionMonitor mTcpConnMonitor;
    private MessageDelayMonitor mMessageDelayMonitor;

    private boolean mHasSuperPermission;

    public AnalyzerServiceBinder(Context ctx) {
        mContext = ctx;
        mWorkThread = new HandlerThread("AnalyzerService");
        mWorkThread.start();
        mWorkHandler = new WorkHandler(mWorkThread.getLooper());
        mCallbacks = new ArrayList<>();
        mSuperExecutor = ExecutorFactory.makeSuperExecutor();
        mXLogHacker = XLogHacker.makeXLogHacker(mSuperExecutor);

        mPacketMonitor = new PacketMonitor(this);
        mTcpConnMonitor = new TcpConnectionMonitor(this);
        mMessageDelayMonitor = MessageDelayMonitor.get(this);

        mWorkHandler.sendEmptyMessage(HDL_GRANT_SUPER_PERMISSION);
    }

    public boolean grantSuperPermission() {
        AnalyzerUtils.logd(TAG, "Grant super permission");
        if (!mHasSuperPermission) {
            mHasSuperPermission = mSuperExecutor.grantSuperPermission(mContext.getPackageCodePath());
        }
        AnalyzerUtils.logd(TAG, "Grant super permission: " + mHasSuperPermission);
        return mHasSuperPermission;
    }

    public void hackWechatXLog() {
        mWorkHandler.sendEmptyMessage(HDL_WECHAT_HACK);
    }

    public void checkWechatXLogHacked() {
        mWorkHandler.sendEmptyMessage(HDL_WECHAT_HACK_CHECK);
    }

    public void runMessageDelayMonitor(MessageDelayMonitorConfiguration conf) {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(HDL_START_MESSAGE_DELAY_MONITOR, conf));
    }

    public void stopMessageDelayMonitor() {
        mWorkHandler.sendEmptyMessage(HDL_STOP_MESSAGE_DELAY_MONITOR);
    }

    public boolean isMessageDelayMonitorRunning() {
        return mMessageDelayMonitor.status();
    }

    public void runPacketMonitor(PacketMonitorConfiguration conf) {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(HDL_START_PACKET_MONITOR, conf));
    }

    public void stopPacketMonitor() {
        mWorkHandler.sendEmptyMessage(HDL_STOP_PACKET_MONITOR);
    }

    public boolean isPacketMonitorRunning() {
        return mPacketMonitor.status();
    }

    public void runTcpConnMonitor(TcpConnMonitorConfiguration conf) {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(HDL_START_TCP_CONNECTION_MONITOR, conf));
    }

    public void stopTcpConnMonitor() {
        mWorkHandler.sendEmptyMessage(HDL_STOP_TCP_CONNECTION_MONITOR);
    }

    public boolean isTcpConnMonitorRunning() {
        return mTcpConnMonitor.status();
    }

    public void registerCallback(AnalyzerServiceCallback callback) {
        synchronized (mCallbacks) {
            if (mCallbacks.contains(callback)) {
                throw new IllegalArgumentException("Duplicated callback register!");
            }
            mCallbacks.add(callback);
        }
    }

    public void unRegisterCallback(AnalyzerServiceCallback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    public XLogHacker getXLogHacker() {
        return mXLogHacker;
    }

    public SuperExecutor getSuperExecutor() {
        return mSuperExecutor;
    }

    private class WorkHandler extends Handler {
        WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HDL_GRANT_SUPER_PERMISSION:
                    mHasSuperPermission = mSuperExecutor.grantSuperPermission(mContext.getPackageCodePath());
                    break;
                case HDL_WECHAT_HACK:
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onWechatXLogHacked(mXLogHacker.hackWechatXLog());
                        }
                    }
                    break;
                case HDL_WECHAT_HACK_CHECK:
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onWechatXLogHackCheck(mXLogHacker.checkXLogFileHacked());
                        }
                    }
                    break;
                case HDL_START_PACKET_MONITOR:
                    String startPacketMonitorResult = mPacketMonitor.monitor((PacketMonitorConfiguration) msg.obj);
                    AnalyzerUtils.logd("wuliang", "startPacketMonitorResult=" + startPacketMonitorResult);
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onPacketMonitorStart(mPacketMonitor.status(), startPacketMonitorResult);
                        }
                    }
                    break;
                case HDL_STOP_PACKET_MONITOR:
                    String stopPacketMonitorResult = mPacketMonitor.stop();
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onPacketMonitorStop(mPacketMonitor.status(), stopPacketMonitorResult);
                        }
                    }
                    break;
                case HDL_START_TCP_CONNECTION_MONITOR:
                    String startTcpConnMonitorResult = mTcpConnMonitor.monitor((TcpConnMonitorConfiguration) msg.obj);
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onTcpConnMonitorStart(mTcpConnMonitor.status(), startTcpConnMonitorResult);
                        }
                    }
                    break;
                case HDL_STOP_TCP_CONNECTION_MONITOR:
                    String stopTcpConnMonitorResult = mTcpConnMonitor.stop();
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onTcpConnMonitorStop(mTcpConnMonitor.status(), stopTcpConnMonitorResult);
                        }
                    }
                    break;
                case HDL_START_MESSAGE_DELAY_MONITOR:
                    String startMsgDelayMonitorResult = mMessageDelayMonitor.monitor((MessageDelayMonitorConfiguration) msg.obj);
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onMessageMonitorStart(mMessageDelayMonitor.status(), startMsgDelayMonitorResult);
                        }
                    }
                    break;
                case HDL_STOP_MESSAGE_DELAY_MONITOR:
                    String stopMsgDelayMonitorResult = mMessageDelayMonitor.stop();
                    synchronized (mCallbacks) {
                        for (AnalyzerServiceCallback callback : mCallbacks) {
                            callback.onMessageMonitorStop(mMessageDelayMonitor.status(), stopMsgDelayMonitorResult);
                        }
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
