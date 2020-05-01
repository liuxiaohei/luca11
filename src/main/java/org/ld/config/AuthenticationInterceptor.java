package org.ld.config;

import org.ld.annotation.NeedToken;
import org.ld.enums.UserErrorCodeEnum;
import org.ld.utils.JwtUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器策略
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        var handlerMethod=(HandlerMethod)handler;
        var method=handlerMethod.getMethod();
        if (method.isAnnotationPresent(NeedToken.class)) {
            var loginToken = method.getAnnotation(NeedToken.class);
            if (loginToken.required()) {
                // 执行认证
                var tokenHeader = request.getHeader(JwtUtils.TOKEN_HEADER);
                if(tokenHeader == null){
                    throw UserErrorCodeEnum.EMPTY_TOKEN.getException();
                }
                var token = tokenHeader.replace(JwtUtils.TOKEN_PREFIX, "");
                JwtUtils.verify(token);
                return true;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    }
}
