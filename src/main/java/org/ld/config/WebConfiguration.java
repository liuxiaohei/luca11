package org.ld.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.reactive.config.WebFluxConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * // todo 整理
 * https://www.cnblogs.com/oldboyooxx/p/10824531.html
 * https://cloud.tencent.com/developer/article/1376509
 * 解决统一包装类返回String 类型报错
 */
@Configuration
public class WebConfiguration implements WebFluxConfigurer {

    @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String pattern; // 全局配置 LocalDateTime 的转换方式

    // WebMvcConfigurer
//    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        final var builder = Jackson2ObjectMapperBuilder.json();
        builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(pattern))); // 再包装日期类型的转换
        converters.add(0, new MappingJackson2HttpMessageConverter(builder.build()));
    }
}
