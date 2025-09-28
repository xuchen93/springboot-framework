package com.github.xuchen93.springboot.framework.example.cache.redis;

import cn.hutool.core.thread.ThreadUtil;
import com.github.xuchen93.springboot.framework.cache.anno.RedisAnnotationLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisAnnotationLockExample {

	@RedisAnnotationLock(key = "simpleLock")
	public void simpleLock() {
		ThreadUtil.sleep(2, TimeUnit.SECONDS);
	}

	@RedisAnnotationLock(key = {"keyLock:", "#key", ":keyEnd"})
	public void keyLock(String key) {
		ThreadUtil.sleep(2, TimeUnit.SECONDS);
	}

	@RedisAnnotationLock(key = "waitTimeLock", waitTime = 1000)
	public void waitIgnoreLock() {
		ThreadUtil.sleep(2, TimeUnit.SECONDS);
	}

	@RedisAnnotationLock(key = "waitSuccessLock", waitTime = 5000)
	public void waitSuccessLock() {
		ThreadUtil.sleep(2, TimeUnit.SECONDS);
	}


	@RedisAnnotationLock(key = "leaseLock", leaseTime = 1000)
	public void leaseLock() {
		ThreadUtil.sleep(2, TimeUnit.SECONDS);
	}

	@RedisAnnotationLock(key = "failLockMsg", waitTime = 1000, errorMsg = {"failLockMsg:", "#lockKey", ":error"}, throwException = true)
	public void failLockMsg(String lockKey) {
		ThreadUtil.sleep(2, TimeUnit.SECONDS);
	}
}
