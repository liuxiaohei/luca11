package org.ld.config;

import io.transwarp.guardian.federation.org.yaml.snakeyaml.Yaml;
import org.ld.api.ConfigPropertiesApi;
import org.ld.beans.ConfigProperties;
import org.ld.utils.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * 自动刷新客户端配置
 */
@Configuration
@Order(value = 1)
public class AutoRefreshConfiguration implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AutoRefreshConfiguration.class);

    @Autowired
    ApplicationContext applicationContext;

    @javax.annotation.Resource
    ConfigPropertiesApi configPropertiesApi;

    @Scheduled(fixedDelay = 1000 * 30)
    public synchronized void autoRefreshConfig() {
        applicationContext.publishEvent(new ConfigEvent(this,"刷新"));
    }

    @Value(value = "classpath:configdesc.json")
    private Resource resource;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.profiles.active}")
    private String profiles;

    /**
     * todo
     */
    @Override
    public void run(String... args) {
        try {
            ConfigProperties configProperties = ConfigProperties.builder().application(appName).profile(profiles).build();
            List<ConfigProperties> configPropertiesList = configPropertiesApi.getAllConfig(configProperties);
            String jsonStr = new String(IOUtil.readFully(resource.getInputStream(), -1,true));
            Yaml yaml = new Yaml();
            URL url = AutoRefreshConfiguration.class.getClassLoader().getResource("application.yml");
            if (url != null) {
                //获取test.yaml文件中的配置数据，然后转换为obj，
                Object obj =yaml.load(new FileInputStream(url.getFile()));
                System.out.println(obj);
                //也可以将值转换为Map
                Map map =(Map)yaml.load(new FileInputStream(url.getFile()));
                System.out.println(map);
                //通过map我们取值就可以了.
            }
//            List<ConfigProperties> configProperties = JsonUtil.json2List(jsonStr,ConfigProperties.class);
        } catch (Exception e) {
            LOG.error("初始化本地配置到注册中心失败", e);
        }
    }
}
