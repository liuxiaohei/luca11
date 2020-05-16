package org.ld.exception;

import org.ld.enums.SystemErrorCodeEnum;
import org.ld.enums.UserErrorCodeEnum;

/**
 * ld
 */
public class CodeStackException extends RuntimeException {

    private final ErrorCode errorCode;

    public CodeStackException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CodeStackException(Throwable e) {
        super(e.getMessage(), e);
        super.setStackTrace(e.getStackTrace());
        this.errorCode = SystemErrorCodeEnum.getSystemErrorCode(e);
    }

    public CodeStackException(UserErrorCodeEnum userErrorCodeEnum, Throwable e) {
        super(e.getMessage(), e);
        super.setStackTrace(e.getStackTrace());
        this.errorCode = new ErrorCode(userErrorCodeEnum);
        this.errorCode.setMsg(e.getMessage());
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
