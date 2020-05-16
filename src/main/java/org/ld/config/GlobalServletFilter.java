package org.ld.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class GlobalServletFilter {

    @Bean
    public OncePerRequestFilter requestFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
                long time = System.currentTimeMillis();
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                time = System.currentTimeMillis() - time;
                logger.info((time > 1000 ? "[严重警告]" : "") + "耗时: 毫秒(" + time + ")");
            }
        };
    }
}
