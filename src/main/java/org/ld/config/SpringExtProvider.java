package org.ld.config;


import akka.actor.*;
import org.springframework.context.ApplicationContext;

/**
 * 通过继承AbstractExtensionId，我们可以在ActorSystem范围内创建并查找SpringExt
 */
public class SpringExtProvider extends AbstractExtensionId<SpringExtProvider.SpringExt> {
    private static final SpringExtProvider provider = new SpringExtProvider();

    public static SpringExtProvider getInstance() {
        return provider;
    }

    @Override
    public SpringExt createExtension(ExtendedActorSystem extendedActorSystem) {
        return new SpringExt();
    }

    public static class SpringExt implements Extension {
        private ApplicationContext context;

        public void init(ApplicationContext context) {
            System.out.println("applicationContext初始化...");
            this.context = context;
        }

        /**
         * 该方法用来创建Props对象，依赖前面创建的DI组件，获取到Props对象，我们就可以创建Actor bean对象
         *
         * @param beanName actor bean 名称
         * @return props
         */
        public Props create(String beanName) {
            return Props.create(DIProducer.class, this.context, beanName);
        }
    }

    public static class DIProducer implements IndirectActorProducer {
        private final ApplicationContext context;
        private final String beanName;

        public DIProducer(ApplicationContext context, String beanName) {
            this.context = context;
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
