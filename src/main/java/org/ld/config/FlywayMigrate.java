package org.ld.config;

import org.flywaydb.core.Flyway;
import org.ld.exception.CodeStackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.DriverManager;
import java.util.Optional;

/**
 * flyway
 */
@Component
public class FlywayMigrate {

    private static final Logger log = LoggerFactory.getLogger(FlywayMigrate.class);

    @Resource
    private DataSourceConfig dataSourceConfig;

    @PostConstruct
    public void init() {
        final var targetDb = Optional.ofNullable(dataSourceConfig.getJdbcUrl())
                .map(str -> str.split("/"))
                .map(strs -> strs[strs.length - 1])
                .map(str -> str.split("[?]"))
                .map(strs -> strs[0]).orElse("");
        final var rawJdbcUrl = Optional.ofNullable(dataSourceConfig.getJdbcUrl())
                .map(e -> e.split(targetDb)[0])
                .filter(e -> !e.contains("useSSL")).map(e -> e + "?useSSL=false")
                .orElse("");
        final var initSql = "CREATE DATABASE IF NOT EXISTS " + targetDb;
        final var flyway = Flyway
                .configure()
                .dataSource(
                        Optional.of(dataSourceConfig.getJdbcUrl())
                                .filter(e -> !e.contains("useSSL")).map(e -> e + "&useSSL=false")
                                .orElse(dataSourceConfig.getJdbcUrl())
                        , dataSourceConfig.getUserName()
                        , dataSourceConfig.getPassWord()).load();
        log.info("init Db jdbcUrl:" + rawJdbcUrl + " database:" + targetDb); //自动创建database
        try (final var connection = DriverManager.getConnection(rawJdbcUrl, dataSourceConfig.getUserName(), dataSourceConfig.getPassWord());
             final var statement = connection.createStatement()) {
            statement.execute(initSql);
            flyway.repair();
            flyway.migrate();
        } catch (Exception e) {
            log.error("", e);
            throw new CodeStackException(e);
        }
    }

}
