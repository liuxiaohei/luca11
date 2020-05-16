package org.ld.examples.akka.akkafsm;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RandomPool;
import org.ld.examples.akka.akkafsm.actor.ProcessChecker;
import org.ld.examples.akka.akkafsm.actor.ProcessDispatcher;
import org.ld.examples.akka.akkafsm.actor.ProcessStarter;
import org.ld.examples.akka.akkafsm.actor.state.ProcessData;
import org.ld.examples.akka.akkafsm.actor.state.ProcessState;
import org.ld.examples.akka.akkafsm.service.ProcessExecutorAdapter;

public class AkkaFsmApplication {

    public static void main(String[] args) {

        var actorSystem = ActorSystem.create("ProcessDispatcherSystem");

        var processExecutorAdapter = new ProcessExecutorAdapter();

        var processStarter = actorSystem.actorOf(new RandomPool(10)
                .props(Props.create(ProcessStarter.class, processExecutorAdapter)));

        var processChecker = actorSystem.actorOf(new RandomPool(10)
                .props(Props.create(ProcessChecker.class, processExecutorAdapter)));

        var a = actorSystem.actorOf(Props.create(
                ProcessDispatcher.class,
                ProcessState.CREATED,
                new ProcessData().setParams("5000", ""),
                processStarter,
                processChecker));
    }
}
