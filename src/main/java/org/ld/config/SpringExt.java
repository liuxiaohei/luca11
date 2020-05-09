package org.ld.config;


import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;

/**
 * 扩展组件，ApplicationContext会在SpringBoot初始化的时候加载进来
 * 构造Props,用于生产ActorRef
 */
public class SpringExt implements Extension {
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
