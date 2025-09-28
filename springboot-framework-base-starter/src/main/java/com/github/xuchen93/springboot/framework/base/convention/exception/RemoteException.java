package com.github.xuchen93.springboot.framework.base.convention.exception;

import com.github.xuchen93.springboot.framework.base.convention.error.BaseErrorCode;
import com.github.xuchen93.springboot.framework.base.convention.error.IErrorCode;
import lombok.ToString;

@ToString
public class RemoteException extends AbstractException {
    public RemoteException(String message, IErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    public RemoteException(String message) {
        this(message, BaseErrorCode.CLIENT_ERROR, null);
    }

    public RemoteException(IErrorCode errorCode) {
        this(null, errorCode, null);
    }

    public RemoteException(String message, IErrorCode errorCode) {
        this(message, errorCode, null);
    }
}
