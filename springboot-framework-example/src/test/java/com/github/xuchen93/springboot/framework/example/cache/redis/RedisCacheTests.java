package com.github.xuchen93.springboot.framework.example.cache.redis;

import com.github.xuchen93.springboot.framework.example.cache.redis.RedisCacheExample;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@Slf4j
@SpringBootTest
class RedisCacheTests {

	@Autowired
	RedisCacheExample redisCacheExample;

	@Test
	void redisCacheTest() {
		for (int i = 0; i < 3; i++) {
			Map<Integer, String> map = redisCacheExample.getMap(5);
			log.info("map: {}", map);
		}
	}

}
