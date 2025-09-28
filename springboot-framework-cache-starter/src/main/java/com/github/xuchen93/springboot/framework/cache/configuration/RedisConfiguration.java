package com.github.xuchen93.springboot.framework.cache.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.cache.aop.RedisAnnotationLockAspect;
import com.github.xuchen93.springboot.framework.cache.properties.CacheProperty;
import com.github.xuchen93.springboot.framework.cache.redis.RedisCacheProxy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@ConditionalOnProperty(prefix = CacheProperty.PROPERTY_PREFIX, name = "redis.enable", havingValue = "true", matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({RedissonAutoConfiguration.class, org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class})
public class RedisConfiguration {

	@Bean
	public RedisSerializer<?> redisSerializer() {
		log.info(CommonUtil.createBeanMessage("redisSerializer"));
		ObjectMapper objectMapper = new ObjectMapper();
		// jdk8之后引入的LocalDateTime，此处支持LocalDateTime
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		SimpleModule timeModule = new JavaTimeModule()
				.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter))
				.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(timeFormatter))
				.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
				.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(timeFormatter));
		objectMapper.registerModule(timeModule);
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
	}


	@Bean
	public RedissonClient redissonClient(@Value("${spring.data.redis.host}") String host,
										 @Value("${spring.data.redis.port}") String port,
										 @Value("${spring.data.redis.password}") String password,
										 @Value("${spring.data.redis.database:0}") int database
	) {
		log.info(CommonUtil.createBeanMessage("redissonClient"));
		Config config = new Config();
		config.useSingleServer()
				.setAddress("redis://" + host + ":" + port)
				.setPassword(password)
				.setDatabase(database)
				.setConnectionMinimumIdleSize(1)
				.setConnectionPoolSize(10);
		return Redisson.create(config);
	}

	/**
	 * RedisTemplate 注入,不指定泛型，可扩展为多种数据结构
	 */
	@Bean
	@Primary
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory, RedisSerializer<?> redisSerializer) {
		log.info(CommonUtil.createBeanMessage("redisTemplate"));
		// 指定序列化方式
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(redisSerializer);
		redisTemplate.setHashValueSerializer(redisSerializer);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		return redisTemplate;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory, RedisSerializer<?> redisSerializer) {
		log.info(CommonUtil.createBeanMessage("stringRedisTemplate"));
		StringRedisTemplate redisTemplate = new StringRedisTemplate();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		return redisTemplate;
	}

	@Bean
	public RedisCacheProxy redisCacheProxy(RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
		return new RedisCacheProxy(redisTemplate, redissonClient);
	}

}
