package com.whq.mvc.common;

public class BusinessException extends RuntimeException {

    private ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public BusinessException() {
        super();
    }

    public BusinessException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
