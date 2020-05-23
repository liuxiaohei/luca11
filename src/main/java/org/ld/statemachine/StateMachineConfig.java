package org.ld.statemachine;

import org.ld.enums.Events;
import org.ld.enums.States;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.EnumSet;

@Configuration
@Scope("prototype") // 用于可以生成状态机的对象
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {
    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states.withStates().initial(States.DRAFT).states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions.withExternal()
                .source(States.DRAFT).target(States.PUBLISH_TODO)
                .event(Events.ONLINE)
                .and()
                .withExternal()
                .source(States.PUBLISH_TODO).target(States.PUBLISH_DONE)
                .event(Events.PUBLISH)
                .and()
                .withExternal()
                .source(States.PUBLISH_DONE).target(States.DRAFT)
                .event(Events.ROLLBACK);
    }

    public static final String orderStateMachineId = "recruitStateMachineId";

    @Bean
    public StateMachinePersister<States, Events, Recruit> persister(){
        return new DefaultStateMachinePersister<>(new StateMachinePersist<>() {
            @Override
            public void write(StateMachineContext<States, Events> context, Recruit order) {
                //此处并没有进行持久化操作
                //order.setStatus(context.getState());
            }

            @Override
            public StateMachineContext<States, Events> read(Recruit order) {
                //此处直接获取order中的状态，其实并没有进行持久化读取操作
                //todo
                StateMachineContext<States, Events> result = new DefaultStateMachineContext<States, Events>(order.getStates(), null, null, null, null, orderStateMachineId);
                return result;
            }
        });
    }
}
