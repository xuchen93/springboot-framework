package com.github.xuchen93.springboot.framework.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(BaseProperty.PROPERTY_PREFIX)
public class BaseProperty {
	public static final String PROPERTY_PREFIX = "xuchen93.framework.base";

	private boolean beanCreateAware = true;

	private LogAspect logAspect = new LogAspect();
	private MethodStatsConfig methodStatsConfig = new MethodStatsConfig();


	@Data
	public static class LogAspect {
		private boolean enableLog = true;
		private boolean enableStats = true;
	}

	@Data
	public static class MethodStatsConfig {
		private boolean enableConsume = true;
		private int consumeSeconds = 3600;
	}
}
