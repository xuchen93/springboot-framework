package com.github.xuchen93.springboot.framework.base.common;

import org.springframework.aop.support.AopUtils;

public class CommonUtil {

	private CommonUtil() {
	}

	public static String getCurrentMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	public static String getCurrentClassName() {
		return Thread.currentThread().getStackTrace()[2].getClassName();
	}

	public static String createBeanMessage(String beanName) {
		return "[xuchen93-framework]创建Bean：" + beanName;
	}

	public static String originClassName(Object bean) {
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		String simpleName = targetClass.getSimpleName();
		if (simpleName.contains("$")) {
			simpleName = simpleName.substring(0, simpleName.indexOf("$"));
		}
		return simpleName;
	}
}
