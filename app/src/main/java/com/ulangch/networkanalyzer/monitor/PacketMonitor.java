package com.ulangch.networkanalyzer.monitor;

import android.content.Context;
import android.text.TextUtils;

import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

/**
 * Created by xyzc on 18-3-5.
 */

public class PacketMonitor extends BaseMonitor{
    private static final String TAG = "PacketMonitor";

    public static final String PACKET_FILE_NAME_DEFAULT = "tcpdump.pcap";

    public static final String PACKET_DIRECTORY_DEFAULT =AnalyzerUtils.DEFAULT_FILE_STORAGE_PATH + "/" + "PacketMonitor";

    private boolean mMonitorRunning = false;
    private PacketMonitorConfiguration mPacketConf;

    private int mSubProcess;

    public PacketMonitor(AnalyzerServiceBinder service) {
        super(service);
    }

    @Override
    public boolean status() {
        return mMonitorRunning;
    }

    @Override
    public String monitor(MonitorConfiguration conf) {
        if (conf == null || !(conf instanceof PacketMonitorConfiguration)) {
            return "Packet monitor configuration is not valid";
        }
        mPacketConf = new PacketMonitorConfiguration((PacketMonitorConfiguration) conf);
        if (mMonitorRunning == mPacketConf.enable) {
            return "Packet monitor is already" + (mMonitorRunning ? " started" : " stopped");
        }
        if (!mPacketConf.enable) {
            return stop();
        }
        if (!mAnalyzerService.grantSuperPermission()) {
            return "Packet monitor start failed because root permission denied";
        }
        if (!ensureTcpdumpToolExist()) {
            return "Packet monitor start failed because tcpdump tool is not exist";
        }
        if (!TextUtils.isEmpty(mPacketConf.storagePath)) {
            setStoragePath(mPacketConf.storagePath);
        } else if (getStoragePath() == null) {
            setStoragePath(PACKET_DIRECTORY_DEFAULT);
        }
        AnalyzerUtils.ensureAnalyzerStorageDirectory();
        AnalyzerUtils.ensureDirectory(getStoragePath());

        StringBuilder command = new StringBuilder();
        command.append("tcpdump ");
        command.append("-i ").append(!TextUtils.isEmpty(mPacketConf.iface) ? mPacketConf.iface : "any").append(" ");
        command.append("-w ").append(getStoragePath()).append("/").append(
                !TextUtils.isEmpty(mPacketConf.fileName) ? mPacketConf.fileName : PACKET_FILE_NAME_DEFAULT);
        command.append(" &");
        AnalyzerUtils.logd("wuliang", "command=" + command.toString());

        String executeResult = mSuperExecutor.execute(command.toString(), true);
        AnalyzerUtils.logd("wuliang", "executeResult=" + executeResult);

        //TODO: KILL TCPDUMP http://bbs.csdn.net/topics/100176432
        return null;
    }

    @Override
    public String stop() {
        return null;
    }

    private boolean ensureTcpdumpToolExist() {
        // TODO: ENSURE
        return true;
    }

    public static class PacketMonitorConfiguration extends MonitorConfiguration{
        public PacketMonitorConfiguration() {}
        public PacketMonitorConfiguration(PacketMonitorConfiguration source) {
            if (source != null) {
                enable = source.enable;
                storagePath = source.storagePath;
                iface = source.iface;
                fileName = source.fileName;
            }
        }

        public String iface;
        public String fileName;
    }
}
