package com.github.xuchen93.springboot.framework.example.cache.redis;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import com.github.xuchen93.springboot.framework.cache.redis.RedisCacheProxy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class RedisTests {

	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	@Autowired
	RedisCacheProxy redisCacheProxy;

	@Test
	void redisTemplate() {
		String key = "testK";
		String value = "testV";
		redisTemplate.opsForValue().set(key, value);
		Object redisValue = redisTemplate.opsForValue().get(key);
		log.info("redis value: {}", redisValue);
		Assert.equals(value, redisValue);
		redisTemplate.delete(key);
		redisValue = redisTemplate.opsForValue().get(key);
		log.info("redis value: {}", redisValue);
		Assert.isNull(redisValue);
	}

	@Test
	void redisCacheProxy() {
		String key = "testK";
		String value = "testV";
		redisCacheProxy.setNx(key, value, 3, TimeUnit.SECONDS);
		IntStream.range(0, 5).forEach(index -> {
			ThreadUtil.sleep(1, TimeUnit.SECONDS);
			Object redisValue = redisTemplate.opsForValue().get(key);
			log.info("redis value: {}", redisValue);
		});
	}


}
