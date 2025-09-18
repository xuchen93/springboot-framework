package com.github.xuchen93.springboot.framework.base.convention.exception;

import cn.hutool.core.util.StrUtil;
import com.github.xuchen93.springboot.framework.base.convention.error.BaseErrorCode;
import com.github.xuchen93.springboot.framework.base.convention.error.IErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends AbstractException {

	public BusinessException(String message, Object... params) {
		this(StrUtil.format(message, params));
	}

	public BusinessException(String message) {
		super(BaseErrorCode.BUSINESS_ERROR.code(), message);
	}


	public BusinessException(int code, String message) {
		super(code, message);
	}


	public BusinessException(IErrorCode errorCode) {
		super(errorCode);
	}
}
