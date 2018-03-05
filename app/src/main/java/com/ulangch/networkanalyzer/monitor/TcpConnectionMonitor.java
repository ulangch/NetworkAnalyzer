package com.ulangch.networkanalyzer.monitor;

import com.ulangch.networkanalyzer.service.AnalyzerServiceBinder;

/**
 * Created by xyzc on 18-3-5.
 */

public class TcpConnectionMonitor extends BaseMonitor{
    private static final String TAG = "TcpConnectionMonitor";

    public TcpConnectionMonitor(AnalyzerServiceBinder service) {
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

    public static class TcpConnMonitorConfiguration extends MonitorConfiguration{
        public TcpConnMonitorConfiguration() {}
        public TcpConnMonitorConfiguration(TcpConnMonitorConfiguration source) {
            enable = source.enable;
            storagePath = source.storagePath;
        }
    }
}
