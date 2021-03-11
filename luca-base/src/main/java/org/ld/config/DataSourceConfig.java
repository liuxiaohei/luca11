package org.ld.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.Properties;

@Getter
@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@PropertySources(value = {@PropertySource(value = "classpath:db.properties")})
public class DataSourceConfig {

    @Value("${db.driver}")
    private String driver;

    @Value("${db.url}")
    private String jdbcUrl;

    @Value("${db.username}")
    private String userName;

    @Value("${db.password}")
    private String passWord;

    String getFullUrl() {
        return getJdbcUrl() + "user=" + getUserName() + "&password=" + getPassWord();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        // Hikari 日语 光
        var datasource = new HikariDataSource();
        datasource.setJdbcUrl(jdbcUrl);
        datasource.setUsername(userName);
        datasource.setPassword(passWord);
        datasource.setDriverClassName(driver);
        datasource.setConnectionTestQuery("select 1 from DUAL");
        datasource.setAutoCommit(true);
//        连接池的最大值，同一时间可以从池分配的最多连接数量，0时无限制   默认值是8
        datasource.setMaximumPoolSize(30);
//        最小空闲值.当空闲的连接数少于阀值时，连接池就会预申请去一些连接，以免洪峰来时来不及申请  默认值是0 -->
        datasource.setMinimumIdle(5);
        datasource.setConnectionTimeout(30000);
        datasource.setValidationTimeout(5000);
        datasource.setIdleTimeout(10000);
        datasource.setMaxLifetime(1800000);
        Properties properties = new Properties();
        properties.put("cachePrepStmts", true);
        properties.put("prepStmtCacheSize", 250);
        properties.put("prepStmtCacheSqlLimit", 2048);
        properties.put("useServerPrepStmts", true);
        properties.put("useLocalSessionState", true);
        properties.put("useLocalTransactionState", true);
        properties.put("rewriteBatchedStatements", true);
        properties.put("cacheResultSetMetadata", true);
        properties.put("cacheServerConfiguration", true);
        properties.put("elideSetAutoCommits", true);
        properties.put("maintainTimeStats", false);
        datasource.setDataSourceProperties(properties);
        return datasource;
    }
}
