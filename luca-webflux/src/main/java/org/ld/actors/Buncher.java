package org.ld.actors;

import akka.actor.AbstractFSM;
import akka.japi.pf.UnitMatch;
import lombok.extern.slf4j.Slf4j;
import org.ld.beans.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Actor 可以处于两种状态：没有消息排队（即Idle）或有消息排队（即Active）
 * 它将保持Active状态，只要消息一直到达并且不请求刷新。Actor 的内部状态数据由发送的目标 Actor 引用和消息的实际队列组成
 */
@Slf4j
@Component("buncher")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Buncher extends AbstractFSM<State, Data> {

    {
        startWith(org.ld.beans.State.Idle, Uninitialized.Uninitialized);

        when(org.ld.beans.State.Idle,
                matchEvent(SetTarget.class, Uninitialized.class,
                        (setTarget, uninitialized) ->
                                stay().using(new Todo(setTarget.getRef(), new LinkedList<>()))));

        onTransition(matchState(org.ld.beans.State.Active, org.ld.beans.State.Idle,
                        () -> {
                            log.info("Active2Idle" );
                            // reuse this matcher
                            final UnitMatch<Data> m = UnitMatch.create(matchData(Todo.class,
                                    todo -> todo.getTarget().tell(new Batch(todo.getQueue()), getSelf())));
                            m.match(stateData());
                        })
                        .state(org.ld.beans.State.Idle, org.ld.beans.State.Active,
                                () -> log.info("Idle2Active" )));

        when(org.ld.beans.State.Active, Duration.ofSeconds(1L),
                matchEvent(Arrays.asList(Flush.class, StateTimeout()), Todo.class,
                        (event, todo) -> goTo(org.ld.beans.State.Idle).using(todo.copy(new LinkedList<>()))));

        whenUnhandled(matchEvent(
                Queue.class, Todo.class,
                (queue, todo) -> goTo(org.ld.beans.State.Active).using(todo.addElement(queue.getObj())))
                .anyEvent((event, state) -> {
                            log().warning(
                                    "received unhandled request {} in state {}/{}",
                                    event,
                                    stateName(),
                                    state);
                            return stay();
                        }));
        initialize();
    }
}

