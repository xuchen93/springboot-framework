package com.github.xuchen93.springboot.framework.base.convention.error;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BaseErrorCode implements IErrorCode {
	SUCCESS(200, "成功"),
	CLIENT_ERROR(420, "客户端异常"),
	BUSINESS_ERROR(421, "业务异常"),
	REMOTE_ERROR(422, "调用第三方服务异常"),

	SERVICE_ERROR(520, "系统异常");

	private final int code;
	private final String message;

	@Override
	public int code() {
		return code;
	}

	@Override
	public String message() {
		return message;
	}
}
