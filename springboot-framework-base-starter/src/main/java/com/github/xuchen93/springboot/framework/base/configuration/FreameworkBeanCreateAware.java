package com.github.xuchen93.springboot.framework.base.configuration;

import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j
public abstract class FreameworkBeanCreateAware implements BeanPostProcessor {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean.getClass().getName().startsWith(packageNamePrefix())) {
			log.info("[xuchen93-framework]创建Bean：class=[{}]，name=[{}]", CommonUtil.originClassName(bean), beanName);
		}
		return bean;
	}

	public abstract String packageNamePrefix();

}
