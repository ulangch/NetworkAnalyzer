package com.ulangch.networkanalyzer.executor;

import android.os.Build;
import android.text.TextUtils;

import com.ulangch.networkanalyzer.utils.AnalyzerUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by xyzc on 18-2-28.
 */

public class SuperExecutor extends BaseExecutor{
    private static final String TAG = "SuperExecutor";

    protected SuperExecutor(String shell) {
        super(TAG, shell);
    }

    protected static SuperExecutor make() {
        return new SuperExecutor(SHELL_PATH_ROOT);
    }

    public boolean grantSuperPermission(String pkgPath) {
        return success(execute("chmod 777 " + pkgPath, true));
    }

    public boolean superCopyFile(String src, String dst) {
        StringBuilder copyFileCommand = new StringBuilder();
        copyFileCommand.append("cp ");
        copyFileCommand.append(src);
        copyFileCommand.append(" ");
        copyFileCommand.append(dst);
        return success(execute(copyFileCommand.toString(), true));
    }

    public boolean superKillProcess(String procId) {
        String killProcCommand = "kill " + procId;
        return success(execute(killProcCommand, true));
    }

    public Set<String> superGrepFile(String baseDir, String regex) {
        StringBuilder grepFileCommand = new StringBuilder();
        grepFileCommand.append("cd ");
        grepFileCommand.append(baseDir);
        grepFileCommand.append(" && ");
        grepFileCommand.append("find . -name ");
        grepFileCommand.append(regex);
        String exeResult = execute(grepFileCommand.toString(), true);
        Set<String> fileSet = null;
        if (success(exeResult)) {
            fileSet = new HashSet<>();
            for (String line : exeResult.split("\n")) {
                if (line.length() > 1) {
                    AnalyzerUtils.logd(TAG, "super grep file: " + (baseDir + line.substring(1, line.length())));
                    fileSet.add(baseDir + line.substring(1, line.length()));
                }
            }
        }
        return fileSet;
    }

    public Map<String, String> superGrepProcess(String regex) {
        StringBuilder grepProcCommand = new StringBuilder();
        grepProcCommand.append("ps ");
        if (Build.VERSION.SDK_INT >= 26) {
            grepProcCommand.append("-A ");
        }
        grepProcCommand.append("|grep ");
        grepProcCommand.append(regex);
        String exeResult = execute(grepProcCommand.toString(), true);
        Map<String, String> procResult = null;
        if (success(exeResult) && !TextUtils.isEmpty(exeResult)) {
            procResult = new HashMap<>();
            for (String line : exeResult.split("\n")) {
                int validCount = 0;
                String procId = null, procName = null;
                for (String ele : line.split(" ")) {
                    if (TextUtils.isEmpty(ele)) {
                        continue;
                    }
                    validCount++;
                    if (validCount == 2) {
                        procId = ele;
                    }
                    if (validCount == 9) {
                        procName = ele;
                    }
                }
                AnalyzerUtils.logd(TAG, "proId=" + procId + ", procName=" + procName);
                if (procId != null && procName != null) {
                    procResult.put(procId, procName);
                }
            }
        }
        return procResult;
    }
}
