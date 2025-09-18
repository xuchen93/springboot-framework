package com.github.xuchen93.springboot.framework.base.configuration;


import com.github.xuchen93.springboot.framework.base.properties.BaseProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = BaseProperty.PROPERTY_PREFIX, name = "bean-create-aware", havingValue = "true", matchIfMissing = true)
public class BaseFreameworkBeanCreateAware extends FreameworkBeanCreateAware {

	public String packageNamePrefix() {
		return "com.github.xuchen93.springboot.framework.base";
	}

}
