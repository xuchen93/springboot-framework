package com.github.xuchen93.springboot.framework.base.support.aop;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.base.convention.enums.MethodStatsType;
import com.github.xuchen93.springboot.framework.base.convention.exception.BusinessException;
import com.github.xuchen93.springboot.framework.base.properties.BaseProperty;
import com.github.xuchen93.springboot.framework.base.support.annotation.LogAnnotation;
import com.github.xuchen93.springboot.framework.base.support.manager.MethodStatsManager;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${xuchen93.framework.base.log-aspect.enable-log:true} || ${xuchen93.framework.base.log-aspect.enable-stats:true}")
public class LogAnnotationAspect {

	private final BaseProperty baseProperty;


	private ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);


	@Around("@within(logAnnotation) || @annotation(logAnnotation)")
	public Object around(ProceedingJoinPoint joinPoint, LogAnnotation logAnnotation) throws Throwable {
		if (logAnnotation == null) {
			logAnnotation = joinPoint.getTarget().getClass().getAnnotation(LogAnnotation.class);
		}
		String uniqueKey = logAnnotation.uniqueKey();
		if (StrUtil.isBlank(uniqueKey)) {
			uniqueKey = CommonUtil.originClassName(joinPoint.getTarget()) + "." + joinPoint.getSignature().getName();
		}
		String baseLog = "";
		if (baseProperty.getLogAspect().isEnableLog()) {
			baseLog = StrUtil.format("{}|{}", uniqueKey, ThreadLocalRandom.current().nextInt(100, 1000));
			Object[] args = joinPoint.getArgs();
			List<Object> argsList = Arrays.stream(args).toList();
			String requestParams = objectMapper.writeValueAsString(argsList.size() == 1 ? argsList.get(0) : argsList);
			log.info("{} 入参:[{}]", baseLog, requestParams);
		}
		long currentTimeMillis = System.currentTimeMillis();
		boolean successFlag = true;
		String res = null;
		try {
			Object result = joinPoint.proceed();
			res = objectMapper.writeValueAsString(result);
			return result;
		} catch (BusinessException e) {
			res = e.getMessage();
			throw e;
		} catch (Exception e) {
			successFlag = false;
			res = e.getMessage();
			throw e;
		} finally {
			long costTime = System.currentTimeMillis() - currentTimeMillis;
			if (baseProperty.getLogAspect().isEnableLog()) {
				log.info("{} 耗时:[{} ms]:{}", baseLog, costTime, res);
			}
			if (baseProperty.getLogAspect().isEnableStats()) {
				if (successFlag) {
					MethodStatsManager.addSuccess(MethodStatsType.LOG_ANNOTATION.getType(), uniqueKey, costTime);
				} else {
					MethodStatsManager.addFail(MethodStatsType.LOG_ANNOTATION.getType(), uniqueKey, costTime);
				}
			}
		}
	}

	@SneakyThrows
	@PreDestroy
	public void perDestory() {
		if (baseProperty.getLogAspect().isEnableStats()) {
			MethodStatsManager.logByType(MethodStatsType.LOG_ANNOTATION.getType(), false);
		}
	}
}
