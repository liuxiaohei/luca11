package org.ld.config;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    Map<String, ActorRef> actorMap = new ConcurrentHashMap<>();

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
                (Class<Actor>) StaticApplicationContext.getType(beanName),
                () -> (Actor) StaticApplicationContext.getBean(beanName)
        );
    }

    /**
     * 可通过bean的名称和ActorId 创建Actor对象
     */
    public ActorRef createActorRef(String beanName, String ActorId) {
        return actorMap.computeIfAbsent(
                ActorId,
                key -> ActorSystemHolder.actorSystem.actorOf(createPropsByName(beanName), key));
    }
}
