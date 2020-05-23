package org.ld.statemachine;

import org.ld.enums.Events;
import org.ld.enums.States;
import org.springframework.boot.CommandLineRunner;
import org.springframework.statemachine.StateMachine;

import javax.annotation.Resource;

public class StartupRunner implements CommandLineRunner {

    @Resource
    StateMachine<States, Events> stateMachine;

    @Override
    public void run(String... args) throws Exception {
        stateMachine.start();
        stateMachine.sendEvent(Events.ONLINE);
        stateMachine.sendEvent(Events.PUBLISH);
        stateMachine.sendEvent(Events.ROLLBACK);
    }
}
