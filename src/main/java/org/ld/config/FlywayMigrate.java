package org.ld.config;

import lombok.Data;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 */
@Component
@Data
public class FlywayMigrate {
    private static Logger log = LoggerFactory.getLogger(FlywayMigrate.class);

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String userName;

    @Value("${spring.datasource.password}")
    private String passWord;

    @PostConstruct
    public void init() {
        Flyway flyway = Flyway.configure().dataSource(jdbcUrl,userName,passWord).load();
        log.info("开始迁移数据");
        try {
            flyway.migrate();
            log.info("迁移结束");
        } catch (Exception e) {
            log.error("",e);
        }
    }

}
