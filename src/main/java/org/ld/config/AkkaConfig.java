package org.ld.config;

import akka.actor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringBoot 集成Akka
 * https://www.wanaright.com/2018/09/08/akka-java8/
 * https://www.infoq.cn/article/Building-Reactive-Applications-with-Akka
 * https://blog.csdn.net/p_programmer/article/details/85041603
 * http://www.manongjc.com/article/43738.html
 * https://blog.csdn.net/qq_35246620/article/details/70157794
 * http://www.hackerav.com/?post=48681
 * https://www.cnblogs.com/yxwkf/p/4613036.html
 */
@Configuration
public class AkkaConfig {

    private static ApplicationContext context;

    private static final AbstractExtensionId<Extension> provider = new AbstractExtensionId<>() {
        @Override
        public Extension createExtension(ExtendedActorSystem extendedActorSystem) {
            return new Extension() {
            };
        }
    };

    @Autowired
    public AkkaConfig(ApplicationContext c) {
        context = c;
    }

    @Bean
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem.create("system");
        provider.get(system);
        return system;
    }

    public Props create(String beanName) {
        return Props.create(DIProducer.class, beanName);
    }

    public static class DIProducer implements IndirectActorProducer {
        private final String beanName;

        public DIProducer(String beanName) {
            this.beanName = beanName;
        }

        @Override
        public Actor produce() {
            return (Actor) context.getBean(beanName);
        }

        @Override
        public Class<? extends Actor> actorClass() {
            return (Class<? extends Actor>) context.getType(beanName);
        }
    }

}
