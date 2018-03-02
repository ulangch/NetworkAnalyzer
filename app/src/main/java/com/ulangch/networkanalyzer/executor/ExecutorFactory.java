package com.ulangch.networkanalyzer.executor;

/**
 * Created by xyzc on 18-2-28.
 */

public class ExecutorFactory {

    public static NormalExecutor makeNormalExecutor() {
        return NormalExecutor.make();
    }

    public static SuperExecutor makeSuperExecutor() {
        return SuperExecutor.make();
    }
}
