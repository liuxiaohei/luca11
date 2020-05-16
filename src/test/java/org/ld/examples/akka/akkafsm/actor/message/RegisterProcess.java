package org.ld.examples.akka.akkafsm.actor.message;

import org.ld.examples.akka.akkafsm.actor.state.ProcessData;
import org.ld.examples.akka.akkafsm.actor.state.ProcessState;

public class RegisterProcess {
    public final ProcessState processState;
    public final ProcessData processData;

    public RegisterProcess(ProcessState processState, ProcessData processData) {
        this.processState = processState;
        this.processData = processData;
    }
}
