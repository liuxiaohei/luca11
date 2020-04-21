package org.ld.exception;

import org.ld.enums.SystemErrorCodeEnum;

/**
 * ld
 */
public class CodeStackException extends RuntimeException {

    private ErrorCode errorCode;

    public CodeStackException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CodeStackException(Throwable e) {
        super(e.getMessage(), e);
        super.setStackTrace(e.getStackTrace());
        this.errorCode = SystemErrorCodeEnum.getSystemError(e).errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
