package com.github.xuchen93.springboot.framework.example.web;


import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class MethodControllerTest {

	private static final String BASE_URL = "http://localhost:8080/example/";

	public static void main(String[] args) {
		MethodControllerTest controllerTest = new MethodControllerTest();
		controllerTest.testAll();
		controllerTest.testGet();
		controllerTest.testGetId();
		controllerTest.testPost();
		controllerTest.testPostBody();
	}

	@Test
	public void testAll() {
		HttpResponse response = HttpUtil.createGet(BASE_URL + "all")
				.execute();
		log.info(response.body());
	}


	@Test
	public void testGet() {
		HttpResponse response = HttpUtil.createGet(BASE_URL + "get")
				.execute();
		log.info("testGet response: {}", response.body());
	}

	@Test
	public void testGetId() {
		String id = "123";  // 测试用例ID
		HttpResponse response = HttpUtil.createGet(BASE_URL + "get/" + id)
				.execute();
		log.info("testGetId response: {}", response.body());
	}

	@Test
	public void testPost() {
		HttpResponse response = HttpUtil.createPost(BASE_URL + "post")
				.execute();
		log.info("testPost response: {}", response.body());
	}

	@Test
	public void testPostBody() {
		// 构建请求体
		JSONObject requestBody = new JSONObject();
		requestBody.put("name", "test");
		requestBody.put("value", "123");

		HttpResponse response = HttpUtil.createPost(BASE_URL + "postBody")
				.body(requestBody.toString())
				.contentType("application/json")  // 指定JSON格式
				.execute();
		log.info("testPostBody response: {}", response.body());
	}
}
