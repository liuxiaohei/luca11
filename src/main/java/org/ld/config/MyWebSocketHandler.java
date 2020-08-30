package org.ld.config;
import org.ld.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//https://blog.csdn.net/sinat_39291367/article/details/89467555
//https://www.cnblogs.com/limuma/p/9315517.html
//https://segmentfault.com/a/1190000022986674
//https://cloud.tencent.com/developer/article/1480108
//https://blog.csdn.net/HXHCHY/article/details/102773424
public class MyWebSocketHandler implements WebSocketHandler {

    @Autowired
    private TokenService tokenService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // 在生产环境中，需对url中的参数进行检验，如token，不符合要求的连接的直接关闭
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        if (handshakeInfo.getUri().getQuery() == null) {
            return session.close(CloseStatus.REQUIRED_EXTENSION);
        } else {
            // todo 暂时没有认证
            // 对参数进行解析，在些使用的是jetty-util包
//            MultiMap<String> values = new MultiMap<String>();
//            UrlEncoded.decodeTo(handshakeInfo.getUri().getQuery(), values, "UTF-8");
//            String token = values.getString("token");
            boolean isValidate = true; //tokenService.validate(token);
            if (!isValidate) {
                return session.close();
            }
        }
        Flux<WebSocketMessage> output = session
                .receive()
                .concatMap(mapper -> {
                    String msg = mapper.getPayloadAsText();
                    System.out.println("mapper: " + msg);
                    return Flux.just(msg);
                })
                .map(value -> {
                    System.out.println("value: " + value);
                    return session.textMessage("Echo " + value);
                });
        return session.send(output);
    }
}
