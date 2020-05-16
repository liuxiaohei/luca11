package org.ld.config;

import akka.actor.ActorSystem;
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
 *
 * https://blog.csdn.net/qq_35246620/article/details/70157794
 * http://www.hackerav.com/?post=48681
 * https://www.cnblogs.com/yxwkf/p/4613036.html
 */
@Configuration
public class AkkaConfig {

    private final ApplicationContext context;

    @Autowired
    public AkkaConfig(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public ActorSystem createSystem() {
        ActorSystem system = ActorSystem.create("system");
        SpringExtProvider.getInstance().get(system).init(context);
        return system;
    }
}
