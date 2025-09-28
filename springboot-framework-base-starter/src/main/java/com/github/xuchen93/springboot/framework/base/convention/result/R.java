package com.github.xuchen93.springboot.framework.base.convention.result;

import com.github.xuchen93.springboot.framework.base.convention.error.BaseErrorCode;
import com.github.xuchen93.springboot.framework.base.convention.error.IErrorCode;
import lombok.Data;

import java.io.Serializable;


@Data
public class R<T> implements Serializable {

	private int code;

	private String msg;

	private T data;


	public R() {

	}

	private R(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	public static <T> R<T> success() {
		return success(null);
	}

	public static <T> R<T> success(T data) {
		return success(BaseErrorCode.SUCCESS.code(), data);
	}

	public static <T> R<T> success(int code, T data) {
		return new R<>(code, "success", data);
	}

	public static <T> R<T> successMsg(String msg) {
		return new R<>(BaseErrorCode.SUCCESS.code(), msg, null);
	}

	public static <T> R<T> success(int code, String msg, T data) {
		return new R<>(code, msg, data);
	}

	public static <T> R<T> fail() {
		return fail("error");
	}

	public static <T> R<T> fail(String msg) {
		return fail(BaseErrorCode.BUSINESS_ERROR.code(), msg);
	}

	public static <T> R<T> fail(IErrorCode errorCode) {
		return fail(errorCode.code(), errorCode.message());
	}

	public static <T> R<T> fail(int code, String msg) {
		return new R<>(code, msg, null);
	}

	public static <T> R<T> fail(int code, String msg, T data) {
		return new R<>(code, msg, data);
	}
}
