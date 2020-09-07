package org.ld.enums;

import org.ld.exception.CodeStackException;
import org.ld.exception.ErrorCode;

/**
 * 自定义异常类型
 */
public enum UserErrorCodeEnum {

    EMPTY_TOKEN(1000, "没有Token"),
    USELESS_TOKEN(1001,"token不存在或已失效，请重新获取token")
    ;

    Integer code;
    String msg;

    UserErrorCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public CodeStackException getException() {
        return new CodeStackException(new ErrorCode(this));
    }
}
