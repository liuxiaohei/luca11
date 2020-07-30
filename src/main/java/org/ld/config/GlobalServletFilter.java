package org.ld.config;

import org.ld.utils.SnowflakeId;
import org.ld.utils.ZLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Configuration
public class GlobalServletFilter {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    @Bean
    public OncePerRequestFilter requestFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
                try {
                    final var uuid = Optional.ofNullable(AroundController.UUIDS.get()).orElseGet(() -> SnowflakeId.get().toString());
                    AroundController.UUIDS.set(uuid);
                    var time = System.currentTimeMillis();
                    filterChain.doFilter(httpServletRequest, httpServletResponse);
                    time = System.currentTimeMillis() - time;
                    String info = uuid + ":" + (time > 1000 ? "[警告]" : "") + "耗时: 毫秒(" + time + ")";
                    LOG.debug(info);
                } finally {
                    AroundController.UUIDS.remove();
                }
            }
        };
    }
}
