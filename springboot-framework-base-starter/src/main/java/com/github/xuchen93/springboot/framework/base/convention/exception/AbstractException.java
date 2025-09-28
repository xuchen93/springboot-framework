package com.github.xuchen93.springboot.framework.base.convention.exception;

import com.github.xuchen93.springboot.framework.base.convention.error.IErrorCode;
import lombok.Getter;

import java.util.Optional;

@Getter
public abstract class AbstractException extends RuntimeException {
	public final int code;
	public final String message;

	public AbstractException(String message, IErrorCode errorCode, Throwable cause) {
		super(message, cause);
		this.code = errorCode.code();
		this.message = Optional.ofNullable(message).orElse(errorCode.message());
	}

	public AbstractException(IErrorCode errorCode) {
		this(errorCode.code(), errorCode.message());
	}

	public AbstractException(int code, String message) {
		this.code = code;
		this.message = message;
	}

}
