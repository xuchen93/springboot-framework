package com.github.xuchen93.springboot.framework.cache.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(CacheProperty.PROPERTY_PREFIX)
public class CacheProperty {
	public static final String PROPERTY_PREFIX = "xuchen93.framework.cache";

	private boolean beanCreateAware = true;

	private CaffeineCacheManagerProperties caffeine = new CaffeineCacheManagerProperties();

	private RedisProperties redis = new RedisProperties();

	/**
	 * Caffeine缓存配置属性类
	 * <p>
	 * 该类用于封装Caffeine缓存的相关配置参数，通过配置文件可灵活控制缓存行为。
	 *
	 * <h3>依赖说明</h3>
	 * 使用此类需在项目中引入Caffeine依赖：
	 * <pre>
	 * &lt;dependency&gt;
	 *     &lt;groupId&gt;com.github.ben-manes.caffeine&lt;/groupId&gt;
	 *     &lt;artifactId&gt;caffeine&lt;/artifactId&gt;
	 *     &lt;version&gt;${caffeine.version}&lt;/version&gt;
	 * &lt;/dependency&gt;
	 * </pre>
	 *
	 * <h3>配置项说明</h3>
	 * <ul>
	 *     <li>{@code enable}: 是否启用Caffeine缓存，默认值为true</li>
	 *     <li>{@code enableCacheManager}: 是否启用缓存管理器，默认值为true</li>
	 * </ul>
	 */
	@Data
	public static class CaffeineCacheManagerProperties {
		/**
		 * 是否启用Caffeine缓存
		 */
		private boolean enableCacheManager = true;
		/**
		 * Caffeine的CacheManager是否是primary
		 */
		private boolean primary = true;
		private int initialCapacity = -1;
		private int maximumSize = -1;
		private long expireAfterWriteSeconds = -1;
		private long expireAfterAccessSeconds = -1;
		private long refreshAfterWriteSeconds = -1;
	}

	@Data
	public static class RedisProperties {
		/**
		 * 是否启用Redis缓存
		 */
		private boolean enable = true;
		/**
		 * 是否启用redis注解锁
		 */
		private boolean enableAnnotationLock = true;
		/**
		 * 打印注解锁的详细日志
		 */
		private boolean enableLockDetail = false;

		private RedisCacheProperties cacheManager = new RedisCacheProperties();

	}

	@Data
	public static class RedisCacheProperties {
		/**
		 * 是否启用redis缓存
		 */
		private boolean enable = false;
		/**
		 * redis缓存的key前缀
		 */
		private String keyPrefix = "cache:";
		/**
		 * Redis的CacheManager是否是primary
		 */
		private boolean primary = false;

		/**
		 * 过期时间，单位秒。默认1小时
		 */
		private long expireSeconds = 3600;
	}

}
