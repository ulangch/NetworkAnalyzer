package com.ulangch.networkanalyzer.service;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.ulangch.networkanalyzer.executor.ExecutorFactory;
import com.ulangch.networkanalyzer.executor.SuperExecutor;
import com.ulangch.networkanalyzer.hacker.XLogHacker;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyzc on 18-3-1.
 */

public class AnalyzerServiceBinder extends IAnalyzerServiceBinder.Stub{
    private static final String TAG = "AnalyzerServiceBinder";

    private static final int HDL_GRANT_SUPER_PERMISSION = 0x01;
    private static final int HDL_WECHAT_HACK = 0x02;
    private static final int HDL_WECHAT_HACK_CHECK = 0x03;

    private Context mContext;

    private List<IAnalyzerServiceCallback> mCallbacks;
    private HandlerThread mWorkThread;
    private WorkHandler mWorkHandler;
    private SuperExecutor mSuperExecutor;
    private XLogHacker mXLogHacker;
    private boolean mHasSuperPermission;

    public AnalyzerServiceBinder(Context ctx) {
        mContext = ctx;
        mWorkThread = new HandlerThread("AnalyzerService");
        mWorkThread.start();
        mWorkHandler = new WorkHandler(mWorkThread.getLooper());
        mCallbacks = new ArrayList<>();
        mSuperExecutor = ExecutorFactory.makeSuperExecutor();
        mXLogHacker = XLogHacker.makeXLogHacker(mSuperExecutor);
        mWorkHandler.sendEmptyMessage(HDL_GRANT_SUPER_PERMISSION);
    }

    @Override
    public boolean grantSuperPermission() {
        AnalyzerUtils.logd(TAG, "Grant super permission");
        if (!mHasSuperPermission) {
            mHasSuperPermission = mSuperExecutor.grantSuperPermission(mContext.getPackageCodePath());
        }
        AnalyzerUtils.logd(TAG, "Grant super permission: " + mHasSuperPermission);
        return mHasSuperPermission;
    }

    @Override
    public void hackWechatXLog() {
        mWorkHandler.sendEmptyMessage(HDL_WECHAT_HACK);
    }

    @Override
    public void checkWechatXLogHacked() {
        mWorkHandler.sendEmptyMessage(HDL_WECHAT_HACK_CHECK);
    }

    @Override
    public void registerCallback(IAnalyzerServiceCallback callback) {
        synchronized (mCallbacks) {
            if (mCallbacks.contains(callback)) {
                throw new IllegalArgumentException("Duplicated callback register!");
            }
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unRegisterCallback(IAnalyzerServiceCallback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
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
                        try {
                            for (IAnalyzerServiceCallback callback : mCallbacks) {
                                callback.onWechatXLogHacked(mXLogHacker.hackWechatXLog());
                            }
                        } catch (RemoteException e) {
                        }
                    }
                    break;
                case HDL_WECHAT_HACK_CHECK:
                    synchronized (mCallbacks) {
                        try {
                            for (IAnalyzerServiceCallback callback : mCallbacks) {
                                callback.onWechatXLogHackCheck(mXLogHacker.checkXLogFileHacked());
                            }
                        } catch (RemoteException e) {
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
