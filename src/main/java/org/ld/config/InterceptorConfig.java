package org.ld.config;

import org.ld.annotation.NeedToken;
import org.ld.enums.UserErrorCodeEnum;
import org.ld.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Configuration
public class InterceptorConfig implements WebFilter {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    // todo https://blog.csdn.net/Javamine/article/details/93035927
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Object handlerMethod = requestMappingHandlerMapping.getHandler(exchange).toProcessor().peek();
        //注意跨域时的配置，跨域时浏览器会先发送一个option请求，这时候getHandler不会时真正的HandlerMethod
        if(handlerMethod instanceof HandlerMethod){
            Valid valid = ((HandlerMethod) handlerMethod).getMethodAnnotation(Valid.class);
            //do your logic
        }
        //preprocess()
        Mono<Void> response = chain.filter(exchange);
        //postprocess()
        return response;

    }


    // WebMvcConfigurer
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new HandlerInterceptor() {
//            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//                if (!(handler instanceof HandlerMethod)) {
//                    return true;
//                }
//                var handlerMethod = (HandlerMethod) handler;
//                var method = handlerMethod.getMethod();
//                if (method.isAnnotationPresent(NeedToken.class)) {
//                    var loginToken = method.getAnnotation(NeedToken.class);
//                    if (loginToken.required()) {
//                        // 执行认证
//                        var tokenHeader = request.getHeader(JwtUtils.TOKEN_HEADER);
//                        if (tokenHeader == null) {
//                            throw UserErrorCodeEnum.EMPTY_TOKEN.getException();
//                        }
//                        var token = tokenHeader.replace(JwtUtils.TOKEN_PREFIX, "");
//                        JwtUtils.verify(token);
//                        return true;
//                    }
//                }
//                return true;
//            }
//
//            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
//
//            }
//
//            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//
//            }
//        }).addPathPatterns("/**");
//    }


}
