package com.github.xuchen93.springboot.framework.example.cache.caffeine;

import cn.hutool.core.thread.ThreadUtil;
import com.github.xuchen93.springboot.framework.example.cache.caffeine.CaffeineCacheExample;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class CaffeineCacheTests {

	@Autowired
	CaffeineCacheExample cacheExample;

	@Test
	void cache() {
		for (int i = 0; i < 2; i++) {
			Map<Integer, String> map = cacheExample.cache(1);
			log.info("index: {}, map: {}", i, map);
		}
	}

	@Test
	void cache2() {
		for (int i = 0; i < 5; i++) {
			Map<Integer, String> map = cacheExample.cache(i);
			log.info("index: {}, map: {}", i, map);
		}
	}

	@Test
	void cacheWriteExpire() {
		for (int i = 0; i < 10; i++) {
			Map<Integer, String> map = cacheExample.cacheWriteExpire(1);
			log.info("index: {}, map: {}", i, map);
			ThreadUtil.sleep(2, TimeUnit.SECONDS);
		}
	}

	@Test
	void cacheAccessExpire() {
		for (int i = 0; i < 7; i++) {
			Map<Integer, String> map = cacheExample.cacheAccessExpire(1);
			log.info("index: {}, map: {}", i, map);
			ThreadUtil.sleep(i, TimeUnit.SECONDS);
		}
	}

}
