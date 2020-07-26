package org.ld.actors;

import akka.actor.AbstractActor;
import org.ld.beans.ProcessFailure;
import org.ld.beans.ProcessStarted;
import org.ld.beans.StartProcess;
import org.ld.utils.ZLogger;

public class ProcessStarter extends AbstractActor {
    private final ProcessExecutorAdapter processExecutorAdapter;

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    public ProcessStarter(ProcessExecutorAdapter processExecutorAdapter) {
        this.processExecutorAdapter = processExecutorAdapter;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartProcess.class, message -> {
                    try {
                        String id = processExecutorAdapter.startProcess(message.processParam);
                        ProcessStarted successMessage = new ProcessStarted(id);
                        LOG.info(message.processParam);
                        getSender().tell(successMessage, getSelf());
                    } catch (Throwable error) {
                        ProcessFailure failureMessage = new ProcessFailure(error);
                        getSender().tell(failureMessage, getSelf());
                    }
                })
                .build();
    }
}
