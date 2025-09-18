package com.github.xuchen93.springboot.framework.web.support.extra;


import com.github.xuchen93.springboot.framework.base.convention.result.R;

/**
 * 对GlobalExceptionAdvice的增强
 */
public interface ExtraExceptionSupport<T extends Exception> {

	/**
	 * 支持拦截的异常
	 *
	 * @return
	 */
	Class<T> exception();


	/**
	 * 自定义返回的结果
	 * <p>默认不输出日志，如果需要输出日志，业务系统在result之前输出
	 */
	R<Object> result(T exception);

	default boolean supports(Exception e) {
		return exception().isInstance(e);
	}
}
