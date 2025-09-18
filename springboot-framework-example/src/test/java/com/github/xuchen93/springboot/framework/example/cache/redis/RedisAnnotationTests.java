package com.github.xuchen93.springboot.framework.example.cache.redis;

import cn.hutool.core.thread.ThreadUtil;
import com.github.xuchen93.springboot.framework.example.cache.redis.RedisAnnotationLockExample;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class RedisAnnotationTests {

	@Autowired
	RedisAnnotationLockExample redisAnnotationLockExample;

	private int threadCount = 5;
	private ExecutorService executorService = ThreadUtil.newFixedExecutor(threadCount, "redisThread", true);

	@AfterEach
	void tearDown() throws InterruptedException {
		executorService.shutdown(); // 启动有序关闭
		// 等待所有任务完成，设置超时时间（根据实际情况调整）
		if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
			executorService.shutdownNow(); // 超时强制关闭
			log.warn("线程池任务超时未完成，已强制关闭");
		} else {
			log.info("线程池所有任务已执行完成");
		}
	}


	@Test
	void simpleLock() {
		IntStream.range(0, threadCount).forEach(index -> executorService.execute(() -> redisAnnotationLockExample.simpleLock()));
		executorService.shutdown();
	}


	@Test
	void keyLock() {
		IntStream.range(0, threadCount)
				.forEach(index -> {
					executorService.execute(() -> {
						redisAnnotationLockExample.keyLock("key" + index % 2);
					});
				});
	}

	@Test
	void waitIgnoreLock() {
		IntStream.range(0, threadCount)
				.forEach(index -> {
					executorService.execute(() -> {
						redisAnnotationLockExample.waitIgnoreLock();
					});
				});
	}

	@Test
	void waitSuccessLock() {
		IntStream.range(0, threadCount)
				.forEach(index -> {
					executorService.execute(() -> {
						redisAnnotationLockExample.waitSuccessLock();
					});
				});
	}

	@Test
	void leaseLock() {
		IntStream.range(0, threadCount)
				.forEach(index -> {
					executorService.execute(() -> {
						redisAnnotationLockExample.leaseLock();
					});
				});
	}

	@Test
	void failLockMsg() {
		IntStream.range(0, threadCount)
				.forEach(index -> {
					executorService.execute(() -> {
						redisAnnotationLockExample.failLockMsg("key" + index % 2);
					});
				});
	}
}
