package com.github.xuchen93.springboot.framework.example.base;


import com.github.xuchen93.springboot.framework.example.base.ClassLogAnnotationExample;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class ClassLogAnnotationTest {

	@Autowired
	ClassLogAnnotationExample classLogAnnotationExample;

	@Test
	public void classMethod() {
		classLogAnnotationExample.classMethod();
		classLogAnnotationExample.classMethod();
	}

	@Test
	public void classMethodVoid() {
		classLogAnnotationExample.classMethodVoid();
	}

	@Test
	public void classMethodParam() {
		classLogAnnotationExample.classMethodParam("testParam");
	}

//	@Test
//	public void classMethodException() {
//		classLogAnnotationExample.classMethodException();
//	}

	@Test
	public void catchClassMethodException() {
		try {
			classLogAnnotationExample.classMethodException();
		} catch (Exception e) {
			log.error("catch exception：{}", e.getMessage());
		}
	}
}
