package com.github.xuchen93.springboot.framework.example.web;


import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class ExceptionControllerTest {

	private static final String BASE_URL = "http://localhost:8080/example/exception/";

	public static void main(String[] args) {
		ExceptionControllerTest controllerTest = new ExceptionControllerTest();
		controllerTest.businessException();
		controllerTest.businessExceptionWait();
		controllerTest.unknownException();
	}

	@Test
	public void businessException() {
		HttpResponse response = HttpUtil.createGet(BASE_URL + "businessException")
				.execute();
		log.info(response.body());
	}


	@Test
	public void businessExceptionWait() {
		HttpResponse response = HttpUtil.createGet(BASE_URL + "businessExceptionWait")
				.execute();
		log.info(response.body());
	}

	@Test
	public void unknownException() {
		HttpResponse response = HttpUtil.createGet(BASE_URL + "unknownException")
				.execute();
		log.info(response.body());
	}
}
