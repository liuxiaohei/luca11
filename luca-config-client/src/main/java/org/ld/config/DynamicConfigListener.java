package org.ld.config;

import lombok.extern.slf4j.Slf4j;
import org.ld.beans.ConfigChange;
import org.ld.utils.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;
import java.util.Set;

/**
 * 配置刷新监听器
 */
@EnableFeignClients("org.ld.api")
@Configuration
@Slf4j
public class DynamicConfigListener implements ApplicationListener<ConfigEvent>, ApplicationContextAware, Ordered {

    @Autowired
    private ContextRefresher refresh;

    private ApplicationContext context;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    public void onApplicationEvent(ConfigEvent event) {
        final ConfigurableEnvironment beforeEnv = (ConfigurableEnvironment) this.context.getEnvironment();
        final MutablePropertySources propertySources = beforeEnv.getPropertySources();
        final MutablePropertySources beforeSources = new MutablePropertySources(propertySources);
        final Set<String> refresh = this.refresh.refresh();
        final Map<String, ConfigChange> contrast = PropertyUtil.contrast(beforeSources, propertySources);
        if (!contrast.isEmpty()) {
            context.publishEvent(new ActionConfigEvent(this, "Refresh config", contrast));
            log.debug("[ActionApplicationListener] The update is successful {}", refresh);
        }
    }

    public int getOrder() {
        return 2147483646;
    }
}
