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
}
