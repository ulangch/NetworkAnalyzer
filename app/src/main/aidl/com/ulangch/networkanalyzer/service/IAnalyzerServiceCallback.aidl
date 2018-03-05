// IAnalyzerServiceCallback.aidl
package com.ulangch.networkanalyzer.service;

// Declare any non-default types here with import statements

interface IAnalyzerServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);

    void onWechatXLogHacked(boolean hack);
    void onWechatXLogHackCheck(boolean hack);
    void onMessageDelayMonitorStatusChanged(boolean running);
}
