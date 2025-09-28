package com.github.xuchen93.springboot.framework.example.base;


import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
public class MethodStatsConfigConsumeTest {

	@Autowired
	ClassLogAnnotationExample classLogAnnotationExample;

	@Test
	public void classMethod() {
		IntStream.range(0, 20).forEach(i -> {
			classLogAnnotationExample.classMethod();
			classLogAnnotationExample.classMethodVoid();
			ThreadUtil.sleep(1000);
		});
	}
}
