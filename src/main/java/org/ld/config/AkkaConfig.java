package org.ld.config;

import akka.actor.*;
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

    private final Map<String, ActorRef> actorMap = new ConcurrentHashMap<>();

    @Bean
    public ActorSystem actorSystem() {
//        ActorSystemHolder.actorSystem.settings().LogLevel();
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
    public ActorRef getActorRef(String beanName, String actorId) {
        ActorSelection actorSelection = ActorSystemHolder.actorSystem.actorSelection(actorId);
        return actorMap.computeIfAbsent(
                actorId,
                key -> ActorSystemHolder.actorSystem.actorOf(createPropsByName(beanName), key));
    }
}
