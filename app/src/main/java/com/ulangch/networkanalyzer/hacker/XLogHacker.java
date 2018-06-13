package com.ulangch.networkanalyzer.hacker;

import android.os.Environment;

import com.ulangch.networkanalyzer.executor.SuperExecutor;
import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Created by xyzc on 18-2-27.
 */

public class XLogHacker{
    private static final String TAG = "XLogHacker";
    private static final String SEPERATOR_FILE = "/";
    private static final String PROC_NAME_WECHAT = "com.tencent.mm";
    private static final String DATA_DIR_WECHAT = "/data/data/com.tencent.mm";
    private static final String XLOG_FILE_WECHAT = "libwechatxlog.so";
    private static final String XLOG_FILE_LIB_WECHAT = "/data/data/com.tencent.mm/lib" + SEPERATOR_FILE + XLOG_FILE_WECHAT;
    private static final String XLOG_FILE_TINKER_WECHAT = "data/data/com.tencent.mm/tinker" + SEPERATOR_FILE + XLOG_FILE_WECHAT;
    private static final String XLOG_TEMP_HACK_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xlog_hack";
    private static final String XLOG_TEMP_ORIGIN_FILE = XLOG_TEMP_HACK_DIR + "/libwechatxlog.org";
    private static final String XLOG_TEMP_HACK_FILE = XLOG_TEMP_HACK_DIR + SEPERATOR_FILE + XLOG_FILE_WECHAT;
    private static final String XLOG_HEX_TO_HACK = "3100001a";
    private static final String XLOG_HEX_HACKED = "310000eb";
    private static final String XLOG_HEX_HACK = "eb";

    public static final int HACK_CODE_FAILED = 0x01;
    public static final int HACK_CODE_SUCCESS = 0x02;
    public static final int HACK_CODE_ALREADY = 0x03;
    public static final int HACK_CODE_NEED_REBOOT_WECHAT = 0x04;

    private SuperExecutor mSupperExecutor;

    private XLogHacker(SuperExecutor executor) {
        mSupperExecutor = executor;
    }

    public static XLogHacker makeXLogHacker(SuperExecutor executor) {
        return new XLogHacker(executor);
    }

    public boolean hackWechatXLog() {
        if (!copyXLogFileToTempDir()) {
            return false;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(new File(XLOG_TEMP_ORIGIN_FILE));
            fos = new FileOutputStream(new File(XLOG_TEMP_HACK_FILE));
            int len;
            int firstHackIndex;
            boolean hackReplace = false;
            byte[] bufferBytes = new byte[1024];
            byte[] bytesToHack = AnalyzerUtils.hexStringToBytes(XLOG_HEX_TO_HACK);
            byte[] bytesHacked = AnalyzerUtils.hexStringToBytes(XLOG_HEX_HACKED);
            byte byteHack = AnalyzerUtils.hexStringToByte(XLOG_HEX_HACK);
            while ((len = fis.read(bufferBytes)) > 0) {
                if ((firstHackIndex = AnalyzerUtils.byteArraySearch(bufferBytes, bytesToHack)) != -1) {
                    hackReplace = true;
                    bufferBytes[firstHackIndex + bytesToHack.length -1] = byteHack;
                } else if (AnalyzerUtils.byteArraySearch(bufferBytes, bytesHacked) != -1) {
                    AnalyzerUtils.logd(TAG, "XLog is already hacked");
                    return true;
                }
                fos.write(bufferBytes, 0, len);
            }
            fos.flush();
            if (!hackReplace) {
                AnalyzerUtils.loge(TAG, "Hack xlog failed");
                return false;
            }
        } catch (Exception e) {
            AnalyzerUtils.loge(TAG, "Exception occur while hack", e);
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {}
        }
        if (!mSupperExecutor.superCopyFile(XLOG_TEMP_HACK_FILE, XLOG_FILE_LIB_WECHAT)) {
            AnalyzerUtils.loge(TAG, "Copy hack xlog to mm lib xlog file failed");
            return false;
        }
        Set<String> otherXLogFile = mSupperExecutor.superGrepFile(DATA_DIR_WECHAT, XLOG_FILE_WECHAT);
        if (otherXLogFile != null && !otherXLogFile.isEmpty()) {
            for (String file : otherXLogFile) {
                if (!mSupperExecutor.superCopyFile(XLOG_TEMP_HACK_FILE, file)) {
                    AnalyzerUtils.loge(TAG, "Copy hack xlog to mm xlog file failed");
                    return false;
                }
            }
        }

        Map<String, String> wechatProcs = mSupperExecutor.superGrepProcess(PROC_NAME_WECHAT);
        if (wechatProcs != null && !wechatProcs.isEmpty()) {
            for (String procId : wechatProcs.keySet()) {
                if (!mSupperExecutor.superKillProcess(procId)){
                    AnalyzerUtils.loge(TAG, "Kill process [" + procId + ", " + wechatProcs.get(procId) + "] failed");
                }
            }
        }

        return true;
    }

    public boolean checkXLogFileHacked() {
        if (!copyXLogFileToTempDir()) {
            return false;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(XLOG_TEMP_ORIGIN_FILE));
            byte[] bufferBytes = new byte[1024];
            byte[] bytesHacked = AnalyzerUtils.hexStringToBytes(XLOG_HEX_HACKED);
            while (fis.read(bufferBytes) > 0) {
                if (AnalyzerUtils.byteArraySearch(bufferBytes, bytesHacked) != -1) {
                    AnalyzerUtils.logd(TAG, "XLog is already hacked");
                    return true;
                }
            }
        } catch (Exception e) {
            AnalyzerUtils.loge(TAG, "Exception occur while hack", e);
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {}
        }
        return false;
    }

    private boolean copyXLogFileToTempDir() {
        File xlogTempDir = AnalyzerUtils.createDir(XLOG_TEMP_HACK_DIR);
        if (xlogTempDir == null || !xlogTempDir.exists()) {
            AnalyzerUtils.loge(TAG, "Create xlog temp dir failed");
            return false;
        }
        File xlogTempOrginFile = new File(XLOG_TEMP_ORIGIN_FILE);
        if (xlogTempOrginFile.exists()) {
            xlogTempOrginFile.delete();
        }
        if (!mSupperExecutor.superCopyFile(XLOG_FILE_LIB_WECHAT, XLOG_TEMP_ORIGIN_FILE)) {
            AnalyzerUtils.loge(TAG, "Copy xlog to temp file failed");
            return false;
        }
        File xlogTempHackFile = AnalyzerUtils.createFile(XLOG_TEMP_HACK_FILE);
        if (xlogTempHackFile == null || !xlogTempHackFile.exists()) {
            AnalyzerUtils.loge(TAG, "Create xlog temp hack file failed");
        }
        return true;
    }
}
