package org.ld.examples.akka.akkafsm.actor.state;

public enum ProcessState {
    CREATED,
    STARTING,
    STARTING_FAILURE,
    CHECKING_STATE,
    EXECUTING
}
