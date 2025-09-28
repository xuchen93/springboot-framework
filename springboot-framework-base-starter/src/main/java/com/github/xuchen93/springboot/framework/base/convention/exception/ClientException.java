package com.github.xuchen93.springboot.framework.base.convention.exception;

import com.github.xuchen93.springboot.framework.base.convention.error.BaseErrorCode;
import com.github.xuchen93.springboot.framework.base.convention.error.IErrorCode;
import lombok.ToString;

@ToString
public class ClientException extends AbstractException {
    public ClientException(String message, IErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    public ClientException(String message) {
        this(message, BaseErrorCode.CLIENT_ERROR, null);
    }

    public ClientException(IErrorCode errorCode) {
        this(null, errorCode, null);
    }

    public ClientException(String message, IErrorCode errorCode) {
        this(message, errorCode, null);
    }
}
