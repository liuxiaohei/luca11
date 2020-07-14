package org.ld.uc;

import java.io.Serializable;

@FunctionalInterface
public interface UCRunnable extends Serializable {
    void run() throws Throwable;
}
