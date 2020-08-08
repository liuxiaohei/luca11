package org.ld.config;

import akka.actor.*;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import org.ld.utils.SpringBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scala.concurrent.Await;

import java.util.concurrent.TimeUnit;

/**
 * SpringBoot 集成Akka
 * https://blog.csdn.net/qq_35246620/article/details/70157794
 * https://www.cnblogs.com/yxwkf/p/4613036.html
 */
@Configuration
public class AkkaConfig {

    private static class ActorSystemHolder {
        private static final ActorSystem actorSystem = ActorSystem.create("lucaSystem");
    }

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystemHolder.actorSystem;
    }

    /**
     * 可通过bean的名称找到对应的ActorProps
     */
    @SuppressWarnings("unchecked")
    public Props createPropsByName(String beanName) {
        return Props.create(
                (Class<Actor>) SpringBeanFactory.getType(beanName),
                () -> (Actor) SpringBeanFactory.getBean(beanName)
        );
    }

    private final Timeout t = new Timeout(1, TimeUnit.DAYS);

    /**
     * 可通过bean的名称和ActorId 创建Actor对象
     */
    public ActorRef getActorRef(String beanName, String actorId) throws Exception {
        final var sel = ActorSystemHolder.actorSystem.actorSelection("/user/" + actorId);
        final var fut = new AskableActorSelection(sel).ask(new Identify(1), t);
        final var indent = (ActorIdentity) Await.result(fut, t.duration());
        return indent.getActorRef()
                .orElseGet(() -> ActorSystemHolder.actorSystem.actorOf(createPropsByName(beanName), actorId));
    }
}
