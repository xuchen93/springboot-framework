package com.github.xuchen93.springboot.framework.cache.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisAnnotationLock {
	String[] key();// 锁的key

	long waitTime() default -1; // -1表示一直等待，0表示不等待，其他值表示等待时间，单位：毫秒

	long leaseTime() default -1; // 锁的初始过期时间，单位：毫秒 -1永不过期

	String[] errorMsg() default {}; // 错误信息，抛BusinessException

	boolean throwException() default false;
}
