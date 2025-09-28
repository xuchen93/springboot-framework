package com.github.xuchen93.springboot.framework.cache.configuration;


import com.github.xuchen93.springboot.framework.base.configuration.FreameworkBeanCreateAware;
import com.github.xuchen93.springboot.framework.cache.properties.CacheProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = CacheProperty.PROPERTY_PREFIX, name = "bean-create-aware", havingValue = "true", matchIfMissing = true)
public class CacheFreameworkBeanCreateAware extends FreameworkBeanCreateAware {

	public String packageNamePrefix() {
		return "com.github.xuchen93.springboot.framework.cache";
	}

}
