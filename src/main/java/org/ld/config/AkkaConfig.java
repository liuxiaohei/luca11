package org.ld.config;

import akka.actor.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringBoot 集成Akka
 * https://blog.csdn.net/qq_35246620/article/details/70157794
 * https://www.cnblogs.com/yxwkf/p/4613036.html
 */
@Configuration
public class AkkaConfig {

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create("lucaSystem");
    }

    public Props createPropsByName(String beanName) {
        return Props.create(DIProducer.class, beanName);
    }

    /**
     * 可通过bean的名称找到对应的Actor
     */
    public static class DIProducer implements IndirectActorProducer {

        private final String beanName;

        public DIProducer(String beanName) {
            this.beanName = beanName;
        }

        @Override
        public Actor produce() {
            return (Actor) StaticApplicationContext.getBean(beanName);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Actor> actorClass() {
            return (Class<? extends Actor>) StaticApplicationContext.getType(beanName);
        }
    }

}
