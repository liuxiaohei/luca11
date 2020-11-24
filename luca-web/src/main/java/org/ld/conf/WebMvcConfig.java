package org.ld.conf;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // todo jwt https://www.cnblogs.com/jichuang/p/12205918.html

    @Value("${localdate.date-format:yyyy-MM-dd HH:mm:ss}")
    private String pattern; // 全局配置 LocalDateTime 的转换方式

    /**
     * 解决 使用ResponseBodyAdvice统一包装响应返回String的时候出现java.lang.ClassCastException: XXX cannot be cast to java.lang.String
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        final Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(pattern))); // 再包装日期类型的转换
        converters.add(0, new MappingJackson2HttpMessageConverter(builder.build()));
    }
}
