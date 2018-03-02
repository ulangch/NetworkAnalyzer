package com.ulangch.networkanalyzer.executor;

import android.text.TextUtils;

import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by xyzc on 18-2-27.
 */

public class BaseExecutor {
    protected static final String SHELL_PATH_NORMAL = "/system/bin/sh";
    protected static final String SHELL_PATH_ROOT = "/system/xbin/su";

    private static final String EXECUTE_FAILED = "EXECUTE_FAILED";

    private String mTag;
    private String mShell;

    protected BaseExecutor(String tag, String shell) {
        mTag = tag;
        mShell = shell;
    }

    public static boolean success(String exeResult) {
        return TextUtils.isEmpty(exeResult) || !exeResult.contains(EXECUTE_FAILED);
    }

    public synchronized String execute(String cmd, boolean waitFor) {
        StringBuilder result = new StringBuilder();
        DataOutputStream dos = null;
        try {
            Process proc = Runtime.getRuntime().exec(mShell);
            dos = new DataOutputStream(proc.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            if (waitFor) {
                proc.waitFor();
                result.append(gatherExecuteError(proc));
                result.append(gatherExecuteResult(proc));
            }
        } catch (Exception e) {
            AnalyzerUtils.loge(mTag, "Exception occur while execute " + "\"" + cmd + "\"", e);
            result.setLength(0);
            result.append(EXECUTE_FAILED);
        } finally {
            // do not close the pipe
        }
        return result.toString();
    }

    private String gatherExecuteError(Process proc) {
        BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String line;
        StringBuilder out = new StringBuilder();
        try {
            while ((line = errReader.readLine()) != null) {
                out.append(line);
                out.append("\n");
            }
            if (!TextUtils.isEmpty(out.toString().trim())) {
                AnalyzerUtils.loge(mTag, "Process error: " + out.toString());
                out.append(EXECUTE_FAILED);
            }
        } catch (IOException e) {
            AnalyzerUtils.loge(mTag, "Exception occur while gather execute error");
        } finally {
            // do not close the pipe
        }
        return out.toString();
    }

    private String gatherExecuteResult(Process proc) {
        BufferedReader retReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        StringBuilder out = new StringBuilder();
        try {
            while ((line = retReader.readLine()) != null) {
                out.append(line);
                out.append("\n");
            }
            if (!TextUtils.isEmpty(out.toString().trim())) {
                AnalyzerUtils.loge(mTag, "Process output: " + out.toString());
            }
        } catch (IOException e) {
            AnalyzerUtils.loge(mTag, "Exception occur while gather execute output");
        } finally {
            // do not close the pipe
        }
        return out.toString();
    }
}
