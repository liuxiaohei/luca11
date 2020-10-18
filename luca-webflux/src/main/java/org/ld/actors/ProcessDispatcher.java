package org.ld.actors;

import akka.actor.AbstractFSM;
import lombok.extern.slf4j.Slf4j;
import org.ld.beans.*;
import org.ld.enums.ProcessState;

import java.time.Duration;
import java.util.Collections;

/**
 * https://blog.csdn.net/qq_35246620/article/details/70157794
 */
@Slf4j
public class ProcessDispatcher extends AbstractFSM<ProcessState, ProcessData> {

    public ProcessDispatcher(ProcessData initData) {

        startWith(ProcessState.CREATED, initData);

        when(ProcessState.CREATED, Duration.ofSeconds(0), matchEvent(
                Collections.singletonList(StateTimeout()), ProcessData.class, (message, data) -> goTo(ProcessState.STARTING)));

        when(ProcessState.STARTING, matchEvent(
                ProcessStarted.class,
                (message, data) -> {
                    log.info("STARTING message.id {}", message.id);
                    return goTo(ProcessState.CHECKING_STATE).using(data.setId(message.id));
                }));

        when(ProcessState.STARTING, matchEvent(
                ProcessFailure.class,
                (message, data) -> goTo(ProcessState.STARTING_FAILURE)));

        when(ProcessState.STARTING_FAILURE, Duration.ofSeconds(5), matchEvent(
                Collections.singletonList(StateTimeout()), ProcessData.class, (message, data) -> goTo(ProcessState.STARTING)));

        when(ProcessState.CHECKING_STATE, matchEvent(
                ProcessFinished.class,
                (message, data) -> {
                    log.info("data.id {}", data.id);
                    return stop();
                }));

        when(ProcessState.CHECKING_STATE, matchEvent(
                ProcessFailure.class,
                (message, data) -> {
                    log.error("data.id {} message.error {}", data.id, message.error);
                    return stop();
                }));

        when(ProcessState.CHECKING_STATE, matchEvent(
                ProcessInProgress.class,
                (message, data) -> {
                    log.info("data.id {}", data.id);
                    return goTo(ProcessState.EXECUTING);
                }));

        when(ProcessState.EXECUTING, Duration.ofSeconds(5),
                matchEvent(Collections.singletonList(StateTimeout()), ProcessData.class,
                        (message, data) -> goTo(ProcessState.CHECKING_STATE)));

        onTransition(
                matchState(ProcessState.CREATED, ProcessState.STARTING, () -> {
                    log.info(ProcessState.CREATED.name() + "2" + ProcessState.STARTING);
                })
                        .state(ProcessState.STARTING, ProcessState.CHECKING_STATE, () -> {
                            log.info(ProcessState.STARTING.name() + "2" + ProcessState.CHECKING_STATE);
                        })
                        .state(ProcessState.EXECUTING, ProcessState.CHECKING_STATE, () -> {
                            log.info(ProcessState.EXECUTING.name() + "2" + ProcessState.CHECKING_STATE);
                        })
                        .state(ProcessState.STARTING_FAILURE, ProcessState.STARTING, () -> {
                            log.info(ProcessState.STARTING_FAILURE.name() + "2" + ProcessState.STARTING);
                        })
        );

        initialize();
    }
}
