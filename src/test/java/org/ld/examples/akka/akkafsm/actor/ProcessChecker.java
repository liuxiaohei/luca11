package org.ld.examples.akka.akkafsm.actor;

import akka.actor.AbstractActor;
import org.ld.examples.akka.akkafsm.actor.message.CheckProcessStatus;
import org.ld.examples.akka.akkafsm.actor.message.ProcessInProgress;
import org.ld.examples.akka.akkafsm.actor.message.ProcessStatus;
import org.ld.examples.akka.akkafsm.service.ProcessExecutorAdapter;
import org.ld.utils.LoggerUtil;
import org.slf4j.Logger;

public class ProcessChecker extends AbstractActor {
    private final ProcessExecutorAdapter processExecutorAdapter;

    private static final Logger LOG = LoggerUtil.newInstance();

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
