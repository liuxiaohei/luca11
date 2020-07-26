package org.ld.examples.akka.akkafsm;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RandomPool;
import org.ld.actors.ProcessChecker;
import org.ld.actors.ProcessDispatcher;
import org.ld.actors.ProcessStarter;
import org.ld.beans.ProcessData;
import org.ld.enums.ProcessState;
import org.ld.actors.ProcessExecutorAdapter;

public class AkkaFsmApplication {

    public static void main(String[] args) {

        var actorSystem = ActorSystem.create("ProcessDispatcherSystem");

        var processExecutorAdapter = new ProcessExecutorAdapter();

        var processStarter = actorSystem.actorOf(new RandomPool(10)
                .props(Props.create(ProcessStarter.class, processExecutorAdapter)));

        var processChecker = actorSystem.actorOf(new RandomPool(10)
                .props(Props.create(ProcessChecker.class, processExecutorAdapter)));

        actorSystem.actorOf(Props.create(
                ProcessDispatcher.class,
                ProcessState.CREATED,
                new ProcessData().setParams("5000", ""),
                processStarter,
                processChecker));
    }
}
