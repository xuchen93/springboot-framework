package com.github.xuchen93.springboot.framework.base.support.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 方法拦截统计对象
 */
@Data
public class MethodStats implements Comparable<MethodStats> {
	private int type;
	private String uniqueKey;
	private int successCount;
	private int failCount;
	private long totalTime;
	private int topCostSize = 10;
	private List<Long> topCostTime = new ArrayList<>();

	public MethodStats(int type, String uniqueKey) {
		this.type = type;
		this.uniqueKey = uniqueKey;
	}


	public synchronized void addSuccess(long costTime) {
		successCount++;
		totalTime += costTime;
		updateTimeRecord(costTime);
	}


	public synchronized void addFail(long costTime) {
		failCount++;
		totalTime += costTime;
		updateTimeRecord(costTime);
	}

	public synchronized void reset() {
		successCount = 0;
		failCount = 0;
		totalTime = 0;
		topCostTime.clear();
	}

	private void updateTimeRecord(long costTime) {
		if (topCostTime.size() < topCostSize) {
			topCostTime.add(costTime);
		} else {
			Long min = Collections.min(topCostTime);
			if (min < costTime) {
				int i = topCostTime.indexOf(min);
				topCostTime.set(i, costTime);
			}
		}
	}

	@Override
	public String toString() {
		return StrUtil.format(
				"{}：{}({}|{})，{}|{}|{}",
				uniqueKey,
				successCount + failCount, successCount, failCount,
				totalTime, totalTime / (successCount + failCount), topCostTime
		);
	}

	@Override
	public int compareTo(MethodStats o) {
		return this.getUniqueKey().compareTo(o.getUniqueKey());
	}
}
