package org.ld.controller;

import org.ld.enums.Events;
import org.ld.enums.States;
import org.ld.statemachine.Recruit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class MyApp {
    ThreadLocal<StateMachine> stateMachineThreadLocal = new ThreadLocal<StateMachine>();

//    @Autowired
//    @Qualifier(value = "recruitStateMachineFactory")
//    StateMachineFactory<States, Events> stateMachineFactory;

    @Resource
    StateMachine<States, Events> stateMachine;

    @Autowired
    private StateMachinePersister<States, Events, Recruit> persister;

    @RequestMapping("/hello")
    void doSignals() {
        System.out.println("start before");
        Recruit recruit = new Recruit();
        StateMachine stateMachinet = getStateMachine(recruit);
        System.out.println("dosignals stateMachinet hashcode " + stateMachinet.hashCode() + "  recruit hashcode is " + recruit.hashCode());
//        stateMachinet.sendEvent(Events.EVENT1);
//        stateMachinet.sendEvent(Events.EVENT2);
    }

    private StateMachine getStateMachine(Recruit recruit) {
        StateMachine machine = stateMachineThreadLocal.get();
        if (null == machine) {
//            machine = stateMachineFactory.getStateMachine("recruitStateMachineId");
        }
        try {
            machine.start();
            persister.restore(machine, recruit);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return machine;
    }

    void sendEvent(Events events, Recruit recruit) {
        Message message = MessageBuilder.withPayload(events).setHeader("recruit", recruit).build();
        StateMachine stateMachine = getStateMachine(recruit);
        stateMachine.sendEvent(message);
    }

    @RequestMapping("/sendEvent1")
    void sendEvent() {
        Recruit recruit = new Recruit();
        recruit.setStates(States.DRAFT);
        StateMachine stateMachine = getStateMachine(recruit);
        stateMachine.sendEvent(Events.PUBLISH);

    }

    @RequestMapping("/sendEvent2")
    void sendEvent2() {
        Recruit recruit = new Recruit();
        recruit.setStates(States.DRAFT);
        StateMachine stateMachine = getStateMachine(recruit);
        stateMachine.sendEvent(Events.PUBLISH);

    }
}
