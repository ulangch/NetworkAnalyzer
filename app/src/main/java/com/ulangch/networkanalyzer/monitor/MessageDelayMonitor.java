package com.ulangch.networkanalyzer.monitor;

import android.text.TextUtils;

import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xyzc on 18-3-5.
 */

public class MessageDelayMonitor extends BaseMonitor{
    private static final String TAG = "MessageDelayMonitor";

    public static final String MESSAGE_DELAY_DIRECTORY_DEFAULT = AnalyzerUtils.DEFAULT_FILE_STORAGE_PATH + "/" + "MessageDelayMonitor";

    private static final String PKG_WECHAT = "com.tencent.mm";
    private static final String PKG_MOBILE_QQ = "com.tencent.mobileqq";

    private static MessageDelayMonitor sSelf;

    private LogcatMonitor mLogcatMonitor;
    private PacketMonitor mPacketMonitor;
    private TcpConnectionMonitor mTcpConnMonitor;

    private MessageDelayMonitorConfiguration mMessageDelayConf;

    private Set<String> mInterestedApps;

    private boolean mMonitorRunning = false;

    private MessageDelayMonitor(AnalyzerServiceBinder service) {
        super(service);
        mLogcatMonitor = new LogcatMonitor(service);
        mPacketMonitor = new PacketMonitor(service);
        mTcpConnMonitor = new TcpConnectionMonitor(service);
    }

    public static MessageDelayMonitor get(AnalyzerServiceBinder service) {
        if (sSelf == null) {
            sSelf = new MessageDelayMonitor(service);
        }
        return sSelf;
    }

    @Override
    public boolean status() {
        return mMonitorRunning;
    }

    @Override
    public String monitor(MonitorConfiguration conf) {
        if (conf == null || !(conf instanceof MessageDelayMonitorConfiguration)) {
            return "Message delay monitor configuration is not valid";
        }
        mMessageDelayConf = new MessageDelayMonitorConfiguration((MessageDelayMonitorConfiguration) conf);
        if (mMonitorRunning == mMessageDelayConf.enable) {
            return "Message delay monitor is already" + (mMonitorRunning ? "started" : "stopped");
        }
        if (!mMessageDelayConf.enable) {
            return stop();
        }

        if (!mAnalyzerService.grantSuperPermission()) {
            return "Message delay monitor start failed because root permission denied";
        }

        if (!TextUtils.isEmpty(mMessageDelayConf.storagePath)) {
            setStoragePath(mMessageDelayConf.storagePath);
        } else if (getStoragePath() == null) {
            setStoragePath(MESSAGE_DELAY_DIRECTORY_DEFAULT);
        }
        AnalyzerUtils.ensureAnalyzerStorageDirectory();
        AnalyzerUtils.ensureDirectory(getStoragePath());

        if (mMessageDelayConf.interestedApps != null) {
            mInterestedApps = new HashSet<>(mMessageDelayConf.interestedApps);
            if (mInterestedApps.contains(PKG_WECHAT)) {
                if (!mAnalyzerService.getXLogHacker().checkXLogFileHacked()
                        && !mAnalyzerService.getXLogHacker().hackWechatXLog()) {
                    return "Message delay monitor start failed " +
                            "because an error occurred while hacking wechat";
                }
            }
            if (mInterestedApps.contains(PKG_MOBILE_QQ)) {
                // TODO: For mobile qq
            }
        }

        String result;

        if (mMessageDelayConf.logcatMonitorConf != null && mMessageDelayConf.logcatMonitorConf.enable) {
            mMessageDelayConf.logcatMonitorConf.storagePath = getStoragePath();
            result = mLogcatMonitor.monitor(mMessageDelayConf.logcatMonitorConf);
            if (!TextUtils.isEmpty(result)) {
                stop();
                return result;
            }
        }
        if (mMessageDelayConf.packetMonitorConf != null && mMessageDelayConf.packetMonitorConf.enable) {
            mMessageDelayConf.packetMonitorConf.storagePath = getStoragePath();
            result = mPacketMonitor.monitor(mMessageDelayConf.packetMonitorConf);
            if (!TextUtils.isEmpty(result)) {
                stop();
                return result;
            }
        }

        if (mMessageDelayConf.tcpConnMonitorConf != null && mMessageDelayConf.tcpConnMonitorConf.enable) {
            mMessageDelayConf.tcpConnMonitorConf.storagePath = getStoragePath();
            result = mTcpConnMonitor.monitor(mMessageDelayConf.tcpConnMonitorConf);
            if (!TextUtils.isEmpty(result)) {
                stop();
                return result;
            }
        }
        mMonitorRunning = true;
        return null;
    }

    @Override
    public String stop() {
        mMonitorRunning = false;
        mMessageDelayConf = null;
        mTcpConnMonitor.stop();
        mPacketMonitor.stop();
        mLogcatMonitor.stop();
        return null;
    }

    public static class MessageDelayMonitorConfiguration extends MonitorConfiguration{

        public MessageDelayMonitorConfiguration() {}

        public MessageDelayMonitorConfiguration(MessageDelayMonitorConfiguration source) {
            if (source != null) {
                enable = source.enable;
                storagePath = source.storagePath;
                if (source.interestedApps != null) {
                    interestedApps = new HashSet<>(source.interestedApps);
                }
                if (source.logcatMonitorConf != null) {
                    logcatMonitorConf = new LogcatMonitor.LogcatMonitorConfiguration(source.logcatMonitorConf);
                }
                if (source.packetMonitorConf != null) {
                    packetMonitorConf = new PacketMonitor.PacketMonitorConfiguration(source.packetMonitorConf);
                }
                if (source.tcpConnMonitorConf != null) {
                    tcpConnMonitorConf = new TcpConnectionMonitor.TcpConnMonitorConfiguration(source.tcpConnMonitorConf);
                }
            }
        }

        public Set<String> interestedApps;
        public LogcatMonitor.LogcatMonitorConfiguration logcatMonitorConf;
        public PacketMonitor.PacketMonitorConfiguration packetMonitorConf;
        public TcpConnectionMonitor.TcpConnMonitorConfiguration tcpConnMonitorConf;
    }
}
