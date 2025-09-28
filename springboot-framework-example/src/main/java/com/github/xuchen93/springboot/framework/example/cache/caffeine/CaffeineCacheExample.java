package com.github.xuchen93.springboot.framework.example.cache.caffeine;

import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.base.support.annotation.LogAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@LogAnnotation
public class CaffeineCacheExample {

	@Cacheable(value = "CaffeineCacheExample.getMap", key = "#id")
	public Map<Integer, String> cache(Integer id) {
		log.info("{} execute：{}", CommonUtil.getCurrentMethodName(), id);
		return Map.of(id, "cache" + id);
	}

	@Cacheable(value = "CaffeineCacheExample.cacheWriteExpire", key = "#id")
	public Map<Integer, String> cacheWriteExpire(Integer id) {
		log.info("{} execute：{}", CommonUtil.getCurrentMethodName(), id);
		return Map.of(id, "cacheWriteExpire" + id);
	}

	@Cacheable(value = "CaffeineCacheExample.cacheAccessExpire", key = "#id")
	public Map<Integer, String> cacheAccessExpire(Integer id) {
		log.info("{} execute：{}", CommonUtil.getCurrentMethodName(), id);
		return Map.of(id, "cacheAccessExpire" + id);
	}
}
