package com.github.xuchen93.springboot.framework.base.support.manager;


import com.github.xuchen93.springboot.framework.base.convention.enums.MethodStatsType;
import com.github.xuchen93.springboot.framework.base.support.model.MethodStats;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MethodStatsManager {
	private static volatile ConcurrentHashMap<String, MethodStats> METHOD_STATS_MAP = new ConcurrentHashMap<>();


	private static MethodStats getMethodStats(int type, String uniqueKey) {
		return METHOD_STATS_MAP.computeIfAbsent(type + uniqueKey, s -> new MethodStats(type, uniqueKey));
	}

	public static void addSuccess(int type, String uniqueKey, long costTime) {
		getMethodStats(type, uniqueKey).addSuccess(costTime);
	}

	public static void addFail(int type, String uniqueKey, long costTime) {
		getMethodStats(type, uniqueKey).addFail(costTime);
	}

	public static List<MethodStats> peekAll() {
		return METHOD_STATS_MAP.values().stream().filter(stats -> stats.getSuccessCount() > 0 || stats.getFailCount() > 0).sorted().toList();
	}

	public static List<MethodStats> peekByType(int type) {
		return METHOD_STATS_MAP.values().stream()
				.filter(stats -> stats.getType() == type)
				.filter(stats -> stats.getSuccessCount() > 0 || stats.getFailCount() > 0)
				.sorted()
				.toList();
	}

	public static void logByType(int type, boolean consume) {
		StringBuffer stringBuffer = new StringBuffer();
		String title = String.format("[%s]方法统计:uniqueKey，调用次：总数(成功数|失败数)，耗时(ms)：总计|平均|TopN值", MethodStatsType.getNameByType(type));
		stringBuffer.append(title);
		METHOD_STATS_MAP.values().stream()
				.filter(stats -> stats.getType() == type)
				.filter(stats -> stats.getSuccessCount() + stats.getFailCount() > 0)
				.sorted()
				.forEach(i -> {
					stringBuffer.append("\n").append(i.toString());
					if (consume) {
						i.reset();
					}
				});
		if (stringBuffer.length() != title.length()) {
			log.info(stringBuffer.toString());
		}
	}

	/**
	 * 非阻塞情况，无法保证数据的绝对精准。（MethodStats可能还没有更新）
	 * 考虑到使用场景仅仅是方法调用统计，允许一定的误差
	 * 建议调用频率不低于1小时一次
	 */
	public static List<MethodStats> consume() {
		// 1. 创建新的空容器
		ConcurrentHashMap<String, MethodStats> newMap = new ConcurrentHashMap<>();
		// 2. 原子性替换旧容器（volatile保证可见性，确保生产者后续写入新容器）
		ConcurrentHashMap<String, MethodStats> oldMap = METHOD_STATS_MAP;
		METHOD_STATS_MAP = newMap;
		// 3. 返回旧容器中的数据（此时旧容器已不再被写入，可安全消费）
		return oldMap.values().stream().sorted().toList();
	}
}
