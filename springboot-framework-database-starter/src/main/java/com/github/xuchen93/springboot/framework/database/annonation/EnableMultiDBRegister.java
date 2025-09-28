package com.github.xuchen93.springboot.framework.database.annonation;

import com.github.xuchen93.springboot.framework.database.configuration.MultiDBAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启多数据源动态注册
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MultiDBAutoRegister.class)
public @interface EnableMultiDBRegister {
}
