// IAnalyzerServiceBinder.aidl
package com.ulangch.networkanalyzer.service;

// Declare any non-default types here with import statements
import com.ulangch.networkanalyzer.service.IAnalyzerServiceCallback;

interface IAnalyzerServiceBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);
    void registerCallback(IAnalyzerServiceCallback callback);
    void unRegisterCallback(IAnalyzerServiceCallback callback);
    boolean grantSuperPermission();
    void hackWechatXLog();
    void checkWechatXLogHacked();
    void runMessageDelayMonitor();
}
