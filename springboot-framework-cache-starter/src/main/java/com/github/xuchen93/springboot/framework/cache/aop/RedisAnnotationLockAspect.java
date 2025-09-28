
package com.github.xuchen93.springboot.framework.cache.aop;

import com.github.xuchen93.springboot.framework.base.convention.exception.BusinessException;
import com.github.xuchen93.springboot.framework.cache.anno.RedisAnnotationLock;
import com.github.xuchen93.springboot.framework.cache.properties.CacheProperty;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.concurrent.TimeUnit;

@Aspect
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = CacheProperty.PROPERTY_PREFIX, name = "redis.enable-annotation-lock", havingValue = "true", matchIfMissing = true)
public class RedisAnnotationLockAspect {
	private final RedissonClient redissonClient;
	private final ExpressionParser parser = new SpelExpressionParser();
	private final CacheProperty cacheProperty;


	@Around("@annotation(redisAnnotationLock)")
	public Object around(ProceedingJoinPoint joinPoint, RedisAnnotationLock redisAnnotationLock) throws Throwable {
		logDetail("进入redis锁切面");
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();

		// 获取redis锁的key、锁的过期时间和锁的续期时间
		String[] keyArray = redisAnnotationLock.key();
		long leaseTime = redisAnnotationLock.leaseTime();
		long waitTime = redisAnnotationLock.waitTime();
		String[] errorMsgArray = redisAnnotationLock.errorMsg();

		// 解析SpEL表达式
		StandardEvaluationContext context = new StandardEvaluationContext();
		Object[] args = joinPoint.getArgs();
		String[] parameterNames = signature.getParameterNames();
		for (int i = 0; i < args.length; i++) {
			context.setVariable(parameterNames[i], args[i]);
		}
		String keyExpression = parseExpression(keyArray);
		String errorMsgExpression = parseExpression(errorMsgArray);

		String key = parser.parseExpression(keyExpression).getValue(context, String.class);
		logDetail("解析redis锁key：{}", key);
		// 获取redis锁
		RLock lock = redissonClient.getLock(key);
		boolean isLocked = false;
		try {
			if (waitTime >= 0) {
				isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
			} else {
				lock.lock(leaseTime, TimeUnit.MILLISECONDS);
				isLocked = true;
			}
		} catch (Exception e) {
			log.error("获取：{} redis锁异常", key, e);
			if (redisAnnotationLock.throwException()) {
				String errMsg = parser.parseExpression(errorMsgExpression).getValue(context, String.class);
				logDetail("获取：{} redis锁异常，抛出异常：{}", key, errMsg);
				throw new BusinessException(errMsg);
			}
		}
		if (!isLocked) {
			if (redisAnnotationLock.throwException()) {
				logDetail("获取：{} 锁失败，抛异常", key);
				if (StringUtil.isNotBlank(errorMsgExpression)) {
					String errMsg = parser.parseExpression(errorMsgExpression).getValue(context, String.class);
					if (StringUtil.isNotBlank(errMsg)) {
						throw new BusinessException(errMsg);
					}
				} else {
					throw new IllegalStateException("获取锁失败! key: " + key);
				}
			} else {
				logDetail("获取：{} 锁失败，忽略执行", key);
			}
			logDetail("获取锁失败，不抛出异常，不执行后续操作");
			return null;
		}
		try {
			logDetail("获取：{} 锁成功", key);
			// 执行业务逻辑
			return joinPoint.proceed();
		} catch (Exception e) {
			logDetail("执行过程触发异常，等待锁自动释放：{}", e.getMessage());
			throw e;
		} finally {
			try {
				// 只有当当前线程持有锁时才释放锁
				if (lock.isHeldByCurrentThread()) {
					lock.unlock();
					logDetail("释放：{} 锁成功", key);
				} else {
					log.warn("当前线程锁已过期，忽略释放: {}", key);
				}
			} catch (Exception e) {
				log.error("当前线程释放锁失败: {}", key, e);
			}
		}
	}

	private String parseExpression(String[] strArray) {
		if (strArray == null || strArray.length == 0) {
			return null;
		}
		// 解析keyArray的每部分
		StringBuilder keyExpression = new StringBuilder();
		for (int i = 0; i < strArray.length; i++) {
			String keyStr = strArray[i];
			// 如果是含有冒号的特殊字符串常量，则用单引号包围
			if (keyStr.startsWith("#")) {
				keyExpression.append(keyStr);
			} else {
				keyExpression.append("'");
				keyExpression.append(keyStr);
				keyExpression.append("'");
			}
			if (i < strArray.length - 1) {
				keyExpression.append("+");
			}
		}
		return keyExpression.toString();
	}

	private void logDetail(String message, String... args) {
		if (cacheProperty.getRedis().isEnableLockDetail()) {
			log.info(message, args);
		}
	}
}
