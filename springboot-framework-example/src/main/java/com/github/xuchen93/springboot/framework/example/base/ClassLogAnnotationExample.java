package com.github.xuchen93.springboot.framework.example.base;

import cn.hutool.core.thread.ThreadUtil;
import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.base.convention.exception.BusinessException;
import com.github.xuchen93.springboot.framework.base.support.annotation.LogAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@LogAnnotation
public class ClassLogAnnotationExample {

	public String classMethod() {
		ThreadUtil.sleep(100, TimeUnit.MILLISECONDS);
		return CommonUtil.getCurrentMethodName();
	}

	public void classMethodVoid() {
		ThreadUtil.sleep(200, TimeUnit.MILLISECONDS);
	}

	public String classMethodParam(String param) {
		ThreadUtil.sleep(300, TimeUnit.MILLISECONDS);
		return CommonUtil.getCurrentMethodName() + "|" + param;
	}

	public String classMethodException() {
		throw new BusinessException(CommonUtil.getCurrentMethodName());
	}

}
