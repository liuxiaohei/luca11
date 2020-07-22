package org.ld.examples.java8;

import lombok.Data;

import java.util.concurrent.Future;

@Data
public class FutureThreadBean<T> {
    private Future<T> fucture;
    private Thread thread;

    public FutureThreadBean() {
        thread = Thread.currentThread();
    }
}
