package com.github.xuchen93.springboot.framework.web.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("xuchen93.framework.web")
public class WebProperty {
	private RequestAspect requestAspect = new RequestAspect();
	private boolean enableExceptionAdvice = true;
	private boolean enableAppStartLog = true;

	@Data
	public static class RequestAspect {
		private boolean enableLog = true;
		private boolean enableStats = true;

	}
}
