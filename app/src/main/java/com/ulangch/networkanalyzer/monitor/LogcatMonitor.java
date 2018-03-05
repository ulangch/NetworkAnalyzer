package com.ulangch.networkanalyzer.monitor;

import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xyzc on 18-3-5.
 */

public class LogcatMonitor extends BaseMonitor{
    private static final String TAG = "LogcatMonitor";

    public LogcatMonitor(AnalyzerServiceBinder service) {
        super(service);
    }

    @Override
    public boolean status() {
        return false;
    }

    @Override
    public String monitor(MonitorConfiguration conf) {
        return null;
    }

    @Override
    public String stop() {
        return null;
    }

    public static class LogcatMonitorConfiguration extends MonitorConfiguration{
        public LogcatMonitorConfiguration() {}
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
