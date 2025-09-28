package com.github.xuchen93.springboot.framework.web.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.xuchen93.springboot.framework.base.convention.enums.MethodStatsType;
import com.github.xuchen93.springboot.framework.base.convention.exception.BusinessException;
import com.github.xuchen93.springboot.framework.base.convention.exception.HttpErrorException;
import com.github.xuchen93.springboot.framework.base.convention.result.R;
import com.github.xuchen93.springboot.framework.base.support.manager.MethodStatsManager;
import com.github.xuchen93.springboot.framework.web.properties.WebProperty;
import com.github.xuchen93.springboot.framework.web.support.extra.ExtraExceptionSupport;
import com.github.xuchen93.springboot.framework.web.support.manager.RequestAspectManager;
import com.github.xuchen93.springboot.framework.web.support.model.RequestAspectKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component("defaultExceptionAdvice")
@RestControllerAdvice
@ConditionalOnProperty(value = "xuchen93.framework.web.enable-exception-advice", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class GlobalExceptionAdviceSupport {

	private ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);


	private final WebProperty webProperty;

	private final List<ExtraExceptionSupport> extraExceptionSupportList;

	@ExceptionHandler(value = BusinessException.class)
	public R<Object> businessException(BusinessException exception) {
		return handleRequestLog(R.fail(exception.getMessage()));
	}

	@ExceptionHandler(value = IllegalArgumentException.class)
	public R<Object> illegalArgumentException(IllegalArgumentException exception) {
		return handleRequestLog(R.fail("非法的请求参数"));
	}

	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = HttpErrorException.class)
	public R<Object> httpErrorException(HttpErrorException exception) {
		return handleRequestLog(R.fail(exception.getStatusCode(), exception.getMessage()));
	}


	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public R<Object> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception) {
		String errorMsg = exception.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));
		return handleRequestLog(R.fail(errorMsg));
	}


	@ExceptionHandler(BindException.class)
	public R<Object> handleBindException(BindException exception) {
		FieldError fieldError = exception.getFieldError();
		if (fieldError != null) {
			return handleRequestLog(R.fail(fieldError.getDefaultMessage()));
		}
		return handleRequestLog(R.fail("参数绑定错误"));
	}

	@ExceptionHandler(value = Exception.class)
	public R<Object> exceptionHandler(Exception exception) {
		if (!CollectionUtils.isEmpty(extraExceptionSupportList)) {
			Optional<ExtraExceptionSupport> optional = extraExceptionSupportList.stream()
					.filter(advice -> advice.supports(exception))
					.findFirst();
			if (optional.isPresent()) {
				return handleRequestLog(optional.get().result(exception));
			}
		}
		log.error("系统错误", exception);
		return handleRequestLog(R.fail("系统错误"));
	}

	@SneakyThrows
	private R<Object> handleRequestLog(R<Object> response) {
		RequestAspectKey requestAspectKey = RequestAspectManager.consumeRequestAspectKey();
		long costTime = System.currentTimeMillis() - requestAspectKey.getStartTime();
		if (webProperty.getRequestAspect().isEnableLog()) {
			log.info("{} 耗时:[{} ms] 出参:[{}]", requestAspectKey.generateBaseLog(), costTime, objectMapper.writeValueAsString(response));
		}
		if (webProperty.getRequestAspect().isEnableStats()) {
			MethodStatsManager.addFail(MethodStatsType.REQUEST.getType(), requestAspectKey.getUniqueKey(), costTime);
		}
		return response;
	}
}
