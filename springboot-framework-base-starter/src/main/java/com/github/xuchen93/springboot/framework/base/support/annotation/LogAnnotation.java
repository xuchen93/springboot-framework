package com.github.xuchen93.springboot.framework.base.support.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAnnotation {

	/**
	 * 默认取className.methodName
	 * @return
	 */
	String uniqueKey() default "";
}
