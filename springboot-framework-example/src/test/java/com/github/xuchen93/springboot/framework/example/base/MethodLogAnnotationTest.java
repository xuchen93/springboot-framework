package com.github.xuchen93.springboot.framework.example.base;


import com.github.xuchen93.springboot.framework.example.base.MethodLogAnnotationExample;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class MethodLogAnnotationTest {

	@Autowired
	MethodLogAnnotationExample methodLogAnnotationExample;

	@Test
	public void method() {
		methodLogAnnotationExample.method();
	}

	@Test
	public void methodVoid() {
		methodLogAnnotationExample.methodVoid();
	}


	@Test
	public void methodException() {
		try {
			methodLogAnnotationExample.methodException();
		} catch (Exception e) {
			log.error("catch exception：{}", e.getMessage());
		}
	}
}
