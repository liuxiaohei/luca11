package org.ld.beans;

public final class ProcessFailure implements ProcessStatus {
    public final Throwable error;

    public ProcessFailure(Throwable error) {
        this.error = error;
    }
}
