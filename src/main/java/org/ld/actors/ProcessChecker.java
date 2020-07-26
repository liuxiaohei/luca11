package org.ld.actors;

import akka.actor.AbstractActor;
import org.ld.beans.CheckProcessStatus;
import org.ld.beans.ProcessInProgress;
import org.ld.beans.ProcessStatus;
import org.ld.utils.ZLogger;

public class ProcessChecker extends AbstractActor {
    private final ProcessExecutorAdapter processExecutorAdapter;

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    public ProcessChecker(ProcessExecutorAdapter processExecutorAdapter) {
        this.processExecutorAdapter = processExecutorAdapter;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CheckProcessStatus.class, message -> {
                    try {
                        ProcessStatus successMessage = processExecutorAdapter.checkProcessStatus(message.id);
                        LOG.info(message.id);
                        getSender().tell(successMessage, getSelf());
                    } catch (Throwable error) {
                        ProcessInProgress failureMessage = new ProcessInProgress();
                        getSender().tell(failureMessage, getSelf());
                    }
                })
                .build();
    }
}
