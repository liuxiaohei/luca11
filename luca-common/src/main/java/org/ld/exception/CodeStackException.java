package org.ld.exception;

import org.ld.enums.SystemErrorCodeEnum;
import org.ld.enums.UserErrorCodeEnum;

/**
 * ld
 */
public class CodeStackException extends RuntimeException {

    private ErrorCode errorCode;

    public static CodeStackException of(Throwable e) {
        if (e instanceof CodeStackException) {
            return (CodeStackException) e;
        } else {
            return new CodeStackException(e);
        }
    }

    public static CodeStackException of(UserErrorCodeEnum userErrorCodeEnum,Throwable e) {
        var r = of(e);
        r.errorCode = new ErrorCode(userErrorCodeEnum);
        r.errorCode.setMsg(e.getMessage());
        return r;
    }

    public CodeStackException(String msg) {
        super(msg);
        this.errorCode = new ErrorCode(SystemErrorCodeEnum.UNKNOWN);
    }

    public CodeStackException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private CodeStackException(Throwable e) {
        super(e.getMessage(), e);
        super.setStackTrace(e.getStackTrace());
        this.errorCode = SystemErrorCodeEnum.getSystemErrorCode(e);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
