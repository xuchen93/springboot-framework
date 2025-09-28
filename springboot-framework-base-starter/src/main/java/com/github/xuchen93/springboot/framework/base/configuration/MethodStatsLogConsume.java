package com.github.xuchen93.springboot.framework.base.configuration;

import com.github.xuchen93.springboot.framework.base.convention.enums.MethodStatsType;
import com.github.xuchen93.springboot.framework.base.properties.BaseProperty;
import com.github.xuchen93.springboot.framework.base.support.manager.MethodStatsManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = BaseProperty.PROPERTY_PREFIX, name = "method-stats.enable-consume", havingValue = "true")
public class MethodStatsLogConsume implements SmartInitializingSingleton {

	private final BaseProperty baseProperty;

	@Override
	public void afterSingletonsInstantiated() {
		Timer timer = new Timer("method-stats-consume-timer");
		long consumePeriod = baseProperty.getMethodStatsConfig().getConsumeSeconds() * 1000L;
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				MethodStatsType.getAllTypes().forEach(type -> MethodStatsManager.logByType(type, true));
			}
		}, consumePeriod, consumePeriod);
	}
}
