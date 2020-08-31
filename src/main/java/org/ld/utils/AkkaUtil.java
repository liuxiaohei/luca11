package org.ld.utils;

import akka.actor.*;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import org.springframework.context.annotation.Configuration;
import scala.concurrent.Await;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * SpringBoot 集成Akka
 * https://blog.csdn.net/qq_35246620/article/details/70157794
 * https://www.cnblogs.com/yxwkf/p/4613036.html
 */
@Configuration
public class AkkaUtil {

    @Resource
    ActorSystem actorSystem;

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
        final var sel = actorSystem.actorSelection("/user/" + actorId);
        final var fut = new AskableActorSelection(sel).ask(new Identify(1), t);
        final var indent = (ActorIdentity) Await.result(fut, t.duration());
        return indent.getActorRef()
                .orElseGet(() -> actorSystem.actorOf(createPropsByName(beanName), actorId));
    }
}
