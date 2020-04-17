package org.ld.exception;

import org.ld.enums.SystemErrorCodeEnum;

@SuppressWarnings("unused")
public class ErrorCode {

    private Integer code;

    private String msg;

    public ErrorCode(SystemErrorCodeEnum value) {
        this.code = value.getCode();
        this.msg = value.getMsg();
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
