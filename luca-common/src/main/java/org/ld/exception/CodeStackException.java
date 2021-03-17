package org.ld.exception;

import org.ld.enums.SystemErrorCodeEnum;
import org.ld.enums.UserErrorCodeEnum;

/**
 * ld
 */
public class CodeStackException extends RuntimeException {

    private ErrorCode errorCode;

    public static CodeStackException of(Throwable e) {
        return e instanceof CodeStackException ? (CodeStackException) e : new CodeStackException(e);
    }

    public static CodeStackException of(UserErrorCodeEnum userErrorCodeEnum, Throwable e) {
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
        if (e instanceof InterruptedException && Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
        this.errorCode = SystemErrorCodeEnum.getSystemErrorCode(e);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
