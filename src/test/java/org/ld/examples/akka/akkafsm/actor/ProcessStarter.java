package org.ld.examples.akka.akkafsm.actor;

import akka.actor.AbstractActor;
import org.ld.examples.akka.akkafsm.actor.message.ProcessFailure;
import org.ld.examples.akka.akkafsm.actor.message.ProcessStarted;
import org.ld.examples.akka.akkafsm.actor.message.StartProcess;
import org.ld.examples.akka.akkafsm.service.ProcessExecutorAdapter;
import org.ld.utils.LoggerUtil;
import org.slf4j.Logger;

public class ProcessStarter extends AbstractActor {
    private final ProcessExecutorAdapter processExecutorAdapter;

    private static final Logger LOG = LoggerUtil.newInstance();

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
