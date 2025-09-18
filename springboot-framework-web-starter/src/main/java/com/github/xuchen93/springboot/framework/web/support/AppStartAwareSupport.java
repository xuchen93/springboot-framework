package com.github.xuchen93.springboot.framework.web.support;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Slf4j
@Component
@ConditionalOnProperty(value = "xuchen93.framework.web.enable-app-start-log", havingValue = "true", matchIfMissing = true)
public class AppStartAwareSupport implements ApplicationContextAware, ApplicationListener<ApplicationReadyEvent> {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@SneakyThrows
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Environment env = applicationContext.getEnvironment();
		log.info("\n----------------------------------------------------------\n\t" +
						"Application '{}' is running! Access URLs:\n\t" +
						"Local: \t\thttp://localhost:{}{}\n\t" +
						"External: \thttp://{}:{}{}\n" +
						"----------------------------------------------------------",
				env.getProperty("spring.application.name"),
				env.getProperty("server.port", "8080"),
				env.getProperty("server.servlet.context-path", ""),
				InetAddress.getLocalHost().getHostAddress(),
				env.getProperty("server.port", "8080"),
				env.getProperty("server.servlet.context-path", ""));
	}
}
