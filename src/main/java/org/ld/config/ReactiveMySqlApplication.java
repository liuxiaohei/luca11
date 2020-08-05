package org.ld.config;

import com.github.jasync.r2dbc.mysql.JasyncConnectionFactory;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import com.github.jasync.sql.db.mysql.util.URLParser;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.function.DatabaseClient;

import java.nio.charset.StandardCharsets;

@Log4j2
@Configuration
public class ReactiveMySqlApplication {

    @Bean
    public DatabaseClient databaseClient() {
        var url = "jdbc:mysql://127.0.0.1/luca?user=root&password=12345678";
        var connectionFactory = new JasyncConnectionFactory(new MySQLConnectionFactory(URLParser.INSTANCE.parseOrDie(url, StandardCharsets.UTF_8)));
        return DatabaseClient.create(connectionFactory);
    }
}


