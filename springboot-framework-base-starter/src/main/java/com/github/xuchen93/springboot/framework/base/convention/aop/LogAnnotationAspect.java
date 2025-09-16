package com.github.xuchen93.springboot.framework.base.convention.aop;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.base.convention.annotation.LogAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Aspect
@Slf4j
@Component
public class LogAnnotationAspect {

	private ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);


	@Around("@annotation(logAnnotation) || @within(logAnnotation)")
	public Object around(ProceedingJoinPoint joinPoint, LogAnnotation logAnnotation) throws Throwable {
		String baseLog = StrUtil.format("{}.{}()|{}", CommonUtil.originClassName(joinPoint.getTarget()), joinPoint.getSignature().getName(), ThreadLocalRandom.current().nextInt(100, 1000));
		Object[] args = joinPoint.getArgs();
		List<Object> argsList = Arrays.stream(args).toList();
		String requestParams = objectMapper.writeValueAsString(argsList.size() == 1 ? argsList.get(0) : argsList);
		log.info("{} 入参:[{}]", baseLog, requestParams);
		long currentTimeMillis = System.currentTimeMillis();
		Object result = joinPoint.proceed();
		log.info("{} 耗时:[{} ms]:[{}]", baseLog, System.currentTimeMillis() - currentTimeMillis, objectMapper.writeValueAsString(result));
		return result;
	}
}
