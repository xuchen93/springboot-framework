package com.github.xuchen93.springboot.framework.cache.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.cache.constants.CacheConstants;
import com.github.xuchen93.springboot.framework.cache.properties.CacheProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Slf4j
@ConditionalOnProperty(prefix = CacheProperty.PROPERTY_PREFIX, name = {"caffeine.enable-cache-manager", "redis.cache-manager.enable"}, havingValue = "true", matchIfMissing = true)
@EnableCaching
@Component
public class CacheManagerRegister implements ImportBeanDefinitionRegistrar, BeanFactoryAware {


	private ConfigurableEnvironment environment;
	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		this.environment = beanFactory.getBean(ConfigurableEnvironment.class);
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		CacheProperty cacheProperty = Binder.get(this.environment)
				.bind(CacheProperty.PROPERTY_PREFIX, CacheProperty.class)
				.orElseThrow(() -> new IllegalArgumentException(String.format("无[%s]配置信息", CacheProperty.PROPERTY_PREFIX)));
		if (cacheProperty.getRedis().getCacheManager().isEnable()) {
			registerRedisCacheManager(registry, cacheProperty.getRedis());
		}
		if (cacheProperty.getCaffeine().isEnableCacheManager()) {
			registerCaffeineCacheManager(registry, cacheProperty.getCaffeine());
		}
	}

	private void registerCaffeineCacheManager(BeanDefinitionRegistry registry, CacheProperty.CaffeineCacheManagerProperties caffeineConfig) {
		String beanName = CacheConstants.CACHE_MANAGER_CAFFEINE;
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CacheManager.class, () -> {
			// 1. 构建CaffeineCacheManager
			CaffeineCacheManager cacheManager = new CaffeineCacheManager();
			cacheManager.setAllowNullValues(true);
			Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
			if (caffeineConfig.getInitialCapacity() > 0) {
				caffeine.initialCapacity(caffeineConfig.getInitialCapacity());
			}
			if (caffeineConfig.getMaximumSize() > 0) {
				caffeine.maximumSize(caffeineConfig.getMaximumSize());
			}
			if (caffeineConfig.getExpireAfterWriteSeconds() > 0) {
				caffeine.expireAfterWrite(Duration.ofSeconds(caffeineConfig.getExpireAfterWriteSeconds()));
			}
			if (caffeineConfig.getExpireAfterAccessSeconds() > 0) {
				caffeine.expireAfterAccess(Duration.ofSeconds(caffeineConfig.getExpireAfterAccessSeconds()));
			}
			if (caffeineConfig.getRefreshAfterWriteSeconds() > 0) {
				caffeine.refreshAfterWrite(Duration.ofSeconds(caffeineConfig.getRefreshAfterWriteSeconds()));
			}
			cacheManager.setCaffeine(caffeine);
			return cacheManager;
		});
		builder.setPrimary(caffeineConfig.isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		log.info(CommonUtil.createBeanMessage(beanName));
	}

	private void registerRedisCacheManager(BeanDefinitionRegistry registry, CacheProperty.RedisProperties redis) {
		String beanName = CacheConstants.CACHE_MANAGER_REDIS;
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CacheManager.class, () -> {
			// 获取已存在的RedisTemplate Bean
			RedisTemplate<String, Object> redisTemplate = beanFactory.getBean(RedisTemplate.class);

			// 2. 构建默认的Redis缓存配置
			RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration
					.defaultCacheConfig()
					// 设置key序列化器
					.serializeKeysWith(RedisSerializationContext.SerializationPair
							.fromSerializer(redisTemplate.getStringSerializer()))
					// 设置value序列化器
					.serializeValuesWith(RedisSerializationContext.SerializationPair
							.fromSerializer(redisTemplate.getValueSerializer()))
					.disableCachingNullValues()
					// 设置缓存前缀
					.computePrefixWith(k -> redis.getCacheManager().getKeyPrefix() + k)
					// 设置默认过期时间
					.entryTtl(Duration.ofSeconds(redis.getCacheManager().getExpireSeconds()));

			// 3. 构建并返回RedisCacheManager
			return RedisCacheManager.RedisCacheManagerBuilder
					.fromConnectionFactory(redisTemplate.getConnectionFactory())
					.cacheDefaults(defaultCacheConfiguration)
					.transactionAware()
					.build();
		});
		builder.setPrimary(redis.getCacheManager().isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		log.info(CommonUtil.createBeanMessage(beanName));
	}


}
