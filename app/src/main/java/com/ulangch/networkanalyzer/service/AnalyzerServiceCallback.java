package com.ulangch.networkanalyzer.service;

/**
 * Created by xyzc on 18-3-5.
 */

public class AnalyzerServiceCallback {
    public void onWechatXLogHacked(boolean hack) {}
    public void onWechatXLogHackCheck(boolean hack) {}
    public void onLogcatMonitorStart(boolean running, String info) {}
    public void onLogcatMonitorStop(boolean running, String info) {}
    public void onMessageMonitorStart(boolean running, String info){}
    public void onMessageMonitorStop(boolean running, String info) {}
    public void onPacketMonitorStart(boolean running, String info) {}
    public void onPacketMonitorStop(boolean running, String info) {}
    public void onTcpConnMonitorStart(boolean running, String info) {}
    public void onTcpConnMonitorStop(boolean running, String info) {}
}
