package com.github.xuchen93.springboot.framework.base.configuration;

import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 感知当前依赖包[com.github.xuchen93.springboot.framework]的Bean创建，并打印日志
 */
@Slf4j
@Component
public class FreameworkBeanCreateAware implements BeanPostProcessor {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean.getClass().getName().startsWith("com.github.xuchen93.springboot.framework")) {
			log.info("[xuchen93-framework]创建Bean：class=[{}]，name=[{}]", CommonUtil.originClassName(bean), beanName);
		}
		return bean;
	}

}
