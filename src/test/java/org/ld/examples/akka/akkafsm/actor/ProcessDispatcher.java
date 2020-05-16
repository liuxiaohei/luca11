package org.ld.examples.akka.akkafsm.actor;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import org.ld.examples.akka.akkafsm.actor.message.*;
import org.ld.examples.akka.akkafsm.actor.state.ProcessData;
import org.ld.examples.akka.akkafsm.actor.state.ProcessState;
import org.ld.utils.LoggerUtil;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Collections;

import static org.ld.examples.akka.akkafsm.actor.state.ProcessState.*;

public class ProcessDispatcher extends AbstractFSM<ProcessState, ProcessData> {

    private static final Logger LOG = LoggerUtil.newInstance();

    public ProcessDispatcher(
            ProcessState initState,
            ProcessData initData,
            ActorRef processStarter,
            ActorRef processChecker
    ) {
        startWith(initState, initData);

        when(CREATED, Duration.ofSeconds(0), matchEvent(
                Collections.singletonList(StateTimeout()), ProcessData.class, (message, data) -> goTo(STARTING)));

        when(STARTING, matchEvent(
                ProcessStarted.class,
                (message, data) -> {
                    LOG.info("STARTING message.id {}", message.id);
                    return goTo(CHECKING_STATE).using(data.setId(message.id));
                }));

        when(STARTING, matchEvent(
                ProcessFailure.class,
                (message, data) -> goTo(STARTING_FAILURE)));

        when(STARTING_FAILURE, Duration.ofSeconds(5), matchEvent(
                Collections.singletonList(StateTimeout()), ProcessData.class, (message, data) -> goTo(STARTING)));

        when(CHECKING_STATE, matchEvent(
                ProcessFinished.class,
                (message, data) -> {
                    LOG.info("data.id {}", data.id);
                    return stop();
                }));

        when(CHECKING_STATE, matchEvent(
                ProcessFailure.class,
                (message, data) -> {
                    LOG.error("data.id {} message.error {}", data.id, message.error);
                    return stop();
                }));

        when(CHECKING_STATE, matchEvent(
                ProcessInProgress.class,
                (message, data) -> {
                    LOG.info("data.id {}", data.id);
                    return goTo(EXECUTING);
                }));

        when(EXECUTING, Duration.ofSeconds(5),
                matchEvent(Collections.singletonList(StateTimeout()), ProcessData.class,
                        (message, data) -> goTo(CHECKING_STATE)));

        onTransition(matchState(CREATED, STARTING, () -> {
            LOG.info(CREATED.name() + "2" + STARTING);
            processStarter.tell(new StartProcess(stateData().processParam, stateData().executorParam), getSelf());
        })
                .state(STARTING, CHECKING_STATE, () -> {
                    LOG.info(STARTING.name() + "2" + CHECKING_STATE);
                    processChecker.tell(new CheckProcessStatus(stateData().id), getSelf());
                })
                .state(EXECUTING, CHECKING_STATE, () -> {
                    LOG.info(EXECUTING.name() + "2" + CHECKING_STATE);
                    processChecker.tell(new CheckProcessStatus(stateData().id), getSelf());
                })
                .state(STARTING_FAILURE, STARTING, () -> {
                    LOG.info(STARTING_FAILURE.name() + "2" + STARTING);
                    processStarter.tell(new StartProcess(stateData().processParam, stateData().executorParam), getSelf());
                }));

        initialize();
    }
}
