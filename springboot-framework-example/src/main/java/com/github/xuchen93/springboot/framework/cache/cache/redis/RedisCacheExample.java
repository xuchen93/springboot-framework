package com.github.xuchen93.springboot.framework.cache.cache.redis;

import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.base.convention.annotation.LogAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@LogAnnotation
public class RedisCacheExample {

	@Cacheable(value = "redisCache", key = "#id")
	public Map<Integer, String> getMap(Integer id) {
		log.info("{} execute：{}", CommonUtil.getCurrentMethodName(), id);
		return Map.of(id, "value" + id);
	}
}
