package com.github.xuchen93.springboot.framework.base.convention.error;

public interface IErrorCode {
	/**
	 * 错误码
	 */
	int code();

	/**
	 * 错误信息
	 */
	String message();
}
