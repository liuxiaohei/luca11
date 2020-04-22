package org.ld.config;

import lombok.Data;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

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
        final String targetDb = Optional.ofNullable(jdbcUrl)
                .map(str -> str.split("/"))
                .map(strs -> strs[strs.length - 1])
                .map(str -> str.split("[?]"))
                .map(strs -> strs[0]).orElse("");
        final String rawJdbcUrl = Optional.ofNullable(jdbcUrl).map(e -> e.split(targetDb)[0]).orElse("");
        final String initSql = "CREATE DATABASE IF NOT EXISTS " + targetDb;
        final Flyway flyway = Flyway
                .configure()
                .dataSource(
                        Optional.of(jdbcUrl)
                                .filter(e -> !e.contains("useSSL")).map(e -> e + "&useSSL=false")
                                .orElse(jdbcUrl)
                        , userName
                        , passWord).load();
        log.info("开始迁移数据");
        log.info("init Db jdbcurl:" + rawJdbcUrl + " database:" + targetDb); //自动创建database
        try (final Connection connection = DriverManager.getConnection(rawJdbcUrl, userName, passWord);
             final Statement statement = connection.createStatement()) {
            statement.execute(initSql);
            flyway.repair();
            flyway.migrate();
            log.info("迁移结束");
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

}
