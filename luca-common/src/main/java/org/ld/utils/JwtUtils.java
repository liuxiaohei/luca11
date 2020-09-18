package org.ld.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.ld.enums.UserErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.springframework.http.HttpHeaders;

import java.util.Date;

@SuppressWarnings("unused")
public class JwtUtils {
    public static final String TOKEN_HEADER = HttpHeaders.AUTHORIZATION;
    public static final String TOKEN_PREFIX = "Bearer ";
    private static final long EXPIRE_TIME = 5 * 60 * 1000;
    private static final String SECRET = "jwtsecretdemo";

    /**
     * 生成签名,5分钟后过期
     *
     * @param name 名称
     * @return 加密后的token
     */
    public static String sign(String name) {
        var date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        var algorithm = Algorithm.HMAC256(SECRET); //使用HS256算法
        return JWT.create() //创建令牌实例
                .withClaim("name", name) //指定自定义声明，保存一些信息
                //.withSubject(name) //信息直接放在这里也行
                .withExpiresAt(date) //过期时间
                .sign(algorithm);
    }

    /**
     * 校验token是否正确
     *
     * @param token 令牌
     */
    public static void verify(String token) {
        try {
            var name = getName(token);
            var algorithm = Algorithm.HMAC256(SECRET);
            var verifier = JWT.require(algorithm)
                    .withClaim("name", name)
                    //.withSubject(name)
                    .build();
            verifier.verify(token);
        } catch (Exception e) {
            throw new CodeStackException(UserErrorCodeEnum.USELESS_TOKEN, e);
        }
    }

    /**
     * 获得token中的信息
     *
     * @return token中包含的名称
     */
    public static String getName(String token) {
        try {
            var jwt = JWT.decode(token);
            return jwt.getClaim("name").asString();
        } catch (Exception e) {
            return null;
        }
    }
}
