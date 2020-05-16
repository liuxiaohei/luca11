package org.ld.examples.akka.akkafsm.service;

import org.ld.examples.akka.akkafsm.actor.message.ProcessFinished;
import org.ld.examples.akka.akkafsm.actor.message.ProcessInProgress;
import org.ld.examples.akka.akkafsm.actor.message.ProcessStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class ProcessExecutorAdapter {

    private static class Process {
        public final long createTime;
        public final long duration;

        public Process(long createTime, long duration) {
            this.createTime = createTime;
            this.duration = duration;
        }
    }

    private final Map<String, Process> processMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     */
    public String startProcess(String processParam) {
        String id = String.valueOf(idGenerator.incrementAndGet());
        processMap.put(id, new Process(System.currentTimeMillis(), Long.parseLong(processParam)));
        return id;
    }

    /**
     */
    public ProcessStatus checkProcessStatus(String id) {
        Process process = processMap.getOrDefault(id, null);
        if (process == null) {
            return new ProcessFinished();
        }
        if (process.createTime + process.duration > System.currentTimeMillis()) {
            return new ProcessInProgress();
        } else {
            processMap.remove(id);
            return new ProcessFinished();
        }
    }
}
