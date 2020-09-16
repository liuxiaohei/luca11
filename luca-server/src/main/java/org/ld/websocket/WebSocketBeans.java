package org.ld.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketBeans {
    //    https://www.jianshu.com/p/d117bf98b4f2
    //    https://www.jianshu.com/p/b584c3dcc44a
    @Bean
    public HandlerMapping handlerMapping() {
        // 对相应的URL进行添加处理器
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/hello", new MyWebSocketHandler());
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1);
        return mapping;
    }
}
