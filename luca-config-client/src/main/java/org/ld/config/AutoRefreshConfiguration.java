package org.ld.config;

import org.ld.beans.ConfigProperties;
import org.ld.utils.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 自动刷新客户端配置
 */
@Configuration
@Order(value = 1)
public class AutoRefreshConfiguration  implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AutoRefreshConfiguration.class);

    @Autowired
    private ContextRefresher contextRefresher;

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public synchronized void autoRefreshConfig() {
        contextRefresher.refresh();
    }

    @Value(value = "classpath:config.xlsx")
    private Resource resource;

    @Override
    public void run(String... args) {
        try {
            final List<ConfigProperties> dbStructures = ExcelUtils.readFile(resource::getInputStream, ConfigProperties.class);
        } catch (Exception e) {
            LOG.error("初始化本地配置到注册中心失败",e);
        }
    }
}
