package com.ulangch.networkanalyzer.executor;

/**
 * Created by xyzc on 18-2-28.
 */

public class NormalExecutor extends BaseExecutor{
    private static final String TAG = "NormalExecutor";

    protected NormalExecutor(String shell) {
        super(TAG, shell);
    }

    protected static NormalExecutor make() {
        return new NormalExecutor(SHELL_PATH_NORMAL);
    }
}
