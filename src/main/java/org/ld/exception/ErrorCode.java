package org.ld.exception;

import org.ld.enums.SystemErrorCodeEnum;
import org.ld.enums.UserErrorCodeEnum;

public class ErrorCode {

    private final Integer code;

    private String msg;

    public ErrorCode(SystemErrorCodeEnum value) {
        this.code = value.getCode();
        this.msg = value.getMsg();
    }

    public ErrorCode(UserErrorCodeEnum e) {
        this.code = e.getCode();
        this.msg = e.getMsg();
    }

    public String getMessage() {
        return msg;
    }

    public ErrorCode setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Integer getCode() {
        return code;
    }

}
