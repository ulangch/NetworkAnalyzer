package com.ulangch.networkanalyzer.monitor;

import android.text.TextUtils;
import android.util.Log;

import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by xyzc on 18-3-5.
 */

public class LogcatMonitor extends BaseMonitor{
    private static final String TAG = "LogcatMonitor";

    private LogcatMonitorConfiguration mLogcatConf;
    private boolean mMonitorRunning;
    private Map<String, Integer> mLogcatPids;
    public LogcatMonitor(AnalyzerServiceBinder service) {
        super(service);
        mLogcatPids = new HashMap<>();
    }

    @Override
    public boolean status() {
        return mMonitorRunning;
    }

    @Override
    public String monitor(MonitorConfiguration conf) {
        if (conf == null || !(conf instanceof LogcatMonitorConfiguration)) {
            return "Logcat monitor configuration is not valid";
        }
        mLogcatConf = new LogcatMonitorConfiguration((LogcatMonitorConfiguration) conf);
        if (mMonitorRunning == mLogcatConf.enable) {
            return "Packet monitor is already" + (mMonitorRunning ? " started" : " stopped");
        }
        if (!mLogcatConf.enable) {
            return stop();
        }
        if (!mAnalyzerService.grantSuperPermission()) {
            return "Packet monitor start failed because root permission denied";
        }
        setStoragePath(!TextUtils.isEmpty(conf.storagePath) ? conf.storagePath
                : (AnalyzerUtils.DEFAULT_FILE_STORAGE_PATH + "/logcat"));
        File logPath = new File(getStoragePath());
        if (logPath.exists() && logPath.isDirectory()) {
            File[] subFiles = logPath.listFiles();
            if (subFiles != null) {
            for (File sub : subFiles) {
                if (sub.exists() && sub.isFile()) {
                    sub.delete();
                }
            }
            }
        } else {
            logPath.mkdir();
        }
        stop();
        AnalyzerUtils.logd(TAG, "Ready to monitor logcat");
        if (mLogcatConf.systemLog && forkLogcatProcess("system", getStoragePath() + "/system.log") < 0) {
            //AnalyzerUtils.loge(TAG, "Monitor system log failed");
        }
        if (mLogcatConf.mainLog && forkLogcatProcess("main", getStoragePath() + "/main.log") < 0) {
            //AnalyzerUtils.loge(TAG, "Monitor main log failed");
        }
        if (mLogcatConf.eventLog && forkLogcatProcess("events", getStoragePath() + "/events.log") < 0) {
            //AnalyzerUtils.loge(TAG, "Monitor event log failed");
        }
        if (mLogcatConf.radioLog && forkLogcatProcess("radio", getStoragePath() + "/radio.log") < 0) {
            // AnalyzerUtils.loge(TAG, "Monitor radio log failed");
        }
        /*if (mLogcatConf.kernelLog) {
            String command = "cat /proc/kmsg > " + (getStoragePath() + "/kernel.log") + " &";
            int pid = -1;
            String pidStr = mSuperExecutor.execute(command, true);
            if (!TextUtils.isEmpty(pidStr)) {
                String[] array = pidStr.split(" ");
                if (array.length == 2) {
                    try {
                        pid = Integer.valueOf(array[1]);
                    } catch (NumberFormatException nfe) {
                        AnalyzerUtils.loge(TAG, "Exception occur when fork logcat process", nfe);
                    }
                }
            }
            AnalyzerUtils.logd(TAG, "Fork kmsg result: " + pidStr);
            if (pid > 0) {
                mLogcatPids.put("kmsg", pid);
            } else {
                AnalyzerUtils.loge(TAG, "Monitor kernel log failed");
            }
        }*/
        mMonitorRunning = true;
        return null;
    }

    private int forkLogcatProcess(String buffer, String file) {
        StringBuilder command = new StringBuilder();
        command.append("logcat -b ");
        command.append(buffer);
        command.append(" -v threadtime > ");
        command.append(file);
        command.append(" &");
        String pidStr = mSuperExecutor.execute(command.toString(), true);
        int pid = -1;
        if (!TextUtils.isEmpty(pidStr)) {
            String[] array = pidStr.split(" ");
            if (array.length == 2) {
                try {
                    pid = Integer.valueOf(array[1]);
                } catch (NumberFormatException nfe) {
                    AnalyzerUtils.loge(TAG, "Exception occur when fork logcat process", nfe);
                }
            }
        }
        if (pid > 0) {
            mLogcatPids.put(buffer, pid);
        }
        // AnalyzerUtils.logd(TAG, "Fork " + buffer + " result: " + pidStr);
        return pid;
    }

    @Override
    public String stop() {
        /*AnalyzerUtils.logd(TAG, "Start to kill : " + mLogcatPids);
        for (int pid : mLogcatPids.values()) {
            mSuperExecutor.superKillProcess(pid);
        }
        mLogcatPids.clear();*/
        Map<String, String> logProcs = mSuperExecutor.superGrepProcess("logcat");
        if (logProcs != null) {
            for (String pid : logProcs.keySet()) {
                mSuperExecutor.superKillProcess(pid);
            }
        }
        mMonitorRunning = false;
        return null;
    }

    public static class LogcatMonitorConfiguration extends MonitorConfiguration{
        public LogcatMonitorConfiguration() {}

        public LogcatMonitorConfiguration(String storagePath) {
            this.storagePath = storagePath;
        }
        public LogcatMonitorConfiguration(LogcatMonitorConfiguration source) {
            if (source != null) {
                enable = source.enable;
                storagePath =  source.storagePath;
                if (source.tags != null) {
                    tags = new HashSet<>(source.tags);
                }
                systemLog = source.systemLog;
                mainLog = source.mainLog;
                radioLog = source.radioLog;
                kernelLog = source.kernelLog;
                eventLog = source.eventLog;
            }
        }

        public Set<String> tags;
        public boolean systemLog = true;
        public boolean mainLog = true;
        public boolean radioLog = true;
        public boolean kernelLog = true;
        public boolean eventLog = true;
    }
}
