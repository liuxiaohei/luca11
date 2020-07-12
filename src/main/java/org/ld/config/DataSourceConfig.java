package org.ld.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;

@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@PropertySources( value = {@PropertySource( value = "classpath:db.properties")})
public class DataSourceConfig {

    @Value("${db.driver}")
    private String driver;

    @Value("${db.url}")
    private String jdbcUrl;

    @Value("${db.username}")
    private String userName;

    @Value("${db.password}")
    private String passWord;

//    @Value("${db.poolPingQuery}")
//    private String poolPingQuery;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource datasource = new HikariDataSource();
        datasource.setJdbcUrl(jdbcUrl);
        datasource.setUsername(userName);
        datasource.setPassword(passWord);
        datasource.setDriverClassName(driver);
//        datasource.setConnectionTestQuery(poolPingQuery);
        datasource.setAutoCommit(true);
        datasource.setMaximumPoolSize(30);
        datasource.setMinimumIdle(5);
        datasource.setConnectionTimeout(30000);
        datasource.setMaxLifetime(1800000);
        return datasource;
    }
}
