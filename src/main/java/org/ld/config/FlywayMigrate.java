package org.ld.config;

import lombok.Data;
import org.flywaydb.core.Flyway;
import org.ld.exception.CodeStackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

/**
 * flyway
 */
@Component
public class FlywayMigrate {
    private static Logger log = LoggerFactory.getLogger(FlywayMigrate.class);

    @Resource
    private DataSourceConfig dataSourceConfig;

//    @Value("${spring.datasource.url}")
//    private String jdbcUrl;
//
//    @Value("${spring.datasource.username}")
//    private String userName;
//
//    @Value("${spring.datasource.password}")
//    private String passWord;

    @PostConstruct
    public void init() {
        final String targetDb = Optional.ofNullable(dataSourceConfig.getJdbcUrl())
                .map(str -> str.split("/"))
                .map(strs -> strs[strs.length - 1])
                .map(str -> str.split("[?]"))
                .map(strs -> strs[0]).orElse("");
        final String rawJdbcUrl = Optional.ofNullable(dataSourceConfig.getJdbcUrl())
                .map(e -> e.split(targetDb)[0])
                .filter(e -> !e.contains("useSSL")).map(e -> e + "?useSSL=false")
                .orElse("");
        final String initSql = "CREATE DATABASE IF NOT EXISTS " + targetDb;
        final Flyway flyway = Flyway
                .configure()
                .dataSource(
                        Optional.of(dataSourceConfig.getJdbcUrl())
                                .filter(e -> !e.contains("useSSL")).map(e -> e + "&useSSL=false")
                                .orElse(dataSourceConfig.getJdbcUrl())
                        , dataSourceConfig.getUserName()
                        , dataSourceConfig.getPassWord()).load();
        log.info("init Db jdbcurl:" + rawJdbcUrl + " database:" + targetDb); //自动创建database
        try (final Connection connection = DriverManager.getConnection(rawJdbcUrl, dataSourceConfig.getUserName(), dataSourceConfig.getPassWord());
             final Statement statement = connection.createStatement()) {
            statement.execute(initSql);
            flyway.repair();
            flyway.migrate();
        } catch (Exception e) {
            log.error("", e);
            throw new CodeStackException(e);
        }
    }

}
