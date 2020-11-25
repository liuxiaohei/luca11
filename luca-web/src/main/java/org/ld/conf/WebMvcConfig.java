package org.ld.conf;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.ld.annotation.NeedToken;
import org.ld.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

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

    // jwt https://www.cnblogs.com/jichuang/p/12205918.html
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
                if (!(object instanceof HandlerMethod)) {
                    return true;
                }
                HandlerMethod handlerMethod = (HandlerMethod) object;
                Method method = handlerMethod.getMethod();
                if (method.isAnnotationPresent(NeedToken.class)) {
                    if (method.getAnnotation(NeedToken.class).required()) {
                        String token = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token
                        if (token == null) {
                            throw new RuntimeException("无token，请重新登录");
                        }
                        JwtUtils.verify(token);
                        return true;
                    }
                }
                return true;
            }
            @Override
            public void postHandle(HttpServletRequest httpServletRequest,
                                   HttpServletResponse httpServletResponse,
                                   Object o, ModelAndView modelAndView) {

            }
            @Override
            public void afterCompletion(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        Object o, Exception e) {
            }
        }).addPathPatterns("/**");    // 拦截所有请求，通过判断是否有 @NeedToken 注解 决定是否需要登录
    }
}
