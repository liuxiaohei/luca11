package org.ld.utils;

import akka.actor.*;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import org.ld.config.LucaConfig;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * SpringBoot 集成Akka
 * https://blog.csdn.net/qq_35246620/article/details/70157794
 * https://www.cnblogs.com/yxwkf/p/4613036.html
 */
public class AkkaUtil {

    /**
     * 可通过bean的名称找到对应的ActorProps
     */
    @SuppressWarnings("unchecked")
    private static Props createPropsByName(String beanName) {
        return Props.create(
                (Class<Actor>) SpringBeanFactory.getType(beanName),
                () -> (Actor) SpringBeanFactory.getBean(beanName)
        );
    }

    private static final Timeout t = new Timeout(1, TimeUnit.DAYS);

    /**
     * 可通过bean的名称和ActorId 创建Actor对象
     */
    public static ActorRef getActorRef(String beanName, String actorId) {
        final var sel = LucaConfig.ActorSystemHolder.ACTORSYSTEM.actorSelection("/user/" + actorId);
        final var fut = new AskableActorSelection(sel).ask(new Identify(1), t);
        final var indent = (ActorIdentity) AkkaFutureAdapter.of(fut).join();
        return indent.getActorRef().orElseGet(() -> LucaConfig.ActorSystemHolder.ACTORSYSTEM.actorOf(createPropsByName(beanName), actorId));
    }

    /**
     * 可通过bean的名称和ActorId 删除Actor对象
     */
    public static void stopActorRef(String beanName, String actorId) {
        var actorRef = getActorRef(beanName,actorId);
        LucaConfig.ActorSystemHolder.ACTORSYSTEM.stop(actorRef);
    }
}
