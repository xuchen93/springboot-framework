package com.github.xuchen93.springboot.framework.web.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.xuchen93.springboot.framework.base.convention.enums.MethodStatsType;
import com.github.xuchen93.springboot.framework.base.support.manager.MethodStatsManager;
import com.github.xuchen93.springboot.framework.web.properties.WebProperty;
import com.github.xuchen93.springboot.framework.web.support.manager.RequestAspectManager;
import com.github.xuchen93.springboot.framework.web.support.model.RequestAspectKey;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${xuchen93.framework.web.request-aspect.enable-log:true} || ${xuchen93.framework.web.request-aspect.enable-stats:true}")
public class RequestAspectSupport {

	private final WebProperty webProperty;

	private ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	private String getJsonParams(Map<String, String[]> map) throws JsonProcessingException {
		Map<String, List<String>> paramsMap = new HashMap<>();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			paramsMap.put(entry.getKey(), Arrays.stream(entry.getValue()).toList());
		}
		return objectMapper.writeValueAsString(paramsMap);
	}

	@Pointcut("execution(* *..controller..*.*(..))")
	public void controllerPointCut() {

	}

	@Around("controllerPointCut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		String uniqueKey = getUniqueKey(joinPoint);
		String requestParams;
		if ("get".equalsIgnoreCase(request.getMethod())) {
			requestParams = getJsonParams(request.getParameterMap());
		} else {
			ServletInputStream inputStream = request.getInputStream();
			if (inputStream.isFinished()) {//兼容requestBody只能读一次
				Object[] args = joinPoint.getArgs();
				List<Object> argsList = Arrays.stream(args).filter(i -> !(i instanceof ServletRequest)).collect(Collectors.toList());
				requestParams = objectMapper.writeValueAsString(argsList.size() == 1 ? argsList.get(0) : argsList);
			} else {
				requestParams = convertInputStreamToString(inputStream);
			}
		}
		RequestAspectKey requestAspectKey = RequestAspectManager.setRequestAspectKey(System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(100, 1000), getClientIp(request), uniqueKey);
		if (webProperty.getRequestAspect().isEnableLog()) {
			log.info("{}的[{}]请求[{}]入参:[{}]", requestAspectKey.generateBaseLog(), request.getMethod(), request.getRequestURI(), requestParams);
		}
		Object result = joinPoint.proceed();
		RequestAspectManager.clear();
		long costTime = System.currentTimeMillis() - requestAspectKey.getStartTime();
		if (webProperty.getRequestAspect().isEnableLog()) {
			log.info("{} 耗时:[{} ms] 出参:[{}]", requestAspectKey.generateBaseLog(), costTime, objectMapper.writeValueAsString(result));
		}
		if (webProperty.getRequestAspect().isEnableStats()) {
			MethodStatsManager.addSuccess(MethodStatsType.REQUEST.getType(), uniqueKey, costTime);
		}
		return result;
	}


	private List<Class<? extends Annotation>> mappingAnnotationClassList = List.of(GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class, RequestMapping.class);
	private Map<Method, String> methodUniqueKeyMap = new ConcurrentHashMap<>();

	private String getUniqueKey(ProceedingJoinPoint joinPoint) {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		return methodUniqueKeyMap.computeIfAbsent(method, m -> {
			RequestMapping classRequestMapping = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequestMapping.class);
			List<String> requestUriList = new ArrayList<>();
			StringBuilder methodName = new StringBuilder();
			if (classRequestMapping != null) {
				requestUriList.add(classRequestMapping.path()[0]);
			}
			mappingAnnotationClassList.stream()
					.map(annotation -> AnnotationUtils.findAnnotation(method, annotation))
					.filter(Objects::nonNull)
					.findFirst()
					.ifPresent(annotation -> {
						Map<String, Object> map = AnnotationUtils.getAnnotationAttributes(annotation);
						if (map.get("path") != null && ((String[]) map.get("path")).length > 0) {
							requestUriList.add(((String[]) map.get("path"))[0]);
						} else if (map.get("value") != null && ((String[]) map.get("value")).length > 0) {
							requestUriList.add(((String[]) map.get("value"))[0]);
						}
						methodName.append(annotation.annotationType().getSimpleName()
								.replace("Mapping", ""));
					});

			String requestUri = requestUriList.stream().map(uri -> {
				if (!uri.startsWith("/")) {
					return "/" + uri;
				}
				return uri;
			}).collect(Collectors.joining(""));
			return methodName + "|" + requestUri;
		});
	}

	private String getClientIp(HttpServletRequest request) {
		String clientIp = request.getHeader("X-Forwarded-For");
		if (clientIp == null) {
			clientIp = request.getRemoteAddr();
		}
		return clientIp;
	}


	private String convertInputStreamToString(InputStream inputStream) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
		}
		return stringBuilder.toString();
	}


	@SneakyThrows
	@PreDestroy
	public void perDestory() {
		if (webProperty.getRequestAspect().isEnableStats()) {
			MethodStatsManager.logByType(MethodStatsType.REQUEST.getType(), false);
		}
	}
}

