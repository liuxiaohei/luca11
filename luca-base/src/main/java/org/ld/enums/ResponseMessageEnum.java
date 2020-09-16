package org.ld.enums;

import org.springframework.http.HttpStatus;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.Response;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ResponseMessageEnum {
    OK(HttpStatus.OK, "操作成功"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "登录参数错误"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "用户被禁止"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "找不到资源"),
    CONFLICT(HttpStatus.CONFLICT, "业务逻辑异常"),
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "参数校验异常"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Hystrix异常");

    HttpStatus httpStatus;
    String name;

    ResponseMessageEnum(HttpStatus httpStatus, String name) {
        this.httpStatus = httpStatus;
        this.name = name;
    }

    public static List<Response> getMessages() {
        return Stream.of(values())
                .map(e -> new ResponseBuilder()
                        .code(e.httpStatus.value() + "")
                        .description(e.name)
                        .build())
                .collect(Collectors.toList());
    }
}
