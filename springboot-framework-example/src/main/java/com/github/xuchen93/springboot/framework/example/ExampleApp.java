package com.github.xuchen93.springboot.framework.example;

import cn.hutool.extra.spring.EnableSpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.github.xuchen93"})
@EnableSpringUtil
public class ExampleApp {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(ExampleApp.class, args);
	}
}
