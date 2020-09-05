package org.ld.beans;

public final class StartProcess {
    public final String processParam;
    public final String executorParam;

    public StartProcess(String processParam, String executorParam) {
        this.processParam = processParam;
        this.executorParam = executorParam;
    }
}
